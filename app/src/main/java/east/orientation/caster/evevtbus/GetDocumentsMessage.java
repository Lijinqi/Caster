package east.orientation.caster.evevtbus;

import android.os.Bundle;

/**
 * Created by ljq on 2018/5/29.
 */

public class GetDocumentsMessage {
    private Bundle mBundle;

    public GetDocumentsMessage(Bundle bundle){
        mBundle = bundle;
    }

    public Bundle getBundle() {
        return mBundle;
    }
}
