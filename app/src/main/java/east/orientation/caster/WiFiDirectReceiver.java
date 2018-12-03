package east.orientation.caster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by ljq on 2018/11/30.
 */

public class WiFiDirectReceiver extends BroadcastReceiver {
    private static final String TAG = "WIFI P2P";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pConfig mConfig = new WifiP2pConfig();
    private WifiP2pManager.ActionListener mActionListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG,"discover success");
        }

        @Override
        public void onFailure(int reason) {
            Log.d(TAG,"discover failed");
        }
    };

    private WifiP2pManager.PeerListListener mListener = peers -> {
        Log.d(TAG, "==================peers list size: "+peers.getDeviceList().size());
        mManager.removeGroup(mChannel,null);
        mManager.cancelConnect(mChannel,null);
        for(WifiP2pDevice device: peers.getDeviceList()){
            Log.d(TAG, "==================device status: "+device.status+" name: "+device.deviceName);
            if (device.deviceName.startsWith("LOLLIPOP")) {//LOLLIPOP
                mConfig.deviceAddress = device.deviceAddress;
                if (device.status == WifiP2pDevice.FAILED || device.status == WifiP2pDevice.AVAILABLE) {

                    mManager.connect(mChannel, mConfig, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "==============connnect success");

//                            if (directReceiver != null) {
//                                unregisterReceiver(directReceiver);
//                                directReceiver = null;
//                                initMiracast();
//                            }
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(TAG, "=================connect failed");
                        }
                    });
                }

            }
        }
        mManager.discoverPeers(mChannel,mActionListener);
    };

    public WiFiDirectReceiver() {
    }

    public WiFiDirectReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.mManager = manager;
        this.mChannel = channel;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("WIFI P2P", "===============wifi direct action: " + action);

        if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // 打开
                mManager.discoverPeers(mChannel, mActionListener);

            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                // 关闭

            }
        } else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            if(null != mManager){
                mManager.requestPeers(mChannel,mListener);
            }

        } else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
            Log.e("WIFI P2P", "==============CONNECTION_CHANGED_ACTION");

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if (info.groupFormed && info.isGroupOwner) {
                            //server 创建ServerSocket
                        } else if (info.groupFormed) {
                            // The other device acts as the client. In this case, we enable the
                            // get file button.
                            //连接Server。

                        }
                    }
                });
            } else {
                // It's a disconnect
            }

        } else if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
            Log.e("WIFI P2P", "==============THIS_DEVICE_CHANGED_ACTION");
        }
    }

}
