package east.orientation.caster;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.xuhao.android.libsocket.sdk.OkSocket;

import java.io.File;

import east.orientation.caster.cast.CastScreenService;

import east.orientation.caster.local.AppInfo;
import east.orientation.caster.local.lifecycle.MobclickAgent;
import east.orientation.caster.cast.request.LogoutRequest;
import east.orientation.caster.cast.request.StopCastRequest;


/**
 * Created by ljq on 2018/3/6.
 */

public class CastApplication extends Application {
    private static CastApplication sAppInstance;
    public static AppInfo sAppInfo;
    private ExitBroadcastReceiver mExitBroadcastReceiver;

    public static CastApplication getAppContext(){
        return sAppInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
        // 初始化AppInfo
        sAppInfo = new AppInfo(this);
        // 注册广播
        initExitReceiver();
        // 初始化OkSocket
        OkSocket.initialize(this);
        // activity管理初始化
        MobclickAgent.init(this);
        // 开启投屏服务
        startService(CastScreenService.getStartIntent(this));
    }

    public static AppInfo getAppInfo() {
        return sAppInfo;
    }

    private void initExitReceiver(){
        mExitBroadcastReceiver = new ExitBroadcastReceiver();
        sAppInstance.registerReceiver(mExitBroadcastReceiver,new IntentFilter("close_caster"));
    }

    public void AppExit(){
        Log.e("APP","AppExit");
        if (sAppInfo.getConnectionManager() != null){
            // 发送登出服务器请求
            sAppInfo.getConnectionManager().send(new LogoutRequest());
            // 发送关闭大屏显示请求
            sAppInfo.getConnectionManager().send(new StopCastRequest());
        }

        // 注销广播
        sAppInstance.unregisterReceiver(mExitBroadcastReceiver);
        // 停止服务
        if (sAppInfo.getCastScreenService() != null){
            sAppInfo.getCastScreenService().onDestroy();
        }
        MobclickAgent.exit();
    }
}
