package east.orientation.caster.util;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import east.orientation.caster.local.AudioConfig;

/**
 * @Title: AudioUtils
 * @Package cn.wecoder.camerademo.audio
 * @Description:
 * @Author Jim
 * @Date 16/3/24
 * @Time 下午5:54
 * @Version
 */
public class AudioUtils {
    public static boolean checkMicSupport(AudioConfig audioConfiguration) {
        boolean result;
        int recordBufferSize = getRecordBufferSize(audioConfiguration);
        byte[] mRecordBuffer = new byte[recordBufferSize];
        AudioRecord audioRecord = getAudioRecord(audioConfiguration);
        try {
            audioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int readLen = audioRecord.read(mRecordBuffer, 0, recordBufferSize);
        result = readLen >= 0;
        try {
            audioRecord.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getRecordBufferSize(AudioConfig audioConfiguration) {
        int frequency = audioConfiguration.frequency;
        int audioEncoding = audioConfiguration.encoding;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        if(audioConfiguration.channelCount == 2) {
            channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
        }
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        int bytesPerFrame = audioConfiguration.encoding;
		/* Get number of samples. Calculate the buffer size
		 * (round up to the factor of given frame size)
		 * 使能被整除，方便下面的周期性通知
		 * */
        int frameSize = bufferSize / bytesPerFrame;
        if (frameSize % AudioConfig.FRAME_COUNT != 0) {
            frameSize += (AudioConfig.FRAME_COUNT - frameSize % AudioConfig.FRAME_COUNT);
            bufferSize = frameSize * bytesPerFrame;
        }

        return bufferSize;
    }

    @TargetApi(18)
    public static AudioRecord getAudioRecord(AudioConfig audioConfiguration) {
        int frequency = audioConfiguration.frequency;
        int audioEncoding = audioConfiguration.encoding;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        if(audioConfiguration.channelCount == 2) {
            channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
        }
        int audioSource = MediaRecorder.AudioSource.MIC;
        if(audioConfiguration.aec) {
            audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        }
        AudioRecord audioRecord = new AudioRecord(audioSource, frequency,
                channelConfiguration, audioEncoding, getRecordBufferSize(audioConfiguration));
        return audioRecord;
    }

    public static MediaCodec getAudioMediaCodec(AudioConfig configuration){
        MediaFormat format = MediaFormat.createAudioFormat(configuration.mime, configuration.frequency, configuration.channelCount);
        if(configuration.mime.equals(AudioConfig.DEFAULT_MIME)) {
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, configuration.aacProfile);
        }
        format.setInteger(MediaFormat.KEY_BIT_RATE, configuration.maxBps * 1024);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, configuration.frequency);
        int maxInputSize = AudioUtils.getRecordBufferSize(configuration);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, configuration.channelCount);

        MediaCodec mediaCodec = null;
        try {
            mediaCodec = MediaCodec.createEncoderByType(configuration.mime);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
        }
        return mediaCodec;
    }
}
