package east.orientation.caster.cast.request;


import east.orientation.caster.local.Common;
import east.orientation.caster.util.BytesUtils;

/**
 * Created by ljq on 2018/4/3.
 * <p>
 * 回复服务器MP3参数请求
 */

public class Mp3ParamsResponse extends BaseRequest {
    private int channelCount;
    private int simpleFmt;
    private int simpleRate;

    public Mp3ParamsResponse(int channelCount, int simpleRate, int simpleFmt) {
        this.channelCount = channelCount;
        this.simpleRate = simpleRate;
        this.simpleFmt = simpleFmt;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_MP3_PARAM_RESPONSE;
    }

    @Override
    public byte[] getData() {

        byte[] cCount = BytesUtils.intToBytes(channelCount);
        byte[] sFmt = BytesUtils.intToBytes(simpleFmt);
        byte[] sRate = BytesUtils.intToBytes(simpleRate);

        byte[] data = new byte[16];
        System.arraycopy(cCount, 0, data, 0, cCount.length);
        System.arraycopy(sFmt, 0, data, 4 + cCount.length, sFmt.length);
        System.arraycopy(sRate, 0, data, 4 + cCount.length + sRate.length, sRate.length);

        return data;
    }
}
