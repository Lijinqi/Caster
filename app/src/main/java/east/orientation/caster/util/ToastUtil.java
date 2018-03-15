package east.orientation.caster.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Toast工具类（liuxia）
 * <p>
 *
 */
public class ToastUtil
{
    private static String S_OLDMESSAGE;
    private static Toast S_TOAST = null;
    private static int DURATION = Toast.LENGTH_SHORT;
    private static long FIRST_TIME = 0;
    private static long NEXT_TIME = 0;
    private static long DURATIONTIME = 0;
    private static final int LONG_DELAY = 3500; //LENGTH_LONG
    private static final int SHORT_DELAY = 2000; // LENGTH_SHORT
    private static final int POSTION = Gravity.BOTTOM;

    /**
     * 显示（长）
     *
     * @param context Context
     * @param message 信息
     */
    public static void show(Context context, String message)
    {
        show(context, message, Toast.LENGTH_LONG, 0);
    }


    /**
     * 显示（长）
     *
     * @param context Context
     * @param resId   消息资源
     */
    public static void show(Context context, @StringRes int resId)
    {
        String message = context.getResources().getString(resId);
        show(context, message, Toast.LENGTH_LONG, 0);
    }

    /**
     * 显示（短）
     *
     * @param context Context
     * @param message 消息
     */
    public static void showShort(Context context, String message)
    {
        show(context, message, 0, 0);
    }

    /**
     * 显示(短)
     *
     * @param context Context
     * @param resId   消息资源Id
     */
    public static void showShort(Context context, @StringRes int resId)
    {
        String message = context.getResources().getString(resId);
        show(context, message, 0, 0);
    }

    /**
     * 显示
     *
     * @param context      Context
     * @param message      消息
     * @param showTime     显示时间
     * @param drawableIcon 带图片的Toast
     */
    public static void show(Context context, String message, int showTime, int drawableIcon)
    {
        if (showTime > 0 && showTime != Toast.LENGTH_SHORT && showTime != Toast.LENGTH_LONG)
        {
            throw new IllegalArgumentException("showTime must be TOAST.LENGTH_SHORT or TOAST.LENGTH_SHORT");
        }

        DURATION = (showTime == Toast.LENGTH_LONG ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);

        DURATIONTIME = DURATION == Toast.LENGTH_SHORT ? SHORT_DELAY : LONG_DELAY;

        if (S_TOAST == null)
        {
            S_TOAST = S_TOAST.makeText(context, message, DURATION);
            S_TOAST.setGravity(POSTION, 0, 35);
            if (drawableIcon != 0)
            {
                LinearLayout toastView = (LinearLayout) S_TOAST.getView();
                ImageView imageCodeProject = new ImageView(context);
                imageCodeProject.setImageResource(drawableIcon);
                toastView.addView(imageCodeProject, 0);
            }
        }
        if (message.equals(S_OLDMESSAGE))
        {
            NEXT_TIME = System.currentTimeMillis();
            if ((NEXT_TIME - FIRST_TIME) > DURATIONTIME)
            {
                S_TOAST.show();
            }
        } else
        {
            S_OLDMESSAGE = message;
            FIRST_TIME = System.currentTimeMillis();
            S_TOAST.setText(message);
            S_TOAST.setDuration(DURATION);
            S_TOAST.show();
        }
    }

    /**
     * 显示
     *
     * @param context      Context
     * @param resId        消息资源ID
     * @param showTime     显示时间
     * @param drawableIcon 带图片的Toast
     */
    public static void show(Context context, @StringRes int resId, int showTime, int drawableIcon)
    {
        String message = context.getResources().getString(resId);
        show(context, message, showTime, drawableIcon);
    }

    /**
     * 显示 子线程
     *
     * @param context  Context
     * @param message  消息
     * @param showTime 显示时间
     */
    public static void showOnThread(final Context context, final String message, final int showTime)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                show(context, message, showTime, 0);
            }
        }).start();
    }

    /**
     * 子线程
     *
     * @param context  Context
     * @param resId    资源ID
     * @param showTime 显示时间
     */
    public static void showOnThread(final Context context, @StringRes final int resId, final int showTime)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                show(context, resId, showTime, 0);
            }
        }).start();
    }
}
