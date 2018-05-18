package east.orientation.caster.ui.fragment;

import com.xuhao.android.libsocket.sdk.bean.ISendable;

/**
 * Created by ljq on 2018/5/3.
 */

public class BytesRequest implements ISendable {
    private byte[] buf;
    public BytesRequest(byte[] bytes){
        buf = bytes;
    }

    @Override
    public byte[] parse() {
        if (buf != null){
            return buf;
        }
        return new byte[0];
    }
}
