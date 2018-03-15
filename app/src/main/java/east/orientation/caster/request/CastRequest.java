package east.orientation.caster.request;

import android.util.Log;

import com.xuhao.android.libsocket.utils.BytesUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/3/12.
 *
 * 投屏图像帧数据
 *
 * data:录屏流中的某帧数据
 */

public class CastRequest extends BaseRequest {

    private byte[] frame ;

    public CastRequest(byte[] frame){
        this.frame = frame;
    }

    @Override
    public int getFlag() {
        return Common.FLAG_CAST_STREAM;
    }

    @Override
    public byte[] getData() {
        if (frame != null)
            return frame;
        return new byte[0];
    }
}
