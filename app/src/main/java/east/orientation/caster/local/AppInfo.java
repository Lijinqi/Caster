package east.orientation.caster.local;

import android.content.Context;
import android.net.wifi.WifiManager;


import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;

import east.orientation.caster.cast.CastScreenService;


/**
 * Created by ljq on 2018/1/9.
 */

public class AppInfo {
    private CastScreenService mCastScreenService;


    private WifiManager mWifiManager;
    private WifiManager.MulticastLock mMulticastLock;
    private volatile boolean isActivityRunning;// 是否在activity
    private volatile boolean isStreamRunning;// 是否在录屏
    private volatile boolean isServerConnected;// 是否连接服务器
    private IConnectionManager mConnectionManager;// 连接tcp管理器

    private LinkedBlockingQueue<byte[]> mScreenVideoStream = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<byte[]> mAudioStream = new LinkedBlockingQueue<>();

    public AppInfo(final Context context) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mMulticastLock = mWifiManager.createMulticastLock("east.orientation.caster");
        mMulticastLock.acquire();
    }

    public CastScreenService getCastScreenService() {
        return mCastScreenService;
    }

    public void setCastScreenService(CastScreenService castScreenService) {
        mCastScreenService = castScreenService;
    }

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    public WifiManager.MulticastLock getMulticastLock() {
        return mMulticastLock;
    }

    public IConnectionManager getConnectionManager() {
        return mConnectionManager;
    }

    public void setConnectionManager(IConnectionManager connectionManager) {
        mConnectionManager = connectionManager;
    }

    public LinkedBlockingQueue<byte[]> getScreenVideoStream() {
        return mScreenVideoStream;
    }

    public LinkedBlockingQueue<byte[]> getAudioStream() {
        return mAudioStream;
    }

    public boolean isServerConnected() {
        return isServerConnected;
    }

    public void setServerConnected(boolean serverConnected) {
        isServerConnected = serverConnected;
    }

    public void setActivityRunning(final boolean activityRunning) {
        isActivityRunning = activityRunning;
    }

    public void setStreamRunning(final boolean streamRunning) {
        isStreamRunning = streamRunning;
    }

    public boolean isActivityRunning() {
        return isActivityRunning;
    }

    public boolean isStreamRunning() {
        return isStreamRunning;
    }

    public boolean isWiFiConnected() {
        return mWifiManager.getConnectionInfo().getIpAddress() != 0;
    }

}
