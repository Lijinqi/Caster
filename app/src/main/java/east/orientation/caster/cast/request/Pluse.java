package east.orientation.caster.cast.request;


import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;

import east.orientation.caster.local.Common;
import east.orientation.caster.util.BytesUtils;

/**
 * Created by ljq on 2018/3/16.
 * <p>
 * 心跳
 * <p>
 * data:心跳次数
 */

public class Pluse extends BaseRequest implements IPulseSendable {
    private int count;// 心跳次数

    public Pluse(int count) {
        this.count = count;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_HEART_BEAT;
    }

    @Override
    public byte[] getData() {
        return BytesUtils.intToBytes(count);
    }
}
