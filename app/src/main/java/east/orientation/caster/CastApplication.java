package east.orientation.caster;

import android.app.Application;
import android.content.IntentFilter;
import android.os.Process;
import android.util.Log;


import com.xuhao.didi.socket.client.sdk.OkSocket;

import org.greenrobot.eventbus.EventBus;

import east.orientation.caster.cast.service.CastScreenService;

import east.orientation.caster.evevtbus.CastMessage;
import east.orientation.caster.local.AppInfo;
import east.orientation.caster.local.lifecycle.MobclickAgent;
import east.orientation.caster.cast.request.LogoutRequest;
import east.orientation.caster.cast.request.StopCastRequest;
import east.orientation.caster.socket.SocketManager;


/**
 * Created by ljq on 2018/3/6.
 */

public class CastApplication extends Application {
    private static CastApplication sAppInstance;
    public static AppInfo sAppInfo;
    private ExitBroadcastReceiver mExitBroadcastReceiver;

    public static CastApplication getAppContext() {
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
        SocketManager.getInstance().init();
        // activity管理初始化
        MobclickAgent.init(this);
        // 开启投屏服务
        startService(CastScreenService.getStartIntent(this));
    }

    public static AppInfo getAppInfo() {
        return sAppInfo;
    }

    private void initExitReceiver() {
        mExitBroadcastReceiver = new ExitBroadcastReceiver();
        sAppInstance.registerReceiver(mExitBroadcastReceiver, new IntentFilter("close_caster"));
    }

    public void AppExit() {
        EventBus.getDefault().post(new CastMessage(CastMessage.MESSAGE_ACTION_STREAMING_STOP));
        if (sAppInfo.getConnectionManager() != null) {
            // 发送登出服务器请求
            sAppInfo.getConnectionManager().send(new LogoutRequest());
            // 发送关闭大屏显示请求
            sAppInfo.getConnectionManager().send(new StopCastRequest());
        }
        // 注销广播
        sAppInstance.unregisterReceiver(mExitBroadcastReceiver);
        // 断开更新网络连接
        SocketManager.getInstance().disConnect();
        // 停止服务
        if (sAppInfo.getCastScreenService() != null) {
            sAppInfo.getCastScreenService().stopSelf();
        }
        //sAppInfo.setConnectionManager(null);

    }
}
