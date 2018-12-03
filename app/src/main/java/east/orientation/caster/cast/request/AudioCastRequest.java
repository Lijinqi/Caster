package east.orientation.caster.cast.request;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/29.
 */

public class AudioCastRequest extends BaseRequest {
    private byte[] frame;

    public AudioCastRequest(byte[] frame) {
        this.frame = frame;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_AUDIO_STREAM;
    }

    @Override
    public byte[] getData() {
        if (frame != null)
            return frame;
        return new byte[0];
    }
}
