package east.orientation.caster.socket.request;


import com.xuhao.didi.core.iocore.interfaces.ISendable;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import east.orientation.caster.util.BytesUtils;


/**
 * Created by ljq on 2018/5/2.
 */

public abstract class StringRequest implements ISendable {

    public abstract String getContent();

    @Override
    public byte[] parse() {
        byte[] body = new byte[0];
        String content = getContent();

        try {
            body = content.getBytes("gbk");
            ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
            bb.order(ByteOrder.BIG_ENDIAN);
            bb.put(BytesUtils.intToBytes(body.length));
            bb.put(body);
            return bb.array();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
