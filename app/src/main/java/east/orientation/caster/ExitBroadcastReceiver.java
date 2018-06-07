package east.orientation.caster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ljq on 2018/6/6.
 */

public class ExitBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        CastApplication.getAppContext().AppExit();
    }

}
