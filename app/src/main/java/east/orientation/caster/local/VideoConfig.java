package east.orientation.caster.local;

/**
 * Created by ljq on 2018/3/28.
 * <p>
 * video codec 相关配置常量
 */

public class VideoConfig {
    public static final int REQUEST_CODE_SCREEN_CAPTURE = 0x10;
    public static final int DEFAULT_SCREEN_DPI = 160;
    public static final int DEFAULT_I_FRAME_INTERVAL = 10;//1 seconds between I-frames

    // 屏幕宽高比
    public static double px = 1920.0 / 1200.0;
    // 分辨率
    public static final int[][] RESOLUTION_OPTIONS = {

            {1200, 720, 480, 360},
            {1920, (int) (720.0 * px), (int) (480.0 * px), (int) (360.0 * px)}
    };

    // 比特率
    public static final int[] BITRATE_OPTIONS = {
            6144 * 1000, // 6M bps
            4096 * 1000, // 4M bps
            2048 * 1000, // 2M bps
            1024 * 1000, // 1M bps
    };

    // fps  frame-rate
    public static final int[] FPS_OPTIONS = {
            10,
            15,
            30,
            60
    };
}
