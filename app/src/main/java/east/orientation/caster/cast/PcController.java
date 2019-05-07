package east.orientation.caster.cast;

import android.app.Instrumentation;
import android.graphics.Point;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.concurrent.LinkedBlockingQueue;

import east.orientation.caster.local.Common;
import east.orientation.caster.local.VideoConfig;
import east.orientation.caster.scrcpy.wrappers.InputManager;
import east.orientation.caster.scrcpy.wrappers.ServiceManager;
import east.orientation.caster.util.BytesUtils;
import east.orientation.caster.view.WindowFloatManager;

/**
 * @author ljq
 * @date 2019/1/15
 * @description
 */

public class PcController {
    private static final String TAG = "PcController";

    private static LinkedBlockingQueue<byte[]> sQueue = new LinkedBlockingQueue<>();
    private static EventThread sEventThread;
    private static Handler sEventHandler;

    private static volatile boolean isRunning;

    private static InputManager mInputManager;
    private static final ServiceManager serviceManager = new ServiceManager();
    private static int sLastX;
    private static int sLastY;

    private static int scrollHeight;
    private static int screenX;
    private static int screenY;
    private static long currentTime;
    private static long lastMouseDown;

    private static Handler.Callback sCallback = msg -> {
        if (msg.what == Common.FLAG_PC_SCREEN_MOVE_EVENT)
            //onPcScreenEvent((byte[]) msg.obj,msg.arg1);
            onScreenEvent((byte[]) msg.obj,msg.arg1);

        return false;
    };

    private static class EventThread extends HandlerThread {
        public EventThread(String name) {
            super(name);
        }

        public EventThread(String name, int priority) {
            super(name, priority);
        }
    }

    private static void onScreenEvent(byte[] body,int index) {
        int mouseType = BytesUtils.bytesToInt(body, 12);
        if (mouseType != Common.TYPE_MOUSE_LEFT  && mouseType != Common.TYPE_MOUSE_RIGHT) return;

        int msgType = BytesUtils.bytesToInt(body, 0);
        int x = BytesUtils.bytesToInt(body, 4);
        int y = BytesUtils.bytesToInt(body, 8);
        if (x == sLastX && y == sLastY) {
            x++;
            y++;
        }
        sLastX = x;
        sLastY = y;

        long now = SystemClock.uptimeMillis();
        switch (msgType) {
            case Common.TYPE_PC_MSG_DOWN:
                int finalNewX = x;
                int finalNewY = y;

                lastMouseDown = now;
                MotionEvent eventDown = MotionEvent.obtain(lastMouseDown,
                        now, MotionEvent.ACTION_DOWN, finalNewX, finalNewY,0);
                eventDown.setSource(InputDevice.SOURCE_TOUCHSCREEN);

                mInputManager.injectInputEvent(eventDown,InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
                break;
            case Common.TYPE_PC_MSG_MOVE:
                int finalNewX1 = x;
                int finalNewY1 = y;

                MotionEvent eventMove = MotionEvent.obtain(lastMouseDown,
                        now, MotionEvent.ACTION_MOVE, finalNewX1, finalNewY1,0);
                eventMove.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                mInputManager.injectInputEvent(eventMove,InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
                break;
            case Common.TYPE_PC_MSG_UP:
                int finalNewX2 = x;
                int finalNewY2 = y;
                MotionEvent eventUp = MotionEvent.obtain(lastMouseDown,
                        now, MotionEvent.ACTION_UP, finalNewX2, finalNewY2,0);
                eventUp.setSource(InputDevice.SOURCE_TOUCHSCREEN);

                mInputManager.injectInputEvent(eventUp,InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
                break;
        }
    }

    private static void onPcScreenEvent(byte[] body,int index) {
        int mouseType = BytesUtils.bytesToInt(body, 12);
        if (mouseType != Common.TYPE_MOUSE_LEFT  && mouseType != Common.TYPE_MOUSE_RIGHT) return;

        int msgType = BytesUtils.bytesToInt(body, 0);
        int x = BytesUtils.bytesToInt(body, 4);
        int y = BytesUtils.bytesToInt(body, 8);
        long now = SystemClock.uptimeMillis();
        int newX = 0,newY = 0;

        int large_width = WindowFloatManager.getInstance().getLargeWidth();
        int large_height = WindowFloatManager.getInstance().getLargeHeight();
        double px = VideoConfig.px;// 平板H/W

        int realWidth = (int) (large_height / px);
        int leftX = (large_width - realWidth) / 2;
        int rightX = (large_width + realWidth) / 2;

        int realHeight = (int) (large_width * px);

        if (WindowFloatManager.getInstance().isROTATION_0()) {
            // 竖屏
            if (WindowFloatManager.getInstance().getCastModel() == WindowFloatManager.CastModel.Normal) {
                // 正常模式
//                int realWidth = (int) (large_height / px);
//                int leftX = (large_width - realWidth) / 2;
//                int rightX = (large_width + realWidth) / 2;
                if (x < leftX || x > rightX) {
                    return;
                } else {
                    // 正常模式
                    newX = (x - leftX) * 1200 / realWidth;
                    newY = y * 1920 / large_height;
                }
            } else {
                // 放大模式
                if (!(Math.abs(screenX - x) <= 5 && Math.abs(screenY - y) <= 5)) {
                    if (SystemClock.uptimeMillis() - currentTime > 200 )
                        scrollHeight = WindowFloatManager.getInstance().getScrollHeight();
                }
                currentTime = SystemClock.uptimeMillis();
                screenX = x;
                screenY = y;

                newX = x * 1200 / large_width;
                newY = y * 1920 / realHeight + scrollHeight;
            }
        } else {
            // 横屏
            newX = x;
            newY = y * 1200 / large_height;

        }
        switch (msgType) {
            case Common.TYPE_PC_MSG_DOWN:
                int finalNewX = x;
                int finalNewY = y;

                lastMouseDown = now;
                MotionEvent eventDown = MotionEvent.obtain(lastMouseDown,
                        now, MotionEvent.ACTION_DOWN, finalNewX, finalNewY,0);
                eventDown.setSource(InputDevice.SOURCE_TOUCHSCREEN);

                mInputManager.injectInputEvent(eventDown,InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
                break;
            case Common.TYPE_PC_MSG_MOVE:
                int finalNewX1 = x;
                int finalNewY1 = y;

                MotionEvent eventMove = MotionEvent.obtain(lastMouseDown,
                        now, MotionEvent.ACTION_MOVE, finalNewX1, finalNewY1,0);
                eventMove.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                mInputManager.injectInputEvent(eventMove,InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
                break;
            case Common.TYPE_PC_MSG_UP:
                int finalNewX2 = x;
                int finalNewY2 = y;
                MotionEvent eventUp = MotionEvent.obtain(lastMouseDown,
                        now, MotionEvent.ACTION_UP, finalNewX2, finalNewY2,0);
                eventUp.setSource(InputDevice.SOURCE_TOUCHSCREEN);

                mInputManager.injectInputEvent(eventUp,InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
                break;
        }
    }

    static int count = 1;
    public static void offer(byte[] bytes) {

        if (isRunning) {
            //Log.e("Duration Start:","count : "+count+" Time: "+SystemClock.uptimeMillis());
            sEventHandler.obtainMessage(Common.FLAG_PC_SCREEN_MOVE_EVENT,count++,0,bytes).sendToTarget();
        }
    }

    public static boolean isIsRunning() {
        return isRunning;
    }

    public static void start() {
        synchronized (PcController.class) {
            sEventThread = new EventThread(PcController.class.getSimpleName(),Process.THREAD_PRIORITY_MORE_FAVORABLE);
            sEventThread.start();
            sEventHandler = new Handler(sEventThread.getLooper(),sCallback);

            isRunning = true;

            mInputManager = serviceManager.getInputManager();
        }
    }

    public static void stop() {
        synchronized (PcController.class) {
            isRunning = false;
            if (sEventThread != null)
                sEventThread.interrupt();
            if (sEventHandler != null)
                sEventHandler.removeCallbacksAndMessages(null);
        }
    }
}
