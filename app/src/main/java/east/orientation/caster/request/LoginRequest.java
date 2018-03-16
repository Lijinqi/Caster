package east.orientation.caster.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/15.
 */

public class LoginRequest extends BaseRequest {
    @Override
    public int getFlag() {
        return Common.FLAG_LOGIN;
    }

    @Override
    public byte[] getData() {
        String cmd = "Tabc\0\0";

        return cmd.getBytes();
    }
}
