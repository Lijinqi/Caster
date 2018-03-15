package east.orientation.caster.request;

import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.utils.BytesUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/9.
 *
 * 请求 基类
 * 自定义包头  len(4字节) + Ornt(4字节) + flag(4字节)
 */

public abstract class BaseRequest implements ISendable{
    protected String head = Common.HEAD;// 4bytes

    public abstract int getFlag();// 4bytes

    public abstract byte[] getData();// len bytes

    @Override
    public byte[] parse() {
        int flag = getFlag();
        byte[] data = getData();
        try {
            if (data != null) {
                ByteBuffer bb = ByteBuffer.allocate(12 + data.length);
                bb.order(ByteOrder.BIG_ENDIAN);
                // 加入长度
                bb.putInt(12+data.length);
                // 加入head
                bb.put(head.getBytes());
                // 加入flag
                bb.put(BytesUtils.intToBytes(flag));
                // 加入data
                bb.put(data);
                return bb.array();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
