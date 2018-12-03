package east.orientation.caster.cast.request;


import com.xuhao.didi.core.iocore.interfaces.ISendable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import east.orientation.caster.local.Common;
import east.orientation.caster.util.BytesUtils;

/**
 * Created by ljq on 2018/3/9.
 * <p>
 * 请求 基类
 * 自定义包头  len(4字节) + Ornt(4字节) + flag(4字节)
 */

public abstract class BaseRequest implements ISendable {
    protected String head = Common.HEAD;// 4bytes

    public abstract int getFlag();// 4bytes

    public abstract byte[] getData();// len bytes

    @Override
    public byte[] parse() {
        int flag = getFlag();
        byte[] data = getData();
        try {
            if (data != null) {
//                if (flag == Common.FLAG_AUDIO_STREAM){
//                    ByteBuffer bb = ByteBuffer.allocate(16 + data.length);
//                    bb.order(ByteOrder.BIG_ENDIAN);
//                    // 加入长度
//                    bb.put(BytesUtils.intToBytes(12+data.length));
//                    // 加入head
//                    bb.put(head.getBytes());
//                    // 加入flag
//                    bb.put(BytesUtils.intToBytes(flag));
//
//                    bb.put(BytesUtils.intToBytes(Common.count++));
//                    // 加入data
//                    bb.put(data);
//
//                    return bb.array();
//
//                }else {
                ByteBuffer bb = ByteBuffer.allocate(12 + data.length);
                bb.order(ByteOrder.BIG_ENDIAN);
                // 加入长度
                bb.put(BytesUtils.intToBytes(8 + data.length));
                // 加入head
                bb.put(head.getBytes());
                // 加入flag
                bb.put(BytesUtils.intToBytes(flag));
                // 加入data
                bb.put(data);

                return bb.array();
//                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
