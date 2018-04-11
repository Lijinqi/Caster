package east.orientation.caster.cast;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import east.orientation.caster.request.AudioCastRequest;
import east.orientation.caster.request.VideoCastRequest;


import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/1/12.
 */

public class FrameSender {
    private static final String TAG = "TcpSender";
    private static Object mLock = new Object();
    private static SendThread mSendThread;
    private static volatile boolean isRunning;

    private final ExecutorService mClientThreadPool = Executors.newCachedThreadPool();

    private static class SendThread extends Thread{
        @Override
        public void run() {
            while (!isInterrupted()){
                if(!isRunning) break;
                byte[] videoStream = getAppInfo().getScreenVideoStream().poll();
                if(videoStream != null){
                    // 发送video数据
                    Log.e(TAG,"TcpSender video:"+videoStream.length);
                    getAppInfo().getConnectionManager().send(new VideoCastRequest(videoStream));
                }
                byte[] audioStream = getAppInfo().getAudioStream().poll();
                if(audioStream != null){
                    // 发送audio数据
                    Log.e(TAG,"TcpSender  audio:"+audioStream.length);
                    getAppInfo().getConnectionManager().send(new AudioCastRequest(audioStream));
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
