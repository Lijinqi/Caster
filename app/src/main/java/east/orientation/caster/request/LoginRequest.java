package east.orientation.caster.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/15.
 *
 * 登录
 *
 * data：携带登陆类型
 */

public class LoginRequest extends BaseRequest {
    private char type;//登录类型

    public LoginRequest(char type){
        this.type = type;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_LOGIN;
    }

    @Override
    public byte[] getData() {
        String cmd = type+"abc\0\0";

        return cmd.getBytes();
    }
}
