package east.orientation.caster.evevtbus;

/**
 * Created by ljq on 2018/1/9.
 */

public class CastMessage {
    public static final String MESSAGE_ACTION_STREAMING_TRY_START = "MESSAGE_ACTION_STREAMING_TRY_START";
    public static final String MESSAGE_ACTION_STREAMING_START = "MESSAGE_ACTION_STREAMING_START";
    public static final String MESSAGE_ACTION_STREAMING_STOP = "MESSAGE_ACTION_STREAMING_STOP";
    public static final String MESSAGE_ACTION_TCP_RESTART = "MESSAGE_ACTION_TCP_RESTART";

    public static final String MESSAGE_STATUS_TCP_OK = "MESSAGE_STATUS_TCP_OK";
    public static final String MESSAGE_STATUS_TCP_ERROR_NO_IP = "MESSAGE_STATUS_TCP_ERROR_NO_IP";
    public static final String MESSAGE_STATUS_TCP_ERROR_PORT_IN_USE = "MESSAGE_STATUS_TCP_ERROR_PORT_IN_USE";
    public static final String MESSAGE_STATUS_TCP_ERROR_UNKNOWN = "MESSAGE_STATUS_TCP_ERROR_UNKNOWN";

    public static final String MESSAGE_STATUS_CAST_GENERATOR_ERROR = "MESSAGE_STATUS_CAST_GENERATOR_ERROR";

    private final String message;

    public CastMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
