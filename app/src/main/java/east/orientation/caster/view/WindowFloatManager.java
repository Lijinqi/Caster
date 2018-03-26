package east.orientation.caster.view;

import android.app.AppOpsManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import java.lang.reflect.Method;

import east.orientation.caster.R;

import static east.orientation.caster.CastApplication.getAppContext;


/**
 * Created by ljq on 2018/3/22.
 */

public class WindowFloatManager {
    private static WindowManager sWindowManager;
    private static DisplayMetrics sDisplayMetrics;
    private static Context mContext;

    private int mScreenWidth;// 屏幕宽
    private int mScreenHeight;// 屏幕长

    private TipLayout mTipLayout;
    private LayoutParams mTipLayoutParam;

    private FloatLayout mFloatLayout;
    private LayoutParams mMenuLayoutParam;

    private int[] mIconsId;


    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    private WindowFloatManager(Context context){
        mContext = context;
        sWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        sDisplayMetrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(sDisplayMetrics);
        mScreenWidth = sDisplayMetrics.widthPixels;
        mScreenHeight = sDisplayMetrics.heightPixels;
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

    /**
     * 显示悬浮球
     */
    public void showTipView() {
        if (mTipLayout == null) {
            mTipLayout = new TipLayout(mContext);
            if (mTipLayoutParam == null && checkFloatWindowPermission()) {
                mTipLayoutParam = new LayoutParams();
                mTipLayoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                mTipLayoutParam.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mTipLayoutParam.format = PixelFormat.TRANSLUCENT;
                mTipLayoutParam.x = mScreenWidth / 2 - mTipLayout.getViewWidth() / 2;
                mTipLayoutParam.y = mScreenHeight / 2 - mTipLayout.getViewHeight() / 2;
                mTipLayoutParam.gravity = Gravity.LEFT | Gravity.TOP;
                mTipLayoutParam.width = mTipLayout.getViewWidth();
                mTipLayoutParam.height = mTipLayout.getViewHeight();
            }
            mTipLayout.setParams(mTipLayoutParam);
            getWindowManager().addView(mTipLayout, mTipLayoutParam);
        }
    }
    /**
     * 显示菜单
     */
    public void showFloatMenu(){
        if (mFloatLayout == null) {
            mFloatLayout = new FloatLayout(mContext);
            if (mMenuLayoutParam == null && checkFloatWindowPermission()) {
                mMenuLayoutParam = new LayoutParams();
                mMenuLayoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                mMenuLayoutParam.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mMenuLayoutParam.format = PixelFormat.TRANSLUCENT;
                mMenuLayoutParam.x = mScreenWidth / 2 - mFloatLayout.getViewWidth()/ 2;
                mMenuLayoutParam.y = mScreenHeight / 2 - mFloatLayout.getViewHeight()/ 2;
                mMenuLayoutParam.gravity = Gravity.LEFT | Gravity.TOP;
                mMenuLayoutParam.width = mFloatLayout.getViewWidth();
                mMenuLayoutParam.height = mFloatLayout.getViewHeight();
            }
            mFloatLayout.setParams(mMenuLayoutParam);
            getWindowManager().addView(mFloatLayout, mMenuLayoutParam);
        }
    }

    /**
     * 设置图标资源
     */
    public void setResourse(int[] iconsId){
        if (iconsId != null && iconsId.length>0)
            mIconsId = iconsId;
        // todo 设置默认图标

    }

    private FloatingActionButton mFloatingActionButton;
    private FloatingActionMenu mFloatingActionMenu;

    public void showFloatMenus(){
        ImageView[] menuIcons = new ImageView[mIconsId.length];
        SubActionButton[] subButtons = new SubActionButton[mIconsId.length];

        ImageView fabIcon = new ImageView(mContext);
        fabIcon.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.app_launcher));

        SubActionButton.Builder subBuilder = new SubActionButton.Builder(mContext);
        FloatingActionMenu.Builder menuBuilder = new FloatingActionMenu.Builder(mContext,true);

        WindowManager.LayoutParams params = FloatingActionButton.Builder.getDefaultSystemWindowParams(mContext);
//        params.x = 50;
//        params.y = 50;
        mFloatingActionButton = new FloatingActionButton.Builder(mContext)
                .setContentView(fabIcon)
                .setSystemOverlay(true)
                .setLayoutParams(params)
                .setPosition(FloatingActionButton.POSITION_TOP_LEFT)
                .build();

        for (int i = 0; i < mIconsId.length; i++) {
            menuIcons[i] = new ImageView(mContext);
            menuIcons[i].setImageDrawable(mContext.getResources().getDrawable(mIconsId[i]));
            subButtons[i] = subBuilder.setContentView(menuIcons[i]).build();
            menuBuilder.addSubActionView(subButtons[i],subButtons[i].getLayoutParams().width,subButtons[i].getLayoutParams().height);
        }
        menuBuilder.setStartAngle(180);
        menuBuilder.setEndAngle(270);
        menuBuilder.attachTo(mFloatingActionButton);
        mFloatingActionMenu = menuBuilder.build();
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
            return m == AppOpsManager.MODE_ALLOWED ;
        } catch (Exception ignore) {
        }
        return false;
    }

}
