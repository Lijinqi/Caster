package east.orientation.caster.cast.record;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import east.orientation.caster.local.AudioConfig;
import east.orientation.caster.util.AudioUtils;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;
import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/3/28.
 *
 * 录音 编码为aac格式
 */

public class AACRecorder {
    private static final String TAG = "AACRecorder";

    private MediaCodec mAudioCodec;

    private AudioRecord mAudioRecord;

    private AudioConfig mAudioConfig;

    private volatile boolean mStopFlag;

    private Thread mAudioProcessor;

    private MediaCodec.BufferInfo mAudioEncodeBuffer;

    private FileOutputStream fos;

    private BufferedOutputStream bos;

    private Runnable mAudioProRunnable = new Runnable() {
        @Override
        public void run() {
            while (!mStopFlag){
                if(mAudioCodec != null){
                    offerEncoder();
                }
            }
        }
    };


    /**
     * start
     */
    public void start(){
        mStopFlag = false;
        //
        prepareFileIO();
        //
        mAudioEncodeBuffer = new MediaCodec.BufferInfo();
        mAudioConfig = AudioConfig.createDefault();

        // 音频编解码
        mAudioCodec = AudioUtils.getAudioMediaCodec(mAudioConfig);
        mAudioCodec.start();

        // 采集音频
        mAudioRecord = AudioUtils.getAudioRecord(mAudioConfig);
        mAudioRecord.startRecording();

        //
        mAudioProcessor = new Thread(mAudioProRunnable);
        mAudioProcessor.start();
    }

    synchronized void offerEncoder() {
        if(mAudioCodec == null) {
            return;
        }

        int inputBufferIndex = mAudioCodec.dequeueInputBuffer(12000);
        if (inputBufferIndex >= 0) {
            final AudioRecord r = Objects.requireNonNull(mAudioRecord);
            final boolean eos = r.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
            final ByteBuffer frame = mAudioCodec.getInputBuffer(inputBufferIndex);
            int offset = frame.position();
            int limit = frame.limit();
            int readLen = 0;
            if (!eos) {
                readLen = r.read(frame, limit);

                if (readLen < 0) {
                    readLen = 0;
                }
            }
            int flags = BUFFER_FLAG_KEY_FRAME;
            if (eos) {
                flags = BUFFER_FLAG_END_OF_STREAM;
            }
            mAudioCodec.queueInputBuffer(inputBufferIndex, offset, readLen, 0, flags);
        }

        int outputBufferIndex = mAudioCodec.dequeueOutputBuffer(mAudioEncodeBuffer, 12000);

        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mAudioCodec.getOutputBuffer(outputBufferIndex);
            //
            outputBuffer.position(mAudioEncodeBuffer.offset);
            outputBuffer.limit(mAudioEncodeBuffer.offset+ mAudioEncodeBuffer.size+7);
            // 添加ADTS
            byte[] buf = new byte[mAudioEncodeBuffer.size+7];
            addADTStoPacket(buf, mAudioEncodeBuffer.size+7);
            // 将编码得到的AAC数据 取出到byte[]中
            outputBuffer.get(buf, 7, mAudioEncodeBuffer.size);


            // todo 写入队列
            getAppInfo().getAudioStream().add(buf);

            // todo 写入文件
            writeToFile(buf);

            if (mAudioCodec != null){
                mAudioCodec.releaseOutputBuffer(outputBufferIndex, false);
            }
            if ((mAudioEncodeBuffer.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                //
                stop();
            }
            outputBufferIndex = mAudioCodec.dequeueOutputBuffer(mAudioEncodeBuffer, 0);
        }
    }

    /**
     * 音频帧添加adts
     *
     * @param packet    打包帧
     * @param packetLen 包长
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC，MediaCodecInfo.CodecProfileLevel.AACObjectLC;
        int freqIdx = 4;  //44100, 见后面注释avpriv_mpeg4audio_sample_rates中32000对应的数组下标，来自ffmpeg源码
        int chanCfg = 1;  //见后面注释channel_configuration，Stero双声道立体声

       /*int avpriv_mpeg4audio_sample_rates[] = {
           96000, 88200, 64000, 48000, 44100, 32000,
                   24000, 22050, 16000, 12000, 11025, 8000, 7350
       };
       channel_configuration: 表示声道数chanCfg
       0: Defined in AOT Specifc Config
       1: 1 channel: front-center
       2: 2 channels: front-left, front-right
       3: 3 channels: front-center, front-left, front-right
       4: 4 channels: front-center, front-left, front-right, back-center
       5: 5 channels: front-center, front-left, front-right, back-left, back-right
       6: 6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel
       7: 8 channels: front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel
       8-15: Reserved
       */

        // fill in ADTS data
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
        packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
        packet[4] = (byte)((packetLen&0x7FF) >> 3);
        packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
        packet[6] = (byte)0xFC;
    }

    private void prepareFileIO(){
        try {
            File file = new File(getAlbumStorageDir("Mic"),String.format("mic_%d.acc", System.currentTimeMillis()));
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos,200*1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStorageDirectory(), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    private void writeToFile(byte[] input){
        try {
            if (bos != null)
                bos.write(input,0,input.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseIO(){
        try {
            if (bos != null) {
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    bos=null;
                }
            }
        }

        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            fos=null;
        }
    }


    /**
     * stop
     */
    synchronized public void stop() {

        mStopFlag = true;
        //
        releaseIO();

        if (mAudioRecord != null){
            mAudioRecord.stop();
            mAudioRecord.release();
        }
        if (mAudioCodec != null) {
            mAudioCodec.stop();
            mAudioCodec.release();
            mAudioCodec = null;
        }
    }
}
