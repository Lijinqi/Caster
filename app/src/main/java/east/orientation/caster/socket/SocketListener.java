package east.orientation.caster.socket;

/**
 * Created by ljq on 2018/8/28.
 */

public interface SocketListener {
    void connected();

    void connectFailed();

    void disconnected();
}
