package east.orientation.caster.cast.miracast;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;

import east.orientation.caster.util.ToastUtil;

/**
 * Created by ljq on 2018/12/5.
 */

public class MiracastManager {
    private static final String TAG = "MiracastManager";
    private static final String ACTION_WIFI_DISPLAY_STATUS_CHANGED = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";
    private Context mContext;
    private WiFiDirectReceiver mWiFiDirectReceiver;
    private WiFiDisplayReceive mWiFiDisplayReceive;
    private boolean isRegisterWiFiDirect;
    private boolean isRegisterWiFiDisplay;
    private boolean isRegisterDisplayListener;

    public MiracastManager(Context context) {
        mContext = context;
    }

    public void start() {
        registerWiFiDirect();
    }

    public void stop() {
        unregisterWiFiDirect();
        unregisterWiFiDisplay();
        DisplayManagerGlobal.getInstance().disconnectWifiDisplay();
        if (mDisplayListener != null && isRegisterDisplayListener)
            DisplayManagerGlobal.getInstance().unregisterDisplayListener(mDisplayListener);
        if (mWiFiDirectReceiver != null) mWiFiDirectReceiver.disconnect();
    }

    private void registerWiFiDirect() {
        mWiFiDirectReceiver = new WiFiDirectReceiver(mContext, new WiFiP2pConnectListener() {
            @Override
            public void discover() {
                ToastUtil.showToast("搜索设备");
            }

            @Override
            public void requestPeer(WifiP2pDeviceList peers) {

            }

            @Override
            public void connected() {
                ToastUtil.showToast("WIFI DIRECT 连接成功");
                unregisterWiFiDirect();
                registerWiFiDisplay();
            }
        });
        IntentFilter directFilter = new IntentFilter();
        directFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        directFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        directFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        directFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        directFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        if (mContext != null) {
            mContext.registerReceiver(mWiFiDirectReceiver, directFilter);
            isRegisterWiFiDirect = true;
        }
    }

    private void unregisterWiFiDirect() {
        if (mContext != null && mWiFiDirectReceiver != null && isRegisterWiFiDirect) {
            mContext.unregisterReceiver(mWiFiDirectReceiver);
            isRegisterWiFiDirect = false;
        }
    }

    private void registerWiFiDisplay() {
        ToastUtil.showToast("registerWiFiDisplay");
        mWiFiDisplayReceive = new WiFiDisplayReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WIFI_DISPLAY_STATUS_CHANGED);
        if (mContext != null) {
            mContext.registerReceiver(mWiFiDisplayReceive, filter);
            isRegisterWiFiDisplay = true;
        }

        DisplayManagerGlobal.getInstance().registerDisplayListener(mDisplayListener, new Handler());
        DisplayManagerGlobal.getInstance().disconnectWifiDisplay();
        DisplayManagerGlobal.getInstance().startWifiDisplayScan();
        isRegisterDisplayListener = true;
    }

    private void unregisterWiFiDisplay() {
        if (mContext != null && mWiFiDisplayReceive != null && isRegisterWiFiDisplay) {
            mContext.unregisterReceiver(mWiFiDisplayReceive);
            isRegisterWiFiDisplay = false;
        }
    }

    private DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
            ToastUtil.showToast(DisplayManagerGlobal.getInstance().getRealDisplay(displayId).getName()+"设备连接成功");
            unregisterWiFiDisplay();
        }

        @Override
        public void onDisplayRemoved(int displayId) {

        }

        @Override
        public void onDisplayChanged(int displayId) {

        }
    };
}
