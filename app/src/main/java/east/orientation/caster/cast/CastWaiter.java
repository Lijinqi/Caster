package east.orientation.caster.cast;

import android.util.Log;

import com.xuhao.android.libsocket.utils.BytesUtils;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import java.util.TimerTask;

import east.orientation.caster.local.Common;


/**
 * Created by ljq on 2018/4/11.
 *
 */

public class CastWaiter {
    private static final String DEFAULT_CAST_IP = "224.0.0.251";
    private static final int DEFAULT_CAST_PORT = 3388;

    //===========================================
    private static CastWaiter instance = new CastWaiter() ;

    private CastWaiter(){

    }

    public static CastWaiter getInstance(){
        return instance;
    }
    //============================================

    private boolean isSearching = true;

    private static Object mLock = new Object();

    private OnSearchListener mSearchListener;

    private MulticastSocket mMulticastSocket;

    private SearchThread mSearchThread;

    public OnSearchListener getSearchListener() {
        return mSearchListener;
    }

    public void setSearchListener(OnSearchListener searchListener) {
        mSearchListener = searchListener;
    }

    private class SearchThread extends Thread{
        @Override
        public void run() {
            try {
                InetAddress address = InetAddress.getByName(DEFAULT_CAST_IP);
                mMulticastSocket = new MulticastSocket(DEFAULT_CAST_PORT);

                mMulticastSocket.joinGroup(address);

                byte[] revBytes = new byte[24];
                DatagramPacket packet = new DatagramPacket(revBytes,revBytes.length,address,DEFAULT_CAST_PORT);
                Log.e("@@","-receive -"+packet.getAddress());
                mMulticastSocket.receive(packet);
                parserData(packet);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("@@","-receive err-"+e);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        CastWaiter.this.stop();
                        CastWaiter.this.start();
                    }
                },1000);
            }
        }
    }

    private void parserData(DatagramPacket packet) {
        if (packet == null)
            return;

        byte[] data = packet.getData();
        byte[] len = new byte[4];
        byte[] head = new byte[4];
        byte[] port = new byte[4];

        System.arraycopy(data,0,len,0,4);
        System.arraycopy(data,4,head,0,4);
        System.arraycopy(data,20,port,0,2);
        int Port = BytesUtils.bytesToInt(port,0);

        int Len = BytesUtils.bytesToInt(len,0);
        String Head = new String(head,0,head.length);
        if (Len == 20 && Common.HEAD.equals(Head)){
            mSearchListener.onSearchFinished(packet.getAddress().getHostAddress(),Port);
            isSearching = false;
        }
    }

    public void start(){
        synchronized (mLock){
            mSearchThread = new SearchThread();
            mSearchThread.start();
            Log.e("@@","waiter start");
        }
    }

    public void stop(){
        synchronized (mLock){
            mSearchThread.interrupt();
            //mSearchThread = null;
            isSearching = false;
            //lock.release();
            Log.e("@@","waiter stop");
        }
    }

    public interface OnSearchListener{
        void onSearchFinished(String ip,int port);
    }
}
