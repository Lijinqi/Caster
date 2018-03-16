package east.orientation.caster.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/16.
 *
 * 登出
 *
 * data:无携带数据
 */

public class LogoutRequest extends BaseRequest {
    @Override
    public int getFlag() {
        return Common.FLAG_LOGOUT;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
