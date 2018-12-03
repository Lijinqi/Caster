package east.orientation.caster.cast.request;


import east.orientation.caster.local.Common;
import east.orientation.caster.util.BytesUtils;

/**
 * Created by ljq on 2018/10/8.
 */

public class ChageModelRequest extends BaseRequest {
    private int model;//0, Normal 1,Large

    public ChageModelRequest(int model) {
        this.model = model;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_SCREEN_CAST_MODEL;
    }

    @Override
    public byte[] getData() {
        return BytesUtils.intToBytes(model);
    }
}
