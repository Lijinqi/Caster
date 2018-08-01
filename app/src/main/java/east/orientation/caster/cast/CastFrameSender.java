package east.orientation.caster.cast;

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

    private static class SendThread extends Thread{
        @Override
        public void run() {
            while (!isInterrupted()){
                if(!isRunning) break;
                byte[] videoStream;
                byte[] audioStream;
                try {
                    videoStream = getAppInfo().getScreenVideoStream().take();
//                  audioStream = getAppInfo().getAudioStream().take();

                    if(getAppInfo().getConnectionManager() != null && getAppInfo().isStreamRunning()){
                        if (videoStream != null){
                            // 发送video数据
                            getAppInfo().getConnectionManager().send(new VideoCastRequest(videoStream));
                            //Log.i(TAG,"video send:"+videoStream.length);
                        }
                    }
//                    if(audioStream != null&& getAppInfo().getConnectionManager() != null){
//                        // 发送audio数据
//                        getAppInfo().getConnectionManager().send(new AudioCastRequest(audioStream));
//                        Log.i("@@"," audio send:"+getAppInfo().getAudioStream().size()+" "+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS") .format(new Date() ));
//
//                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void start(){
        synchronized (mLock){
            mSendThread = new SendThread();
            mSendThread.start();
            isRunning = true;
        }
    }

    public static void stop(){
        synchronized (mLock){
            isRunning = false;
            if (mSendThread != null)
                mSendThread.interrupt();
        }
    }
}
