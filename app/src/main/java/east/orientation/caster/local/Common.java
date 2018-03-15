package east.orientation.caster.local;

/**
 * Created by ljq on 2018/3/8.
 */

public class Common  {
    /**********************************************************/
    /**手写板相关常量**/
    /**********************************************************/
    public static String SAVE_DIR_NAME = "Caster";


    /**********************************************************/
    /**接口格式相关常量**/
    /**********************************************************/
    public static String HEAD = "Ornt";// 包头
    public static String TAIL = "Tech";// 包尾

    public static int FLAG_LOGIN = 0x1001;// 登录
    public static int FLAG_LOGOUT = 0x1002;// 登出
    public static int FLAG_HEART = 0x1003;// 心跳
    public static int FLAG_HEART_RESPONSE = 0x1004;// 心跳回执
    public static int FLAG_SET_CLASS_PWD = 0x1005;// 设置听课密码

    public static int FLAG_RESET = 0x2001;// 恢复初始状态
    public static int FLAG_THEME = 0x2002;// 设置主题信息
    public static int FLAG_START_CAST = 0x2003;// 开启投屏
    public static int FLAG_STOP_CAST = 0x2004;// 关闭投屏
    public static int FLAG_CAST_STREAM = 0x2005;// 帧数据流

    /**********************************************************/
    /**录屏相关常量**/
    /**********************************************************/
    public static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    public static final int VIEWER_PORT = 28888;
    public static final int DEFAULT_SCREEN_WIDTH = 960;
    public static final int DEFAULT_SCREEN_HEIGHT = 640;
    public static final int DEFAULT_SCREEN_DPI = 160;
    public static final int DEFAULT_VIDEO_BITRATE = 800*1024;
    public static final int DEFAULT_VIDEO_FPS = 30;
    public static final int DEFAULT_I_FRAME_INTERVAL = 1;//1 seconds between I-frames
    public static final long DEFAULT_REPEAT_PREVIOUS_FRAME_AFTER = 1000000 / 15;

    public static final String NOTIFICATION_CHANNEL_ID = "com.lijinqi.castscreenservice.NOTIFICATION_CHANNEL_01";
    public static final int NOTIFICATION_START_STREAMING = 10;
    public static final int NOTIFICATION_STOP_STREAMING = 11;

    public static final String EXTRA_DATA = "EXTRA_DATA";

    public static final String SERVICE_MESSAGE_PREPARE_STREAMING = "SERVICE_MESSAGE_PREPARE_STREAMING";

    public static final String ACTION_START_STREAM = "ACTION_START_STREAM";
    public static final String ACTION_STOP_STREAM = "ACTION_STOP_STREAM";
    public static final String ACTION_EXIT = "ACTION_EXIT";

    // 分辨率
    public static final int[][] RESOLUTION_OPTIONS = {
            {1920,1280, 960, 640},
            {1080,720, 540, 360}
    };

    // 比特率
    public static final int[] BITRATE_OPTIONS = {
            1024000, // 1 Mbps
            2048000, // 2 Mbps
            4096000, // 4 Mbps
            6144000  // 6 Mbps
    };

    // fps  frame-rate
    public static final int[] FPS_OPTIONS = {
            15,
            30,
            60
    };


    /**********************************************************/
    /**SharePreference存储相关常量**/
    /**********************************************************/
    public static final String KEY_NAME = "key_name";
    public static final String KEY_SIZE = "key_size";
    public static final String KEY_BITRATE = "key_bitrate";
    public static final String KEY_FPS = "key_fps";
}
