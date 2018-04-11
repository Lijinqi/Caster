package east.orientation.caster.evevtbus;

/**
 * Created by ljq on 2018/4/11.
 */

public class ConnectMessage {
    private String ip;
    private int port;
    public ConnectMessage(String ip,int port){
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
