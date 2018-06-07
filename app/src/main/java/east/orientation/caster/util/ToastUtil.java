package east.orientation.caster.util;

import android.content.Context;
import android.widget.Toast;

import east.orientation.caster.CastApplication;

/**
 * Created by ljq on 2018/6/6.
 */

public class ToastUtil {

    public static final int TIME_SHORT = Toast.LENGTH_SHORT;
    public static final int TIME_LONG = Toast.LENGTH_LONG;

    private static Toast mToast;
    private static Context mContext = CastApplication.getAppContext();

    public static void showToast(int resId) {
        showToast(mContext.getResources().getString(resId));
    }

    public static void showToast(String text) {
        showToast(text, TIME_SHORT);
    }

    public static void showToast(int resId, int duration) {
        showToast(mContext.getResources().getString(resId), duration);
    }

    public static void showToast(String text, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, text, duration);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }
}
