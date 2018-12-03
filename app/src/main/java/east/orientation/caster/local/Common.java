package east.orientation.caster.local;

import android.os.Environment;

import java.io.File;

/**
 * Created by ljq on 2018/3/8.
 */

public class Common {
    public static final String APP_KEY = "Caster";
    // TODO TEST
    public static int count = 0;
    /**********************************************************/
    /**手写板相关常量**/
    /**********************************************************/
    public static final String SAVE_DIR_NAME = "Caster";
    public static final String DEFAULT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + Common.SAVE_DIR_NAME;


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
    public static final int FLAG_MP3_PARAM_REQUEST = 0x2007;// 大屏mp3参数请求
    public static final int FLAG_MP3_PARAM_RESPONSE = 0x2008;// mp3参数设置
    public static final int FLAG_SCREEN_CUT_SIZE_REQUEST = 0x2009;//请求大屏分辨率
    public static final int FLAG_SCREEN_LARGE_SIZE_RESPONSE = 0x2010;//大屏回复  携带分辨率
    public static final int FLAG_SCREEN_CUT_SIZE_RESPONSE = 0x2011;//回复大屏  Rect坐标
    public static final int FLAG_SCREEN_ROTATION = 0x2012;//屏幕横竖参数
    public static final int FLAG_SCREEN_CAST_MODEL = 0x2014;//投屏模式 0，普通模式 1， 放大模式

    public static final int FLAG_SCREEN_ROTATION_1 = 1;
    public static final int FLAG_SCREEN_ROTATION_0 = 0;

    /**********************************************************/
    /**投屏相关常量**/
    /**********************************************************/

    public static final String NOTIFICATION_CHANNEL_ID = "com.lijinqi.castscreenservice.NOTIFICATION_CHANNEL_01";
    public static final int NOTIFICATION_START_STREAMING = 10;
    public static final int NOTIFICATION_STOP_STREAMING = 11;

    public static final String EXTRA_DATA = "EXTRA_DATA";

    public static final String SERVICE_MESSAGE_PREPARE_STREAMING = "SERVICE_MESSAGE_PREPARE_STREAMING";

    public static final String ACTION_START_STREAM = "ACTION_START_STREAM";
    public static final String ACTION_STOP_STREAM = "ACTION_STOP_STREAM";
    public static final String ACTION_EXIT = "ACTION_EXIT";

    public static final int CAST_MODE_MIRACAST = 0;
    public static final int CAST_MODE_WIFI = 1;

    /**********************************************************/
    /**SharePreference存储相关常量**/
    /**********************************************************/

    // 投屏相关
    public static final String KEY_NAME = "key_name";
    public static final String KEY_SIZE = "key_size";
    public static final String KEY_BITRATE = "key_bitrate";
    public static final String KEY_FPS = "key_fps";
    public static final String KEY_CAST_MODE = "key_cast_mode";



}
