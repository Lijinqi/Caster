package east.orientation.caster.local;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.xuhao.android.libsocket.sdk.connection.IConnectionManager;

import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * Created by ljq on 2018/1/9.
 */

public class AppInfo {
    private WifiManager mWifiManager;

    private volatile boolean isActivityRunning;// 是否在activity
    private volatile boolean isStreamRunning;// 是否在录屏
    private volatile boolean isServerConnected;// 是否连接服务器

    private IConnectionManager mConnectionManager;// 连接tcp管理器
    private String mServerIp;

    private ConcurrentLinkedDeque<byte[]> mScreenStream = new ConcurrentLinkedDeque<>();

    public AppInfo(final Context context) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public IConnectionManager getConnectionManager() {
        return mConnectionManager;
    }

    public void setConnectionManager(IConnectionManager connectionManager) {
        mConnectionManager = connectionManager;
    }

    public String getServerIp() {
        return mServerIp;
    }

    public void setServerIp(String serverIp) {
        mServerIp = serverIp;
    }

    public ConcurrentLinkedDeque<byte[]> getScreenStream() {
        return mScreenStream;
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
