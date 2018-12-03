package east.orientation.caster.cast.request;


import java.nio.ByteBuffer;

import east.orientation.caster.local.Common;
import east.orientation.caster.util.BytesUtils;

/**
 * Created by ljq on 2018/4/16.
 * <p>
 * 告知大屏选中区域
 */

public class SelectRectResponse extends BaseRequest {
    int left, top, right, bottom;

    public SelectRectResponse(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_SCREEN_CUT_SIZE_RESPONSE;
    }

    @Override
    public byte[] getData() {
        ByteBuffer bb = ByteBuffer.allocate(16);
        // 左
        bb.put(BytesUtils.intToBytes(left));
        // 上
        bb.put(BytesUtils.intToBytes(top));
        // 右
        bb.put(BytesUtils.intToBytes(right));
        // 下
        bb.put(BytesUtils.intToBytes(bottom));
        return bb.array();
    }
}
