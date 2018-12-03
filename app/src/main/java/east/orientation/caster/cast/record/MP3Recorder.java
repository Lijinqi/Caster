package east.orientation.caster.cast.record;

import android.media.AudioRecord;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import east.orientation.caster.local.AudioConfig;
import east.orientation.caster.util.AudioUtils;
import east.orientation.lamelibrary.LameUtil;

import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/4/2.
 * <p>
 * 录音 编码为MP3格式
 */

public class MP3Recorder {
    private static final String TAG = MP3Recorder.class.getSimpleName();
    private AudioRecord mAudioRecord = null;
    private int mBufferSize;
    private short[] mPCMBuffer;
    private Mp3EncodeThread mEncodeThread;
    private boolean mIsRecording;

    public void start() {
        if (mIsRecording) {
            return;
        }
        mIsRecording = true;
        initAudioRecorder();
        new Thread() {
            @Override
            public void run() {
                //设置线程权限
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                while (mIsRecording) {
                    int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                    if (readSize > 0) {
                        mEncodeThread.addTask(mPCMBuffer, readSize);
                    }
                }
                // release and finalize audioRecord
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
                // stop the encoding thread and try to wait
                // until the thread finishes its job
                mEncodeThread.sendStopMessage();
            }
        }.start();
    }

    public void stop() {
        mIsRecording = false;
    }

    private void initAudioRecorder() {
        // pcm 录音
        mBufferSize = AudioUtils.getRecordBufferSize(AudioConfig.createDefault());
        mAudioRecord = AudioUtils.getAudioRecord(AudioConfig.createDefault());
        mPCMBuffer = new short[mBufferSize];
        /*
		 * Initialize lame buffer
		 * mp3 sampling rate is the same as the recorded pcm sampling rate
		 * The bit rate is 32kbps
		 *
		 */
        LameUtil.init(AudioConfig.DEFAULT_FREQUENCY, AudioConfig.DEFAULT_CHANNEL_COUNT, AudioConfig.DEFAULT_FREQUENCY,
                AudioConfig.DEFAULT_MIN_BPS, AudioConfig.DEFAULT_LAME_MP3_QUALITY);
        // Create and run thread used to encode data
        mEncodeThread = new Mp3EncodeThread(mBufferSize);
        mEncodeThread.start();
        mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
        mAudioRecord.setPositionNotificationPeriod(AudioConfig.FRAME_COUNT);
        mAudioRecord.startRecording();
    }


    public static class Mp3EncodeThread extends HandlerThread implements AudioRecord.OnRecordPositionUpdateListener {
        private StopHandler mHandler;

        private static final int PROCESS_STOP = 1;

        private static final int DEFAULT_FRAME_SIZE = 144000 * AudioConfig.DEFAULT_MIN_BPS / AudioConfig.DEFAULT_FREQUENCY;// 根据LameUtil.init 参数计算 每帧长度

        private byte[] mMp3Buffer;

        private FileOutputStream mFileOutputStream;

        private byte[] mFramesBuffer;
        private int mFramesLength;
        private int mFrameOffset;

        private ArrayBlockingQueue<short[]> mPlayQueue = new ArrayBlockingQueue<short[]>(100);

        private int mReadSize;

        private static class StopHandler extends Handler {
            private Mp3EncodeThread encodeThread;

            public StopHandler(Looper looper, Mp3EncodeThread encodeThread) {
                super(looper);
                this.encodeThread = encodeThread;
            }

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == PROCESS_STOP) {
                    //处理缓冲区中的数据
                    while (encodeThread.processData() > 0) ;
                    // Cancel any event left in the queue
                    removeCallbacksAndMessages(null);
                    encodeThread.flushAndRelease();
                    getLooper().quit();
                }
            }
        }

        public Mp3EncodeThread(int bufferSize) {
            super("Mp3EncodeThread");
            /*try {
                File file = new File(getAlbumStorageDir("Mic"),String.format("mic_%d.mp3", System.currentTimeMillis()));
                mFileOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
            mMp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
            mFramesBuffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25)) * 2];
            mFramesLength = 0;
            mFrameOffset = 0;
        }

        @Override
        public synchronized void start() {
            super.start();
            mHandler = new StopHandler(getLooper(), this);
        }

        @Override
        public void onMarkerReached(AudioRecord recorder) {

        }

        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            processData();
        }

        /**
         * 从缓冲区中读取并处理数据，使用lame编码MP3
         *
         * @return 从缓冲区中读取的数据的长度
         * 缓冲区中没有数据时返回0
         */
        private int processData() {
            while (mPlayQueue.size() > 0) {
                short[] buffer;
                try {
                    if (mFramesLength - mFrameOffset < DEFAULT_FRAME_SIZE + DEFAULT_FRAME_SIZE / 2) {
                        if (mFrameOffset != 0) {
                            System.arraycopy(mFramesBuffer, mFrameOffset, mFramesBuffer, 0, mFramesLength - mFrameOffset);
                            mFramesLength -= mFrameOffset;
                            mFrameOffset = 0;
                        }

                        buffer = mPlayQueue.take();

                        int encodedSize = LameUtil.encode(buffer, buffer, mReadSize, mMp3Buffer);
                        System.arraycopy(mMp3Buffer, 0, mFramesBuffer, mFramesLength, encodedSize);
                        mFramesLength += encodedSize;

                        if (mFramesLength - mFrameOffset < DEFAULT_FRAME_SIZE + DEFAULT_FRAME_SIZE / 2)
                            continue;
                    }

                    boolean bAligned = false;
                    for (int i = mFrameOffset; i < mFramesLength; i++) {

                        if (mFramesBuffer[i] == -1 && mFramesBuffer[i + 1] == -5) {
                            if (i == mFrameOffset) {
                                bAligned = true;
                                i += DEFAULT_FRAME_SIZE - 1;
                            } else {
                                if (bAligned) {
                                    int fremesize = i - mFrameOffset;
                                    if (fremesize >= DEFAULT_FRAME_SIZE - 1 && fremesize <= DEFAULT_FRAME_SIZE + 2) {

                                        byte[] buf = new byte[fremesize];
                                        System.arraycopy(mFramesBuffer, mFrameOffset, buf, 0, fremesize);
                                        // 写入文件
                                        //mFileOutputStream.write(buf);
                                        // 加入传输队列
                                        getAppInfo().getAudioStream().add(buf);

                                        //Log.e(TAG,bytesToHexString(buf));
                                        //Log.d(TAG, "add size = " + buf.length);
                                    } else {
                                        mFrameOffset++;
                                        break;
                                    }
                                }
                                mFrameOffset = i;
                                bAligned = true;
                                i += DEFAULT_FRAME_SIZE - 1;
                                if (mFramesLength - mFrameOffset < DEFAULT_FRAME_SIZE + DEFAULT_FRAME_SIZE / 2)
                                    break;

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        private void check() {
            if (mHandler == null) {
                throw new IllegalStateException();
            }
        }

        public void sendStopMessage() {
            check();
            mHandler.sendEmptyMessage(PROCESS_STOP);
        }

        public Handler getHandler() {
            check();
            return mHandler;
        }

        public void addTask(short[] rawData, int readSize) {
            try {
                mReadSize = readSize;
                mPlayQueue.put(rawData);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Flush all data left in lame buffer to file
         */
        private void flushAndRelease() {
            //将MP3结尾信息写入buffer中
            final int flushResult = LameUtil.flush(mMp3Buffer);
            if (flushResult > 0) {
                try {
                    byte[] audioFrame = new byte[flushResult];
                    System.arraycopy(mMp3Buffer, 0, audioFrame, 0, flushResult);
                    // 写入文件
                    //mFileOutputStream.write(mMp3Buffer, 0, flushResult);
                    // 加入传输队列
                    getAppInfo().getAudioStream().add(audioFrame);

                    //Log.d(TAG,"flush add size"+audioFrame.length);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    /*if (mFileOutputStream != null) {
                        try {
                            mFileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/
                    LameUtil.close();
                }
            }
        }

        public File getAlbumStorageDir(String albumName) {
            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStorageDirectory(), albumName);
            if (!file.mkdirs()) {
                Log.e("@@@", "Directory not created");
            }
            return file;
        }

        public static String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("");
            if (src == null || src.length <= 0) {
                return null;
            }
            for (int i = 0; i < src.length; i++) {
                int v = src[i] & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv.toUpperCase() + " ");
            }
            return stringBuilder.toString();
        }
    }
}
