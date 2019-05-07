package east.orientation.caster.cast.request;

import east.orientation.caster.local.Common;

/**
 * @author ljq
 * @date 2019/1/11
 * @description
 */

public class PcScreenEventResponse extends BaseRequest {

    private int type;//类型 0x200 移动位置，0x201 点下，0x202 抬起
    private int x;// x轴
    private int y;// y轴
    private int mouseType;// 1左按键 ， 2 右按键

    @Override
    public int getFlag() {
        return Common.FLAG_PC_SCREEN_MOVE_EVENT;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
