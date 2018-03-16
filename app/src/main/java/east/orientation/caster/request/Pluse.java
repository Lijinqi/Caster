package east.orientation.caster.request;

import com.xuhao.android.libsocket.sdk.bean.IPulseSendable;
import com.xuhao.android.libsocket.utils.BytesUtils;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/16.
 *
 * 心跳
 *
 * data:心跳次数
 */

public class Pluse extends BaseRequest implements IPulseSendable {
    private int count;// 心跳次数

    public Pluse(int count){
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
