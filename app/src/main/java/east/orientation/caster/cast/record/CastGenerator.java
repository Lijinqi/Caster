package east.orientation.caster.cast.record;

import east.orientation.caster.cast.record.AACRecorder;
import east.orientation.caster.cast.record.MP3Recorder;
import east.orientation.caster.cast.record.ScreenRecorder;


/**
 * Created by ljq on 2018/1/9.
 * <p>
 * 投屏录制音频和屏幕 管理类
 */
public class CastGenerator {
    private static final String TAG = "CastGenerator";
    // video
    private ScreenRecorder mScreenRecorder;
    private ScrcpyRecorder mScrcpyRecorder;

    //audio
    private AACRecorder mAACRecorder;
    private MP3Recorder mMP3Recorder;

    public CastGenerator() {
        //video
        mScreenRecorder = new ScreenRecorder();
        //mScrcpyRecorder = new ScrcpyRecorder();

        //audio
        //mAACRecorder = new AACRecorder();
        //mMP3Recorder = new MP3Recorder();
    }

    /**
     * 开启屏幕采集 和 音频采集
     */
    public void start() {
        // 录屏 输出视频
        mScreenRecorder.start();
        //mScrcpyRecorder.start();

        // aac 编码录音
        //mAACRecorder.start();
        // mp3 编码录音
        //mMP3Recorder.start();
    }

    /**
     * 释放编码器
     */
    public void stop() {
        mScreenRecorder.stop();
        //mScrcpyRecorder.stop();

        //mAACRecorder.stop();
        //mMP3Recorder.stop();
    }
}
