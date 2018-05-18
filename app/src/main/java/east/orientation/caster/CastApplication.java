package east.orientation.caster;

import android.app.Application;

import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.interceptor.HttpLogInterceptor;
import com.xuhao.android.libsocket.sdk.OkSocket;

import east.orientation.caster.cast.CastScreenService;
import east.orientation.caster.cnjy21.constant.APIConstant;
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


    public static CastApplication getAppContext(){
        return sAppInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
        // 初始化OkSocket
        OkSocket.initialize(this);
        // activity管理初始化
        MobclickAgent.init(this);
        // 初始化http Log
        initLog();
        // 初始化http
        initNet();
        // 初始化AppInfo
        sAppInfo = new AppInfo(this);
        // 开启投屏服务
        startService(CastScreenService.getStartIntent(this));
    }

    public static AppInfo getAppInfo() {
        return sAppInfo;
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
                //配置日志拦截器
                .interceptor(new HttpLogInterceptor()
                        .setLevel(HttpLogInterceptor.Level.BODY));

    }

    public void AppExit(){
        if (sAppInfo.getConnectionManager() != null){
            // 发送登出服务器请求
            sAppInfo.getConnectionManager().send(new LogoutRequest());
            // 发送关闭大屏显示请求
            sAppInfo.getConnectionManager().send(new StopCastRequest());
        }
        MobclickAgent.exit();
    }
}
