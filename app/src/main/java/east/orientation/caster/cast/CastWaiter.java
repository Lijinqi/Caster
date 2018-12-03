package east.orientation.caster.cast;

import android.util.Log;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import east.orientation.caster.local.Common;
import east.orientation.caster.util.BytesUtils;

import static east.orientation.caster.CastApplication.getAppInfo;


/**
 * Created by ljq on 2018/4/11.
 */

public class CastWaiter {
    private static final String DEFAULT_CAST_IP = "224.0.0.251";
    private static final int DEFAULT_CAST_PORT = 3388;

    //===========================================
    private static CastWaiter instance = new CastWaiter();

    private CastWaiter() {

    }

    public static CastWaiter getInstance() {
        return instance;
    }
    //============================================

    private boolean isSearching = false;

    private static Object mLock = new Object();

    private OnSearchListener mSearchListener;
    private InetSocketAddress mSocketAddress;
    private NetworkInterface mNetworkInterface;
    private MulticastSocket mMulticastSocket;

    private SearchThread mSearchThread;

    public OnSearchListener getSearchListener() {
        return mSearchListener;
    }

    public void setSearchListener(OnSearchListener searchListener) {
        mSearchListener = searchListener;
    }

    private class SearchThread extends Thread {
        @Override
        public void run() {
            try {
                mSocketAddress = new InetSocketAddress(DEFAULT_CAST_IP, DEFAULT_CAST_PORT);
                mMulticastSocket = new MulticastSocket(DEFAULT_CAST_PORT);

                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface iface = networkInterfaces.nextElement();
                    try {
                        Log.e("@@", "interface - " + iface.getDisplayName());
                    } catch (Exception e) {
                    }
                }
                mNetworkInterface = NetworkInterface.getByName("wlan0");
                mMulticastSocket.setNetworkInterface(mNetworkInterface);
                mMulticastSocket.joinGroup(mSocketAddress, mNetworkInterface);
                mMulticastSocket.setSoTimeout(3000);
                byte[] revBytes = new byte[24];
                DatagramPacket packet = new DatagramPacket(revBytes, revBytes.length, mSocketAddress);

                isSearching = true;

                mMulticastSocket.receive(packet);
                parserData(packet);

            } catch (Exception e) {
                e.printStackTrace();
                isSearching = false;
                Log.e("@@", "-receive err-" + e);
                CastWaiter.this.stop();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                        CastWaiter.this.start();
                    }
                }, 1000);
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

        System.arraycopy(data, 0, len, 0, 4);
        System.arraycopy(data, 4, head, 0, 4);
        System.arraycopy(data, 20, port, 0, 2);
        int Port = BytesUtils.bytesToInt(port, 0);

        int Len = BytesUtils.bytesToInt(len, 0);
        String Head = new String(head, 0, head.length);
        if (Len == 20 && Common.HEAD.equals(Head)) {
            mSearchListener.onSearchFinished(packet.getAddress().getHostAddress(), Port);
            isSearching = false;
        }
    }

    public void start() {
        synchronized (mLock) {
            if (mSearchThread != null) return;
            mSearchThread = new SearchThread();
            mSearchThread.start();
            Log.e("@@", "waiter start");
        }
    }

    public void stop() {
        synchronized (mLock) {
            isSearching = false;

            try {
                mMulticastSocket.leaveGroup(mSocketAddress, mNetworkInterface);
                mMulticastSocket.close();
                mMulticastSocket = null;
                //
                mSearchThread.interrupt();
                mSearchThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("@@", "waiter stop");
        }
    }

    public interface OnSearchListener {
        void onSearchFinished(String ip, int port);
    }
}
