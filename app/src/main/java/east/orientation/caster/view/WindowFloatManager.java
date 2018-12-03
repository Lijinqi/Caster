package east.orientation.caster.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;

import east.orientation.caster.R;
import east.orientation.caster.cast.request.ChageModelRequest;
import east.orientation.caster.cast.request.ScreenRotationRequest;
import east.orientation.caster.evevtbus.CastMessage;
import east.orientation.caster.local.Common;
import east.orientation.caster.local.VideoConfig;
import east.orientation.caster.ui.activity.SettingActivity;
import east.orientation.caster.util.RomUtils;
import east.orientation.caster.util.SharePreferenceUtil;
import east.orientation.caster.util.ToastUtil;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
import static east.orientation.caster.CastApplication.getAppContext;
import static east.orientation.caster.CastApplication.getAppInfo;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_TRY_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_TCP_OK;
import static east.orientation.caster.local.Common.FLAG_SCREEN_ROTATION_0;
import static east.orientation.caster.local.Common.FLAG_SCREEN_ROTATION_1;


/**
 * Created by ljq on 2018/3/22.
 */

public class WindowFloatManager {
    private static final String TAG = "WindowFloatManager";

    enum CastModel {
        Normal(0),
        Large(1);

        int value;

        CastModel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private CastModel mCastModel = CastModel.Normal;
    private static WindowManager sWindowManager;
    private static DisplayMetrics sDisplayMetrics;
    private static Context mContext;

    private static int[] DEFAULT_ICONS = new int[]{R.mipmap.ic_cast_large, R.mipmap.ic_stu_screen, R.mipmap.ic_cast_all, R.mipmap.ic_setting, R.mipmap.ic_exit};
    private int[] mIconsId;

    private double px;// 大屏 H / W
    private int mStatusBarHeight;
    private int mNavigationbarHeight;

    private int mScreenWidth;// &录屏 分辨率W
    private int mScreenHeight;// &录屏 分辨率H
    private int mRectHeight;// 选中区域高度

    private boolean isInit;// 是否初始化
    private int mStartLine;// 起始位置
    private LineStartChangeListener mLineStartChangeListener;
    //private View mLine;
    private WindowManager.LayoutParams mLineParams;
    private PureVerticalSeekBar mSeekBar;
    private WindowManager.LayoutParams mSeekParams;

    private WindowFloatManager(Context context) {
        mContext = context;
        sWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        sDisplayMetrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(sDisplayMetrics);
        mIconsId = DEFAULT_ICONS;
    }

    private static class InnerClass {
        private static WindowFloatManager instance = new WindowFloatManager(getAppContext());
    }

    public static WindowFloatManager getInstance() {
        return InnerClass.instance;
    }

    /**
     * 得到 WindowManger对象
     *
     * @return
     */
    public static WindowManager getWindowManager() {
        if (sWindowManager == null)
            sWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return sWindowManager;
    }

    public static DisplayMetrics getDisplayMetrics() {
        if (sDisplayMetrics == null) {
            sDisplayMetrics = new DisplayMetrics();
            sWindowManager.getDefaultDisplay().getMetrics(sDisplayMetrics);
        }
        return sDisplayMetrics;
    }

    /**
     * 设置图标资源
     */
    public void setResource(int[] iconsId) {
        if (iconsId != null && iconsId.length > 0)
            mIconsId = iconsId;
    }

    public void setLineStartChangeListener(LineStartChangeListener listener) {
        mLineStartChangeListener = listener;
    }

    public void setScreen() {
        if (!isInit) return;
        sWindowManager.getDefaultDisplay().getMetrics(sDisplayMetrics);
        int indexSize = SharePreferenceUtil.get(getAppContext(), Common.KEY_SIZE, 0);
        mScreenWidth = VideoConfig.RESOLUTION_OPTIONS[0][indexSize];
        mScreenHeight = VideoConfig.RESOLUTION_OPTIONS[1][indexSize];
        mRectHeight = (int) (mScreenWidth * px);// 根据录屏分辨率算
        // 更改大屏实际播放分辨率
        int left, top, right, bottom;
        left = 0;
        right = mScreenWidth;
        bottom = (int) (mScreenHeight * ((double) mLineParams.y / (double) (sDisplayMetrics.heightPixels - mStatusBarHeight)));
        top = bottom - mRectHeight;
        if (mLineStartChangeListener != null) {
            mLineStartChangeListener.onChange(left, top, right, bottom);
        }
        if (getAppInfo().getConnectionManager() != null) {
            getAppInfo().getConnectionManager().send(new ScreenRotationRequest(FLAG_SCREEN_ROTATION_1));
        }
        if (getAppInfo().isStreamRunning()) {
            showOrHideScrollView(true);
        }
    }

    public void setHorizontal() {
        if (!isInit) return;
        sWindowManager.getDefaultDisplay().getMetrics(sDisplayMetrics);
        int indexSize = SharePreferenceUtil.get(getAppContext(), Common.KEY_SIZE, 0);
        mScreenWidth = VideoConfig.RESOLUTION_OPTIONS[0][indexSize];
        mScreenHeight = VideoConfig.RESOLUTION_OPTIONS[1][indexSize];
        int left, top, right, bottom;
        left = 0;
        right = mScreenWidth;
        bottom = mScreenHeight;
        top = 0;
        if (mLineStartChangeListener != null) {
            mLineStartChangeListener.onChange(left, top, right, bottom);
        }
        if (getAppInfo().getConnectionManager() != null) {
            getAppInfo().getConnectionManager().send(new ScreenRotationRequest(FLAG_SCREEN_ROTATION_0));
        }

        showOrHideScrollView(false);
    }

    public void initScroll(int large_width, int large_height) {
        isPort = false;
//        if (mLine != null){
//            if (mLine.isAttachedToWindow())
//                sWindowManager.removeView(mLine);
//        }
//        if (mSeekBar != null){
//            if (mSeekBar.isAttachedToWindow())
//                sWindowManager.removeView(mSeekBar);
//        }
        px = (double) large_height / (double) large_width;
        int indexSize = SharePreferenceUtil.get(getAppContext(), Common.KEY_SIZE, 0);
        mScreenWidth = VideoConfig.RESOLUTION_OPTIONS[0][indexSize];
        mScreenHeight = VideoConfig.RESOLUTION_OPTIONS[1][indexSize];

        // 得到状态栏 和 底部 操作栏高度
        getStatusAndNavigationHeight();

        mRectHeight = (int) (mScreenWidth * px);// 根据录屏分辨率算
        if (isROTATION_0()) {
            mStartLine = (int) (sDisplayMetrics.widthPixels * px - mStatusBarHeight);// 根据实际分辨率计算
        } else {
            mStartLine = (int) ((sDisplayMetrics.heightPixels + mNavigationbarHeight) * px - mStatusBarHeight);// 根据实际分辨率计算
        }

//        mLine = new View(mContext);
//        mLine.setBackgroundColor(Color.RED);
        mLineParams = getDefaultSystemWindowParams();
        mLineParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLineParams.width = sDisplayMetrics.widthPixels;
        mLineParams.height = 2;
        mLineParams.y = mStartLine;

        if (mSeekBar == null) {
            mSeekBar = new PureVerticalSeekBar(mContext);
        }
        //mSeekBar.setColor(mContext.getResources().getColor(R.color.aliceblue),mContext.getResources().getColor(R.color.blue));
        mSeekBar.setProgress(0);
        mSeekBar.setImage_background(R.mipmap.app_icon);
        mSeekBar.setDragable(true);
        mSeekBar.setPadding(80, 0, 80, 0);
        mSeekBar.setOnSlideChangeListener(new PureVerticalSeekBar.OnSlideChangeListener() {
            @Override
            public void OnSlideChangeListener(View view, float progress) {
                mLineParams.y = (int) ((sDisplayMetrics.heightPixels - mStartLine - mStatusBarHeight) * (progress / 100) + mStartLine);

                int left, top, right, bottom;
                left = 0;
                right = mScreenWidth;
                bottom = (int) (mScreenHeight * ((double) mLineParams.y / (double) (sDisplayMetrics.heightPixels - mStatusBarHeight)));
                top = bottom - mRectHeight;
                if (mLineStartChangeListener != null) {
                    mLineStartChangeListener.onChange(left, top, right, bottom);
                }

                mLineParams.y = mLineParams.y + mNavigationbarHeight;
//                if (mLineParams.y>sDisplayMetrics.heightPixels)
//                    mLine.setVisibility(View.GONE);
//                else
//                    mLine.setVisibility(View.VISIBLE);
//                sWindowManager.updateViewLayout(mLine,mLineParams);
            }

            @Override
            public void onSlideStopTouch(View view, float progress) {

            }
        });

        mSeekParams = getDefaultSystemWindowParams();
        mSeekParams.x = 10;// 距离特定边距离 根据Gravity
        mSeekParams.width = 70;
        mSeekParams.height = sDisplayMetrics.heightPixels / 5;

        isInit = true;
        if (isROTATION_0()) {
            setScreen();
        } else {
            setHorizontal();
        }
    }

    private void getStatusAndNavigationHeight() {
        Resources resources = mContext.getResources();
        int resIdStatusbarHeight = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resIdStatusbarHeight > 0) {
            mStatusBarHeight = resources.getDimensionPixelSize(resIdStatusbarHeight);//状态栏高度
        }
        int resIdShow = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        boolean hasNavigationBar = false;
        if (resIdShow > 0) {
            hasNavigationBar = resources.getBoolean(resIdShow);//是否显示底部navigationBar
        }
        if (hasNavigationBar) {
            int resIdNavigationBar = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resIdNavigationBar > 0) {
                mNavigationbarHeight = resources.getDimensionPixelSize(resIdNavigationBar);//navigationBar高度
            }

        }
    }

    private boolean isPort;

    public void showOrHideScrollView(boolean isShow) {
        if (isShow) {
            if (isROTATION_0()) {

                if (!isPort /*&& mLine!= null*/ && mSeekBar != null) {
//                    if (mLine.isAttachedToWindow()){
//                        sWindowManager.removeView(mLine);
//                    }
                    if (mSeekBar.isAttachedToWindow()) {
                        sWindowManager.removeView(mSeekBar);
                    }
                    //sWindowManager.addView(mLine,mLineParams);
                    sWindowManager.addView(mSeekBar, mSeekParams);
                }
                isPort = true;
            } else {
                isPort = false;
            }
        } else {
            if (/*mLine!= null &&*/ mSeekBar != null) {
//                if (mLine.isAttachedToWindow()){
//
//                    sWindowManager.removeView(mLine);
//                }
                if (mSeekBar.isAttachedToWindow()) {

                    sWindowManager.removeView(mSeekBar);
                }
                isPort = false;
            }
        }
    }

    /**
     * 是否竖屏
     */
    public boolean isROTATION_0() {
        int rotation = sWindowManager.getDefaultDisplay().getRotation();
        Log.d(TAG, "rotation " + rotation);
        if (ROTATION_0 == rotation || ROTATION_180 == rotation)
            return true;
        else
            return false;
    }

    /**
     * 系统悬浮
     */
    private FloatingActionButton mFloatingActionButton;
    private FloatingActionMenu mFloatingActionMenu;

    public void showFloatMenus() {

        // 子菜单图标
        ImageView[] menuIcons = new ImageView[mIconsId.length];
        // 在菜单按钮
        SubActionButton[] subButtons = new SubActionButton[mIconsId.length];
        // 主按钮
        ImageView fabIcon = new ImageView(mContext);
        fabIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        fabIcon.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.snow));

        SubActionButton.Builder subBuilder = new SubActionButton.Builder(mContext);
        FloatingActionMenu.Builder menuBuilder = new FloatingActionMenu.Builder(mContext, true);

        WindowManager.LayoutParams params = FloatingActionButton.Builder.getDefaultSystemWindowParams(mContext);

        mFloatingActionButton = new FloatingActionButton.Builder(mContext)
                .setContentView(fabIcon)
                .setSystemOverlay(true)
                .setLayoutParams(params)
                .setPosition(FloatingActionButton.POSITION_RIGHT_CENTER)
                .build();

        for (int i = 0; i < mIconsId.length; i++) {
            menuIcons[i] = new ImageView(mContext);
            menuIcons[i].setImageDrawable(mContext.getResources().getDrawable(mIconsId[i]));
            menuIcons[i].setScaleType(ImageView.ScaleType.CENTER);
            subButtons[i] = subBuilder.setContentView(menuIcons[i]).build();
            menuBuilder.addSubActionView(subButtons[i], subButtons[i].getLayoutParams().width, subButtons[i].getLayoutParams().height);
        }
        menuBuilder.setStartAngle(180);
        menuBuilder.setEndAngle(270);
        menuBuilder.attachTo(mFloatingActionButton);
        menuBuilder.setActionViewLongPressListener(actionView -> {
//          if(!getAppInfo().isActivityRunning()){//判断是否已打开
            // 打开主页面
//
//            mContext.startActivity(new Intent(mContext, MainActivity.class)
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//          }

        });
        mFloatingActionMenu = menuBuilder.build();

        // 子菜单项点击事件
        setItemClickListener(subButtons);

        // actionView 动画
        PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 360);
        final ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(mFloatingActionButton, pvhR);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setDuration(800);
        mFloatingActionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {

                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                animation.start();

            }
        });
    }

    private void setItemClickListener(SubActionButton[] subButtons) {
        // 投屏
        subButtons[0].setOnClickListener(v -> {
            if (getAppInfo().isServerConnected()) {
                if (getAppInfo().isStreamRunning()) {
                    // 如果正在投屏则停止
                    //EventBus.getDefault().post(new CastMessage(MESSAGE_ACTION_STREAMING_STOP));
                    if (mCastModel == CastModel.Normal) {
                        getAppInfo().getConnectionManager().send(new ChageModelRequest(CastModel.Large.getValue()));
                        mCastModel = CastModel.Large;
                        if (mSeekBar != null) mSeekBar.setVisibility(View.VISIBLE);
                    } else {
                        getAppInfo().getConnectionManager().send(new ChageModelRequest(CastModel.Normal.getValue()));
                        mCastModel = CastModel.Normal;
                        if (mSeekBar != null) mSeekBar.setVisibility(View.GONE);
                    }
                } else {
                    // 如果未投屏则开启
                    CastMessage stickyEvent = EventBus.getDefault().getStickyEvent(CastMessage.class);
                    if (stickyEvent == null || MESSAGE_STATUS_TCP_OK.equals(stickyEvent.getMessage())) {
                        EventBus.getDefault().postSticky(new CastMessage(MESSAGE_ACTION_STREAMING_TRY_START));
                    }
                    getAppInfo().getConnectionManager().send(new ChageModelRequest(CastModel.Normal.getValue()));
                    if (mSeekBar != null) mSeekBar.setVisibility(View.GONE);
                }

            } else {
                ToastUtil.showToast("未连接服务器,请开启服务器！");
            }
        });
        // 演示
        subButtons[1].setOnClickListener(v -> {
            ToastUtil.showToast("开发ing !");
        });
        // 广播
        subButtons[2].setOnClickListener(v -> {
            ToastUtil.showToast("开发ing !");

        });
        // 设置
        subButtons[3].setOnClickListener(v -> {
            mContext.startActivity(new Intent(mContext, SettingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        subButtons[4].setOnClickListener(v -> {
            getAppContext().AppExit();
        });
    }

    public static WindowManager.LayoutParams getDefaultSystemWindowParams() {
        int paramType = TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            paramType = TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            paramType = TYPE_PHONE;
        } else {
            paramType = TYPE_SYSTEM_ALERT;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.CENTER | Gravity.LEFT;
        return params;
    }

    /**
     * 检查权限
     *
     * @return
     */
    public static boolean checkFloatWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(getAppContext());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //AppOpsManager添加于API 19
            return checkOps();
        } else {
            //4.4以下一般都可以直接添加悬浮窗
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean checkOps() {
        try {
            Object object = getAppContext().getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = getAppContext().getPackageName();
            int m = (Integer) method.invoke(object, arrayOfObject1);
            //4.4至6.0之间的非国产手机，例如samsung，sony一般都可以直接添加悬浮窗
            return m == AppOpsManager.MODE_ALLOWED || !RomUtils.isDomesticSpecialRom();
        } catch (Exception ignore) {
        }
        return false;
    }

    public interface LineStartChangeListener {
        void onPrepare(int left, int top, int right, int bottom);

        void onChange(int left, int top, int right, int bottom);
    }
}
