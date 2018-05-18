package east.orientation.caster.cast;

import east.orientation.caster.cast.request.AudioCastRequest;
import east.orientation.caster.cast.request.BaseRequest;
import east.orientation.caster.cast.request.VideoCastRequest;

import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/1/12.
 */

public class FrameSender {
    private static final String TAG = "TcpSender";
    private static Object mLock = new Object();
    private static SendThread mSendThread;
    private static volatile boolean isRunning;

    private static class SendThread extends Thread{
        @Override
        public void run() {
            while (!isInterrupted()){
                if(!isRunning) break;
                byte[] videoStream = getAppInfo().getScreenVideoStream().poll();
                byte[] audioStream = getAppInfo().getAudioStream().poll();

                if(videoStream != null && getAppInfo().getConnectionManager() != null){
                    // 发送video数据
                    getAppInfo().getConnectionManager().send(new VideoCastRequest(videoStream));
//                    Log.i("@@","video send:"+getAppInfo().getScreenVideoStream().size()+ " "+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS") .format(new Date() ));
                }
                if(audioStream != null&& getAppInfo().getConnectionManager() != null){
                    // 发送audio数据
                    getAppInfo().getConnectionManager().send(new AudioCastRequest(audioStream));
//                    Log.i("@@"," audio send:"+getAppInfo().getAudioStream().size()+" "+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS") .format(new Date() ));

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
            mSendThread.interrupt();
        }
    }
}
