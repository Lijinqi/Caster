package east.orientation.caster.cast.protocol;


import com.xuhao.didi.core.protocol.IReaderProtocol;

import java.nio.ByteOrder;

import east.orientation.caster.util.BytesUtils;

/**
 * Created by ljq on 2018/3/9.
 * <p>
 * 自定义包头 len(4字节 ) + Ornt(4字节) + flag(4字节)
 */

public class NormalProtocol implements IReaderProtocol {

    @Override
    public int getHeaderLength() {
        return 12;
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        if (header == null || header.length == 0) {
            return 0;
        }
        if (ByteOrder.BIG_ENDIAN.toString().equals(byteOrder.toString())) {
            return BytesUtils.bytesToInt2(header, 0) - 8;//获取总长度后减去包头(Ornt)和flag长度
        } else {
            return BytesUtils.bytesToInt(header, 0) - 8;//获取总长度后减去包头(Ornt)和flag长度
        }
    }
}
