package east.orientation.caster.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ljq on 2018/5/29.
 */

public class SuUtil {
    private static Process process;

    public static void test(String cmd) {
        OutputStream out = process.getOutputStream();
        cmd = cmd+ " \n";
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(String cmdStr) {
        OutputStream out = process.getOutputStream();
        String cmd = cmdStr;
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化进程
     */
    public static void initProcess() {
        if (process == null)
            try {
                process = Runtime.getRuntime().exec("sh");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * 结束进程
     */
    private static void killProcess(String packageName) {
        OutputStream out = process.getOutputStream();
        String cmd = "am force-stop " + packageName + " \n";
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startIntent(String packageName) {
        OutputStream out = process.getOutputStream();
        String cmd = "am start -n " + packageName + " \n";
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     */
    public static void close() {
        if (process != null)
            try {
                process.getOutputStream().close();
                process = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
