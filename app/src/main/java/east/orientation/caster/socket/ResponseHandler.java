package east.orientation.caster.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import east.orientation.caster.local.Common;


/**
 * Created by ljq on 2018/8/31.
 */

public class ResponseHandler extends Handler {
    private static final String TAG = "ResponseHandler";
    public static final int SUCCESS_RESPONSE = 1;

    public ResponseHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        byte[] bytes = (byte[]) msg.obj;
        try {
            if (Common.HEAD.equals(new String(bytes, 0, 4, "gbk"))) {
                String response;
                byte zero = 0;
                if (zero == bytes[bytes.length - 1]) {
                    response = new String(bytes, 0, bytes.length - 1, "gbk");
                } else {
                    response = new String(bytes, 0, bytes.length, "gbk");
                }
                String cmd = response.substring(response.indexOf("=") + 1, response.indexOf(","));
                int index = response.indexOf("=", response.indexOf("=") + 1) + 1;
                String isOk = response.substring(index, index + 1);

                Log.d(TAG, cmd + " - response :" + response);

                if (UpdateManager.CMD_QUERY_UPDATE.equals(cmd)) {

                    UpdateManager.handleUpdate("1".equals(isOk), response);
                } else if (UpdateManager.CMD_DOWNLOAD_UPDATE.equals(cmd)) {

                    UpdateManager.handleDownload("1".equals(isOk), response, bytes);
                }
            }
        } catch (UnsupportedEncodingException e) {

        }
    }
}
