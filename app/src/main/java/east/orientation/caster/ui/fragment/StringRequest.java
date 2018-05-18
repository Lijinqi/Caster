package east.orientation.caster.ui.fragment;

import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.utils.BytesUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Created by ljq on 2018/5/2.
 */

public class StringRequest implements ISendable {
    private String content = "";

    public StringRequest(String content) {
        this.content = content;
    }

    @Override
    public byte[] parse() {
        byte[] body = content.getBytes(Charset.defaultCharset());
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(BytesUtils.intToBytes(body.length));
        bb.put(body);
        return bb.array();
    }
}
