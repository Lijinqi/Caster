package east.orientation.caster.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/12.
 *
 * 恢复初始状态
 *
 * data:null
 */

public class RestRequest extends BaseRequest {

    @Override
    public int getFlag() {
        return Common.FLAG_RESET;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
