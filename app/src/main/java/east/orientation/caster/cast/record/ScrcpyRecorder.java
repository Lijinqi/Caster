package east.orientation.caster.cast.record;

import android.graphics.Rect;
import android.net.LocalServerSocket;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import east.orientation.caster.local.VideoConfig;
import east.orientation.caster.scrcpy.Device;
import east.orientation.caster.scrcpy.Options;
import east.orientation.caster.scrcpy.ScreenEncoder;

/**
 * @author ljq
 * @date 2019/5/6
 * @description
 */
public class ScrcpyRecorder {
    private final static String TAG = "ScrcpyRecorder";

    private Options mOptions;
    private Device mDevice;
    private ScreenEncoder mScreenEncoder;

    public void start() {

        mOptions = createOptions();
        mDevice = new Device(mOptions);
        mScreenEncoder = new ScreenEncoder(mOptions.getSendFrameMeta(), mOptions.getBitRate());
        try {
            // synchronous
            mScreenEncoder.streamScreen(mDevice);
        } catch (IOException e) {
            // this is expected on close
            Log.d(TAG,"Screen streaming stopped");
        }
    }

    public void stop() {
        mScreenEncoder.stop();
    }

    private static Options createOptions() {
        Options options = new Options();

        int maxSize = 1920; // multiple of 8
        options.setMaxSize(maxSize);

        options.setBitRate(VideoConfig.BITRATE_OPTIONS[0]);

        Rect crop = parseCrop();
        options.setCrop(crop);

        options.setSendFrameMeta(false);

        return options;
    }

    private static Rect parseCrop() {

        int width = 1200;
        int height = 1920;
        int x = 0;
        int y = 0;
        return new Rect(x, y, x + width, y + height);
    }
}
