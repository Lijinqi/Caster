package east.orientation.caster.extract;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by ljq on 2018/6/1.
 */

public abstract class BaseExtract {
    public Handler mHandler = new Handler(Looper.getMainLooper());

    public abstract void onExtract(String srcPath, String extractPath,String password,IExtractListener listener);
}
