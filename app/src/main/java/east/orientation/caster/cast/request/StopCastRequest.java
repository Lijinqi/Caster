package east.orientation.caster.cast.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/12.
 * <p>
 * 停止投屏 无携带数据
 * <p>
 * data:null
 */

public class StopCastRequest extends BaseRequest {
    @Override
    public int getFlag() {
        return Common.FLAG_STOP_CAST;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
