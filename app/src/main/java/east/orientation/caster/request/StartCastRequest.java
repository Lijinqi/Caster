package east.orientation.caster.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/12.
 *
 * 启动投屏
 *
 * data:null
 */

public class StartCastRequest extends BaseRequest {


    @Override
    public int getFlag() {
        return Common.FLAG_START_CAST;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
