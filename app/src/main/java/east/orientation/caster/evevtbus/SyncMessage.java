package east.orientation.caster.evevtbus;

/**
 * Created by ljq on 2018/6/5.
 */

public class SyncMessage {
    private String action;
    private Object object;
    public SyncMessage(String action,Object object){
        this.action = action;
        this.object = object;
    }

    public String getAction() {
        return action;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
