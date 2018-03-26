package east.orientation.caster.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import east.orientation.caster.R;

/**
 * Created by ljq on 2018/3/22.
 */

public class FloatLayout extends FrameLayout {
    // windowManager
    private WindowManager mWindowManager;

    // params
    private WindowManager.LayoutParams mParams;

    // 记录悬浮窗宽度
    private int mViewWidth;

    // 记录悬浮窗高度
    private int mViewHeight;

    // 记录状态栏高度
    private int mStatusBarHeight;

    //记录当前手指位置在屏幕上的横坐标值
    private float xInScreen;

    //记录当前手指位置在屏幕上的纵坐标值
    private float yInScreen;

    //记录手指按下时在屏幕上的横坐标的值
    private float xDownInScreen;

    //记录手指按下时在屏幕上的纵坐标的值
    private float yDownInScreen;

    //记录手指按下时在小悬浮窗的View上的横坐标的值
    private float xInView;

    //记录手指按下时在小悬浮窗的View上的纵坐标的值
    private float yInView;



    public FloatLayout(Context context) {
        super(context);
        mWindowManager = WindowFloatManager.getWindowManager();
        LayoutInflater.from(context).inflate(R.layout.layout_float,this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                //if (isLongPress) {
                    // 长按手指移动的时候更新小悬浮窗的位置
                    updateViewPosition();
                //}
                break;
            case MotionEvent.ACTION_UP:
                //isLongPress = false;
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {

                }
                break;
            default:
                break;
        }
        //return mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params
     *            小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        mWindowManager.updateViewLayout(this, mParams);
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                mStatusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mStatusBarHeight;
    }

    /**
     * 得到view宽度
     *
     * @return
     */
    public int getViewWidth() {
        return mViewWidth;
    }

    /**
     * 得到view高度
     * @return
     */
    public int getViewHeight() {
        return mViewHeight;
    }
}
