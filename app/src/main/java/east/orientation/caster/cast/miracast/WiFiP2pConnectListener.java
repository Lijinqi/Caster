package east.orientation.caster.cast.miracast;

import android.net.wifi.p2p.WifiP2pDeviceList;

/**
 * Created by ljq on 2018/12/5.
 */

public interface WiFiP2pConnectListener {
    void discover();
    void requestPeer(WifiP2pDeviceList peers);
    void connected();
}
