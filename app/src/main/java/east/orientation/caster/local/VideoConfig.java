package east.orientation.caster.local;

/**
 * Created by ljq on 2018/3/28.
 *
 * video codec 相关配置常量
 */

public class VideoConfig {
    public static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    public static final int VIEWER_PORT = 28888;
    public static final int DEFAULT_SCREEN_WIDTH = 960;
    public static final int DEFAULT_SCREEN_HEIGHT = 540;
    public static final int DEFAULT_SCREEN_DPI = 1;
    public static final int DEFAULT_VIDEO_BITRATE = 800*1024;
    public static final int DEFAULT_VIDEO_FPS = 30;
    public static final int DEFAULT_I_FRAME_INTERVAL = 1;//1 seconds between I-frames
    public static final long DEFAULT_REPEAT_PREVIOUS_FRAME_AFTER = 1000000 / 15;
    // 分辨率
    public static final int[][] RESOLUTION_OPTIONS = {
            {1920,1280, 960, 640},
            {1080,720, 540, 360}
    };

    // 比特率
    public static final int[] BITRATE_OPTIONS = {
            600*1000, // 600k bps
            800*1000, // 800k bps
            1024*1000, // 1M bps
            2048*1000  // 2M bps
    };

    // fps  frame-rate
    public static final int[] FPS_OPTIONS = {
            15,
            30,
            60
    };
}
