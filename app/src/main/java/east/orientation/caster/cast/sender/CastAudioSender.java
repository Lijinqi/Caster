package east.orientation.caster.cast.sender;

import android.util.Log;

import east.orientation.caster.cast.request.AudioCastRequest;
import east.orientation.caster.cast.request.VideoCastRequest;

import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/8/2.
 */

public class CastAudioSender {
    private static final String TAG = "CastFrameSender";
    private static SendThread mSendThread;
    private static volatile boolean isRunning;

    private static class SendThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                if (!isRunning) break;

                byte[] audioStream;
                try {
                    audioStream = getAppInfo().getAudioStream().take();
                    // 发送audio数据
                    getAppInfo().getConnectionManager().send(new AudioCastRequest(audioStream));
                    //Log.d(TAG,"audio send:"+audioStream.length);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public static void start() {
        synchronized (CastAudioSender.class) {
            mSendThread = new SendThread();
            mSendThread.start();
            isRunning = true;
        }
    }

    public static void stop() {
        synchronized (CastAudioSender.class) {
            isRunning = false;
            if (mSendThread != null)
                mSendThread.interrupt();
        }
    }
}
