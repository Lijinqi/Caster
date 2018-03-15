package east.orientation.caster.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/12.
 *
 * 设置主题信息
 *
 * data: 连续排列的两个字符串（0结尾），分别表示“主题”“副标题”
 */

public class SetThemeRequest extends BaseRequest {
    @Override
    public int getFlag() {
        return Common.FLAG_THEME;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
