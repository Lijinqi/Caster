package east.orientation.caster.socket.request;


/**
 * Created by ljq on 2018/11/5.
 */

public class AppFiledownRequest extends StringRequest {
    private String type;
    private String apkName;
    private long offset;
    private int length;

    public AppFiledownRequest(String type, String apkName, long offset, int length) {
        this.type = type;
        this.apkName = apkName;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public String getContent() {

        return String.format("Orntcmd=filedown_type,data=%s,%s,%s,%s", type, apkName, offset, length);
    }
}
