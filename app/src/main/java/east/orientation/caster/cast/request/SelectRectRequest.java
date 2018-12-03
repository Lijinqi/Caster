package east.orientation.caster.cast.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/4/16.
 * <p>
 * 登录后 请求大屏分辨率
 */

public class SelectRectRequest extends BaseRequest {

    @Override
    public int getFlag() {
        return Common.FLAG_SCREEN_CUT_SIZE_REQUEST;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
