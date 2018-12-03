package east.orientation.caster.socket.request;


/**
 * Created by ljq on 2018/11/5.
 */

public class AppsQueryRequest extends StringRequest {
    private String type;
    private String appKey;

    public AppsQueryRequest(String type, String appKey) {
        this.type = type;
        this.appKey = appKey;
    }

    @Override
    public String getContent() {
        return String.format("Orntcmd=filequery_type,data=%s,%s", type, appKey);
    }
}
