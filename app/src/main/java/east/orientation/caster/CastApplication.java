package east.orientation.caster;

import android.app.Activity;
import android.app.Application;
import android.app.ApplicationErrorReport;

import com.xuhao.android.libsocket.sdk.OkSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import east.orientation.caster.cast.CastScreenService;
import east.orientation.caster.local.AppInfo;
import east.orientation.caster.local.lifecycle.MobclickAgent;
import east.orientation.caster.request.LogoutRequest;
import east.orientation.caster.request.StopCastRequest;

/**
 * Created by ljq on 2018/3/6.
 */

public class CastApplication extends Application {
    private static CastApplication sAppInstance;

    public static AppInfo sAppInfo;


    public static CastApplication getAppContext(){
        return sAppInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
        // activity管理初始化
        MobclickAgent.init(this);
        // 初始化OkSocket
        OkSocket.initialize(this);
        // 初始化AppInfo
        sAppInfo = new AppInfo(this);
        // 开启投屏服务
        startService(CastScreenService.getStartIntent(this));
    }

    public static AppInfo getAppInfo() {
        return sAppInfo;
    }

    public void AppExit(){
        // 发送登出服务器请求
        getAppInfo().getConnectionManager().send(new LogoutRequest());
        // 发送关闭大屏显示请求
        getAppInfo().getConnectionManager().send(new StopCastRequest());
        MobclickAgent.exit();
    }
}
