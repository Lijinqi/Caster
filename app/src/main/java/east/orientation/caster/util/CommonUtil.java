package east.orientation.caster.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;

import java.util.Arrays;

import east.orientation.caster.CastApplication;

/**
 * Created by ljq on 2018/5/2.
 */

public class CommonUtil {

    /**
     * 是否关键帧 h264
     * @param data
     * @return
     */
    public static boolean isIFrame(byte[] data) {
        if( data == null || data.length < 5) {
            return false;
        }
        if (data[0] == 0x0
                && data[1] == 0x0
                && data[2] == 0x0
                && data[3] == 0x1
                && data[4] == 0x67) {
            Log.d("IFrame", "check I frame data: " + Arrays.toString(Arrays.copyOf(data, 5)));
            return true;
        }
        byte nalu = data[4];
        return ((nalu & 0x1F) == 5);
    }

    /**
     * check if network avalable
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }

        return false;
    }

    public static float dp2px(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, CastApplication.getAppContext().getResources().getDisplayMetrics());
    }
}
