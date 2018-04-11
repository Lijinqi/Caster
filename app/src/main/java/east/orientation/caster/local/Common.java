package east.orientation.caster.local;

/**
 * Created by ljq on 2018/3/8.
 */

public class Common  {
    /**********************************************************/
    /**手写板相关常量**/
    /**********************************************************/
    public static final String SAVE_DIR_NAME = "Caster";


    /**********************************************************/
    /**接口格式相关常量**/
    /**********************************************************/
    public static final String HEAD = "Ornt";// 包头
    public static final String TAIL = "Tech";// 包尾

    // 登录类型
    public static final char LOGIN_TYPE_TEACHER = 'T';// 老师
    public static final char LOGIN_TYPE_ADMIN = 'A';// 管理员
    public static final char LOGIN_TYPE_STUDENT = 'S';// 学生

    public static final int FLAG_LOGIN = 0x1001;// 登录
    public static final int FLAG_LOGIN_RESPONSE = 0x1002;//登录回执
    public static final int FLAG_LOGOUT = 0x1003;// 登出
    public static final int FLAG_HEART_BEAT = 0x1004;// 心跳
    public static final int FLAG_HEART_BEAT_RESPONSE = 0x1005;// 心跳回执

    public static final int FLAG_RESET = 0x2001;// 恢复初始状态
    public static final int FLAG_START_CAST = 0x2003;// 开启投屏
    public static final int FLAG_STOP_CAST = 0x2004;// 关闭投屏
    public static final int FLAG_VIDEO_STREAM = 0x2005;// 视频帧数据流
    public static final int FLAG_AUDIO_STREAM = 0x2006;// 音频数据流
    public static final int FLAG_MP3_PARAM_REQUEST = 0x2007;// mp3参数请求
    public static final int FLAG_MP3_PARAM_RESPONSE = 0x2008;// mp3参数设置

    /**********************************************************/
    /**投屏相关常量**/
    /**********************************************************/

    public static final int DEFAULT_PORT = 28888;
    public static final String NOTIFICATION_CHANNEL_ID = "com.lijinqi.castscreenservice.NOTIFICATION_CHANNEL_01";
    public static final int NOTIFICATION_START_STREAMING = 10;
    public static final int NOTIFICATION_STOP_STREAMING = 11;

    public static final String EXTRA_DATA = "EXTRA_DATA";

    public static final String SERVICE_MESSAGE_PREPARE_STREAMING = "SERVICE_MESSAGE_PREPARE_STREAMING";

    public static final String ACTION_START_STREAM = "ACTION_START_STREAM";
    public static final String ACTION_STOP_STREAM = "ACTION_STOP_STREAM";
    public static final String ACTION_EXIT = "ACTION_EXIT";




    /**********************************************************/
    /**SharePreference存储相关常量**/
    /**********************************************************/

    // 投屏相关
    public static final String KEY_NAME = "key_name";
    public static final String KEY_SIZE = "key_size";
    public static final String KEY_BITRATE = "key_bitrate";
    public static final String KEY_FPS = "key_fps";
}
