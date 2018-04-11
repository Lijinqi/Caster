package east.orientation.caster.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;

import east.orientation.caster.R;
import east.orientation.caster.evevtbus.CastMessage;
import east.orientation.caster.ui.MainActivity;
import east.orientation.caster.ui.SettingActivity;
import east.orientation.caster.ui.WriteActivity;
import east.orientation.caster.util.FloatWindowPermissionChecker;
import east.orientation.caster.util.RomUtils;
import east.orientation.caster.util.ToastUtil;

import static east.orientation.caster.CastApplication.getAppContext;
import static east.orientation.caster.CastApplication.getAppInfo;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_TRY_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_TCP_OK;


/**
 * Created by ljq on 2018/3/22.
 */

public class WindowFloatManager {
    private static WindowManager sWindowManager;
    private static DisplayMetrics sDisplayMetrics;
    private static Context mContext;

    private static int[] DEFAULT_ICONS = new int[]{R.mipmap.ic_pen,R.mipmap.ic_res,R.mipmap.ic_cast_large,R.mipmap.ic_stu_screen,R.mipmap.ic_cast_all,R.mipmap.ic_setting};
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



    /**
     * 设置图标资源
     */
    public void setResource(int[] iconsId){
        if (iconsId != null && iconsId.length>0)
            mIconsId = iconsId;
    }

    private FloatingActionButton mFloatingActionButton;
    private FloatingActionMenu mFloatingActionMenu;

    public void showFloatMenus(){
        if (!FloatWindowPermissionChecker.checkFloatWindowPermission()){
            ToastUtil.show(mContext,"需开启权限");
            FloatWindowPermissionChecker.tryJumpToPermissionPage(mContext);
        }

        // 子菜单图标
        ImageView[] menuIcons = new ImageView[mIconsId.length];
        // 在菜单按钮
        SubActionButton[] subButtons = new SubActionButton[mIconsId.length];
        // 主按钮
        ImageView fabIcon = new ImageView(mContext);
        fabIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        fabIcon.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.app_launcher));

        SubActionButton.Builder subBuilder = new SubActionButton.Builder(mContext);
        FloatingActionMenu.Builder menuBuilder = new FloatingActionMenu.Builder(mContext,true);

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
            menuBuilder.addSubActionView(subButtons[i],subButtons[i].getLayoutParams().width,subButtons[i].getLayoutParams().height);
        }
        menuBuilder.setStartAngle(0);
        menuBuilder.setEndAngle(90);
        menuBuilder.attachTo(mFloatingActionButton);
        menuBuilder.setActionViewLongPressListener(new FloatingActionMenu.ActionViewLongPressListener() {
            @Override
            public void onLongPressed(View actionView) {
                if(!getAppInfo().isActivityRunning()){
                    // 打开主页面
                    Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                    mContext.startActivity(new Intent(mContext, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        });
        mFloatingActionMenu = menuBuilder.build();

        // 子菜单项点击事件
        setItemClickListener(subButtons);

        // actionView 动画
        PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 360);
        final ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(mFloatingActionButton, pvhR);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(-1);
        animation.setDuration(10);
//        mFloatingActionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
//            @Override
//            public void onMenuOpened(FloatingActionMenu menu) {
//                mFloatingActionButton.setRotation(0);
//                animation.start();
//            }
//
//            @Override
//            public void onMenuClosed(FloatingActionMenu menu) {
//                animation.cancel();
//                mFloatingActionButton.setRotation(0);
//            }
//        });
    }

    private void setItemClickListener(SubActionButton[] subButtons) {
        // 画板
        subButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, WriteActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        // 资源
        subButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show(mContext,"开发ing !");
            }
        });
        // 投屏
        subButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CastMessage stickyEvent = EventBus.getDefault().getStickyEvent(CastMessage.class);
                if (stickyEvent == null || MESSAGE_STATUS_TCP_OK.equals(stickyEvent.getMessage())) {
                    EventBus.getDefault().postSticky(new CastMessage(MESSAGE_ACTION_STREAMING_TRY_START));
                }
            }
        });
        // 演示
        subButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show(mContext,"开发ing !");
            }
        });
        // 广播
        subButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show(mContext,"开发ing !");
            }
        });
        // 设置
        subButtons[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, SettingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
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

}
