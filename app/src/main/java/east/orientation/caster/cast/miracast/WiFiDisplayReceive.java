package east.orientation.caster.cast.miracast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.util.Log;

import east.orientation.caster.util.ToastUtil;

/**
 * Created by ljq on 2018/12/5.
 */

public class WiFiDisplayReceive extends BroadcastReceiver {
    private static final String TAG = "WiFiDisplayReceive";
    public static final String ACTION_WIFI_DISPLAY_STATUS_CHANGED = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";

    private WifiDisplayStatus mWifiDisplayStatus;
    private WifiDisplay mWifiDisplay;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "action: " + intent);
        if (action.equals(ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
            mWifiDisplayStatus = DisplayManagerGlobal.getInstance().getWifiDisplayStatus();
            WifiDisplay[] wifiDisplays = mWifiDisplayStatus.getDisplays();
            for (WifiDisplay display : wifiDisplays) {
                Log.d(TAG, "wifi display: " + display.toString());

                if (mWifiDisplay == null) {
                    if (display.canConnect() && display.getDeviceName().startsWith("LOLLIPOP")) {
                        Log.d(TAG, "");
                        ToastUtil.showToast("```````1");
                        mWifiDisplay = display;
                        DisplayManagerGlobal.getInstance().connectWifiDisplay(mWifiDisplay.getDeviceAddress());
                    }
                } else {
                    if (!mWifiDisplay.getDeviceAddress().equals(display.getDeviceAddress())) {
                        if (display.canConnect() && display.getDeviceName().startsWith("LOLLIPOP")) {
                            Log.d(TAG, "```````2");
                            ToastUtil.showToast("```````2");
                            mWifiDisplay = display;
                            DisplayManagerGlobal.getInstance().connectWifiDisplay(mWifiDisplay.getDeviceAddress());
                        }
                    }
                }
            }
        }
    }
}
