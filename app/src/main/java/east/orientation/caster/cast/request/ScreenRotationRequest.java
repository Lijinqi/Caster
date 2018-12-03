package east.orientation.caster.cast.request;


import east.orientation.caster.local.Common;
import east.orientation.caster.util.BytesUtils;

/**
 * Created by ljq on 2018/7/17.
 * <p>
 * 屏幕横竖参数
 */

public class ScreenRotationRequest extends BaseRequest {
    private int rotation;// 0：横屏，1：竖屏

    public ScreenRotationRequest(int rotation) {
        this.rotation = rotation;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_SCREEN_ROTATION;
    }

    @Override
    public byte[] getData() {
        return BytesUtils.intToBytes(rotation);
    }
}
