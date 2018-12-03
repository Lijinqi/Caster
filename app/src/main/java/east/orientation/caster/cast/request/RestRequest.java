package east.orientation.caster.cast.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/12.
 * <p>
 * 恢复初始状态
 * <p>
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
