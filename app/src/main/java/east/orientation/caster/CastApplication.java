package east.orientation.caster;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.common.ViseConfig;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.interceptor.HttpLogInterceptor;
import com.xuhao.android.libsocket.sdk.OkSocket;

import java.io.File;

import east.orientation.caster.cast.CastScreenService;
import east.orientation.caster.cnjy21.constant.APIConstant;
import east.orientation.caster.local.AppInfo;
import east.orientation.caster.local.lifecycle.MobclickAgent;
import east.orientation.caster.cast.request.LogoutRequest;
import east.orientation.caster.cast.request.StopCastRequest;
import east.orientation.caster.soket.Client;
import east.orientation.caster.soket.SocketTransceiver;
import east.orientation.caster.sync.SyncService;
import okhttp3.Cache;

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
        // 初始化http Log
        initLog();
        // 初始化http
        initNet();
        // 开启投屏服务
        startService(CastScreenService.getStartIntent(this));
        // 开启同步服务
        startService(new Intent(this, SyncService.class));
    }

    public static AppInfo getAppInfo() {
        return sAppInfo;
    }

    private void initExitReceiver(){
        mExitBroadcastReceiver = new ExitBroadcastReceiver();
        sAppInstance.registerReceiver(mExitBroadcastReceiver,new IntentFilter("close_caster"));
    }

    private void initLog() {
        ViseLog.getLogConfig()
                .configAllowLog(true)//是否输出日志
                .configShowBorders(false);//是否排版显示
        ViseLog.plant(new LogcatTree());//添加打印日志信息到Logcat的树
    }

    private void initNet() {
        ViseHttp.init(this);
        ViseHttp.CONFIG()
                //配置请求主机地址 http://dev.21cnjy.com/
                .baseUrl(APIConstant.DOMAIN)
                .setCookie(true)
                //配置是否使用OkHttp的默认缓存
                .setHttpCache(true)
                //配置OkHttp缓存路径
                .setHttpCacheDirectory(new File(ViseHttp.getContext().getCacheDir(), ViseConfig.CACHE_HTTP_DIR))
                //配置自定义OkHttp缓存
                .httpCache(new Cache(new File(ViseHttp.getContext().getCacheDir(), ViseConfig.CACHE_HTTP_DIR), ViseConfig.CACHE_MAX_SIZE))
                //配置自定义离线缓存
                .cacheOffline(new Cache(new File(ViseHttp.getContext().getCacheDir(), ViseConfig.CACHE_HTTP_DIR), ViseConfig.CACHE_MAX_SIZE))
                //配置自定义在线缓存
                .cacheOnline(new Cache(new File(ViseHttp.getContext().getCacheDir(), ViseConfig.CACHE_HTTP_DIR), ViseConfig.CACHE_MAX_SIZE))
                //配置日志拦截器
                .interceptor(new HttpLogInterceptor()
                        .setLevel(HttpLogInterceptor.Level.BODY));

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
        if (sAppInfo.getSyncService() != null){
            sAppInfo.getSyncService().onDestroy();
        }
        MobclickAgent.exit();
    }
}
