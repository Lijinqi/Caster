package east.orientation.caster.cast.miracast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.util.Log;

import east.orientation.caster.util.ToastUtil;

/**
 * Created by ljq on 2018/11/30.
 */

public class WiFiDirectReceiver extends BroadcastReceiver {
    private static final String TAG = "WIFI P2P";
    private Context mContext;
    private Handler mHandler;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pConfig mConfig = new WifiP2pConfig();
    private WiFiP2pConnectListener mWiFiP2pConnectListener;
    private boolean mLastGroupFormed = false;
    private boolean mWifiP2pSearching;

    private WifiP2pManager.ActionListener mActionListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            mWiFiP2pConnectListener.discover();
            Log.e(TAG, "discover success");
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "discover failed");
        }
    };

    private WifiP2pManager.PeerListListener mListener = peers -> {
        Log.e(TAG, "peers list size: " + peers.getDeviceList().size());
        mWiFiP2pConnectListener.requestPeer(peers);

        for (WifiP2pDevice device : peers.getDeviceList()) {
            Log.e(TAG, "device status: " + device.status + " name: " + device.deviceName +device.status);
            ToastUtil.showToast("device status: " + device.status + " name: " + device.deviceName +device.status);
            if (device.deviceName.startsWith("HAGIBIS")) {//LOLLIPOP
                mConfig.deviceAddress = device.deviceAddress;
                if (device.status == WifiP2pDevice.FAILED || device.status == WifiP2pDevice.AVAILABLE) {
                    mManager.connect(mChannel, mConfig, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // 连接已开始 并不代表成功连接
                            Log.e(TAG, "connect success");

                        }

                        @Override
                        public void onFailure(int reason) {
                            // 开始连接失败
                            Log.e(TAG, "connect failed reason: " + reason);
                        }
                    });
                    mManager.stopPeerDiscovery(mChannel,null);
                } else {
                    startSearch();
                }

            }
        }
    };

    public WiFiDirectReceiver(Context context,WiFiP2pConnectListener listener) {
        mContext = context;
        mHandler = new Handler();
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        mWiFiP2pConnectListener = listener;
    }

    public void disconnect() {
        // 停止搜索设备
        mManager.stopPeerDiscovery(mChannel,null);
        // 取消邀请
        mManager.cancelConnect(mChannel,null);
        // 移除群组
        mManager.removeGroup(mChannel,null);
        // 清理队列消息
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("WIFI P2P", "wifi direct action: " + action);

        if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // 打开
                startSearch();

            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                // 关闭

            }
        } else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            if (null != mManager) {
                mManager.requestPeers(mChannel, mListener);

            }
        } else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
            Log.e("WIFI P2P", "CONNECTION_CHANGED_ACTION");
            if (mManager == null) return;
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifip2pinfo = (WifiP2pInfo) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            if (networkInfo.isConnected()) {
                Log.d(TAG, "Connected");
                mWiFiP2pConnectListener.connected();
            } else if (!mLastGroupFormed) {
                //start a search when we are disconnected
                //but not on group removed broadcast event
                startSearch();
            }
            mLastGroupFormed = wifip2pinfo.groupFormed;
        } else if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {

            Log.e("WIFI P2P", "THIS_DEVICE_CHANGED_ACTION");

        } else if (action.equals(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)) {
            int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,
                    WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
            Log.d(TAG, "Discovery state changed: " + discoveryState);
            if (discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                mWifiP2pSearching = true;
            } else {
                mWifiP2pSearching = false;
            }
        }
    }

    private void startSearch() {
        if (mManager != null && !mWifiP2pSearching) {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {

                }
                public void onFailure(int reason) {
                    Log.d(TAG, " discover fail " + reason);
                }
            });
        }
    }
}
