package east.orientation.caster.cast.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/12.
 * <p>
 * 投屏图像帧数据
 * <p>
 * data:录屏流中的某帧数据
 */

public class VideoCastRequest extends BaseRequest {

    private byte[] frame;

    public VideoCastRequest(byte[] frame) {
        this.frame = frame;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_VIDEO_STREAM;
    }

    @Override
    public byte[] getData() {
        if (frame != null)
            return frame;
        return new byte[0];
    }
}
