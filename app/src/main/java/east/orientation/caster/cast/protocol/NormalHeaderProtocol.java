package east.orientation.caster.cast.protocol;

import com.xuhao.android.libsocket.sdk.protocol.IHeaderProtocol;
import com.xuhao.android.libsocket.utils.BytesUtils;

import java.nio.ByteOrder;

/**
 * Created by ljq on 2018/3/9.
 *
 * 自定义包头 len(4字节 ) + Ornt(4字节) + flag(4字节)
 */

public class NormalHeaderProtocol implements IHeaderProtocol {

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
            return BytesUtils.bytesToInt2(header, 0) -8;//获取总长度后减去包头(Ornt)和flag长度
        } else {
            return BytesUtils.bytesToInt(header, 0) -8;//获取总长度后减去包头(Ornt)和flag长度
        }
    }
}
