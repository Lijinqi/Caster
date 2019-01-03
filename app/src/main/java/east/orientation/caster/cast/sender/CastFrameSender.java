package east.orientation.caster.cast.sender;

import android.util.Log;

import east.orientation.caster.cast.request.VideoCastRequest;

import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/1/12.
 */

public class CastFrameSender {
    private static final String TAG = "CastFrameSender";
    private static Object mLock = new Object();
    private static SendThread mSendThread;
    private static volatile boolean isRunning;

    private static class SendThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                if (!isRunning) break;
                byte[] videoStream;
                try {
                    videoStream = getAppInfo().getScreenVideoStream().take();

                    if (getAppInfo().getConnectionManager() != null && getAppInfo().isStreamRunning()) {
                        if (videoStream != null) {
                            // 发送video数据
                            getAppInfo().getConnectionManager().send(new VideoCastRequest(videoStream));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void start() {
        synchronized (mLock) {
            mSendThread = new SendThread();
            mSendThread.start();
            isRunning = true;
        }
    }

    public static void stop() {
        synchronized (mLock) {
            isRunning = false;
            if (mSendThread != null)
                mSendThread.interrupt();
        }
    }
}
