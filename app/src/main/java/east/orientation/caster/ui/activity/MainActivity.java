package east.orientation.caster.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.client.impl.client.action.ActionDispatcher;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.NoneReconnect;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.nio.ByteOrder;

import east.orientation.caster.R;
import east.orientation.caster.cast.hotspot.WifiApManager;
import east.orientation.caster.cast.miracast.MiracastManager;
import east.orientation.caster.cast.service.CastScreenService;
import east.orientation.caster.cast.CastWaiter;
import east.orientation.caster.evevtbus.CastMessage;
import east.orientation.caster.evevtbus.ConnectMessage;
import east.orientation.caster.evevtbus.ModeMessage;
import east.orientation.caster.local.AudioConfig;
import east.orientation.caster.local.Common;
import east.orientation.caster.cast.protocol.NormalProtocol;
import east.orientation.caster.cast.request.LoginRequest;
import east.orientation.caster.cast.request.Mp3ParamsResponse;
import east.orientation.caster.cast.request.Pluse;
import east.orientation.caster.cast.request.SelectRectRequest;
import east.orientation.caster.cast.request.SelectRectResponse;
import east.orientation.caster.cast.request.StartCastRequest;
import east.orientation.caster.socket.SocketManager;
import east.orientation.caster.socket.UpdateManager;
import east.orientation.caster.util.BytesUtils;
import east.orientation.caster.util.CommonUtil;
import east.orientation.caster.util.SharePreferenceUtil;
import east.orientation.caster.util.ToastUtil;
import east.orientation.caster.view.WindowFloatManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static east.orientation.caster.CastApplication.getAppContext;
import static east.orientation.caster.CastApplication.getAppInfo;
import static east.orientation.caster.cast.service.CastScreenService.getProjectionManager;
import static east.orientation.caster.cast.service.CastScreenService.setMediaProjection;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_STOP;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_TRY_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_CAST_GENERATOR_ERROR;

import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_TCP_OK;
import static east.orientation.caster.local.VideoConfig.REQUEST_CODE_SCREEN_CAPTURE;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ACTION_SHUTDOWN = 1;
    private static final int ACTION_DISCONNECTED = 2;
    private static final int ACTION_CONNECTED = 3;

    private WifiApManager mWifiApManager;
    private MiracastManager mMiracastManager;
    private int mCurrenMode;

    private int mResultCode;// 请求录屏权限返回的 code
    private Intent mResultData;//请求录屏权限返回的 intent
    //private boolean isReset;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACTION_SHUTDOWN:
                    ToastUtil.showToast("服务器断开", Toast.LENGTH_LONG);
                    break;
                case ACTION_DISCONNECTED:
                    ToastUtil.showToast("服务器断开", Toast.LENGTH_LONG);
                    break;
                case ACTION_CONNECTED:
                    ToastUtil.showToast("已连接服务器", Toast.LENGTH_LONG);
                    break;
            }
        }
    };

    // 屏幕旋转监听
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int rotation = WindowFloatManager.getWindowManager().getDefaultDisplay().getRotation();
            Log.e("@@", "屏 :" + rotation);
            if (ROTATION_0 == rotation || ROTATION_180 == rotation) {
                // 竖屏
                WindowFloatManager.getInstance().setScreen();
            } else if (ROTATION_90 == rotation || ROTATION_270 == rotation) {
                // 横屏
                WindowFloatManager.getInstance().setHorizontal();
            }
        }
    };

    private ConnectionInfo mConnectionInfo;
    private OkSocketOptions mOkOptions;
    private SocketActionAdapter mSocketActionAdapter = new SocketActionAdapter() {
        @Override
        public void onSocketIOThreadStart(String action) {
            super.onSocketIOThreadStart(action);
            Log.e(TAG, "onSocketIOThreadStart");
        }

        @Override
        public void onSocketIOThreadShutdown(String action, Exception e) {
            super.onSocketIOThreadShutdown(action, e);
            Log.e("@@", "onSocketIOThreadShutdown" + e);
            // 重置搜索广播
            resetSearcher();
            mHandler.obtainMessage(ACTION_SHUTDOWN).sendToTarget();

        }

        @Override
        public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
            super.onSocketDisconnection(info, action, e);
            Log.e("@@", "onSocketDisconnection" + e);
            // 重置搜索广播
            resetSearcher();
        }

        @Override
        public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
            super.onSocketConnectionSuccess(info, action);

            // 设置连接状态
            getAppInfo().setServerConnected(true);
            // 连接成功则发送心跳
            getAppInfo().getConnectionManager().getPulseManager().setPulseSendable(new Pluse(1)).pulse();
            // 连接成功则发送登陆请求
            getAppInfo().getConnectionManager().send(new LoginRequest(Common.LOGIN_TYPE_TEACHER));
            Log.e("@@", "onSocketConnectionSuccess");
            mHandler.obtainMessage(ACTION_CONNECTED).sendToTarget();
        }

        @Override
        public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
            super.onSocketConnectionFailed(info, action, e);
            Log.e("@@", "onSocketConnectionFailed" + e);

            // 重置搜索广播
            resetSearcher();
        }

        @Override
        public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(info, action, data);
            //Log.e(TAG,"onSocketReadResponse");
            // 处理回执
            handleResponse(data);
        }

        @Override
        public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
            super.onSocketWriteResponse(info, action, data);
            //Log.e(TAG,"发送 ："+data.parse().length);
        }

        @Override
        public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
            super.onPulseSend(info, data);
            //Log.e(TAG,"心跳已发送");
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "Caster onNewIntent");
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏窗口
        hideWindow();
        // 注册eventbus
        EventBus.getDefault().register(this);
        // runtime permission
        MainActivityPermissionsDispatcher.showSystemWindowWithPermissionCheck(this);
        MainActivityPermissionsDispatcher.getRuntimePermissionWithPermissionCheck(this);
        // 初始化
        init();

        setContentView(R.layout.activity_main);
    }

    private void hideWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = 0;
        params.height = 0;//此句用于自定义窗口大小，实现Activity窗口非全屏显示
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        this.getWindow().setAttributes(params);//params2用于设备整个Activity的窗口属性
    }

    /**
     * 初始化
     */
    private void init() {
        if (SharePreferenceUtil.get(getApplicationContext(),Common.KEY_CAST_MODE,0) == Common.CAST_MODE_WIFI) {
            // 默认开启广播搜索
            //connectToServer("192.168.0.108",10402);
            startSearchServer();
        } else if (SharePreferenceUtil.get(getApplicationContext(),Common.KEY_CAST_MODE,0) == Common.CAST_MODE_MIRACAST) {
            // 默认开启miracast
            startMiracast();
        } else if (SharePreferenceUtil.get(getApplicationContext(),Common.KEY_CAST_MODE,0) == Common.CAST_MODE_HOTSPOT) {
            // 默认开启热点
            startAp("Caster","12345678");
            // 开启广播搜索
            startSearchServer();
        }

        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
        // 检查更新
        checkUpdate();
    }

    private void checkUpdate() {
        Log.i(TAG, "checkUpdate " + SocketManager.getInstance().isConnect());
        UpdateManager.init(getApplicationContext(), new UpdateManager.UpdateAdapter() {
            @Override
            public void update(UpdateManager.Apk apk) {
                Log.d(TAG, "update: " + apk);
                int localVersion = CommonUtil.getAppVersionCode(getApplicationContext());
                if (localVersion > 0 && localVersion < apk.getVersionCode())
                    UpdateManager.download(UpdateManager.TYPE_APK, apk.getApkName(), 0, UpdateManager.DEFAULT_PACKAGE_SIZE);
            }

            @Override
            public void noUpdate() {
                Log.d(TAG, "no update");
            }
        }, new UpdateManager.DownloadAdapter() {
            @Override
            public void start(String apkName) {
                Log.d(TAG, "download start: " + apkName);
                UpdateManager.createNotification(getApplicationContext());
            }

            @Override
            public void progress(long progress, long totalSize) {
                Log.d(TAG, "download progress: " + progress + "/" + totalSize);
                UpdateManager.updateNotification(progress, totalSize);
            }

            @Override
            public void finish(final String apkPath) {
                Log.d(TAG, "download end: " + apkPath);
                UpdateManager.finishNotification(getApplicationContext(), new File(apkPath));

                UpdateManager.install(new File(apkPath), getApplicationContext());

                UpdateManager.onCancel();
            }
        });
        UpdateManager.getUpdate(UpdateManager.TYPE_APK, Common.APP_KEY);
    }

    private void startMiracast() {
        if (mMiracastManager == null)
            mMiracastManager = new MiracastManager(getApplicationContext());
        mMiracastManager.start();
    }

    private void stopMiracast() {
        if (mMiracastManager == null)
            mMiracastManager = new MiracastManager(getApplicationContext());
        mMiracastManager.stop();
    }

    /**
     * 开启热点
     * @param ssid
     * @param password
     */
    private void startAp(String ssid,String password) {
        if (mWifiApManager == null)
            mWifiApManager = new WifiApManager(getApplicationContext());
        mWifiApManager.createHotspot(ssid,password);
    }

    /**
     * 关闭热点
     */
    private void stopAp() {
        if (mWifiApManager == null)
            mWifiApManager = new WifiApManager(getApplicationContext());
        mWifiApManager.stopHotspot();
    }

    /**
     * start udp广播监听
     */
    private void startSearchServer() {
        CastWaiter.getInstance().setSearchListener((ip, port) -> {
            EventBus.getDefault().post(new ConnectMessage(ip, port));
        });
        CastWaiter.getInstance().start();
    }

    /**
     * stop udp广播监听
     */
    private void stopSearchServer() {
        CastWaiter.getInstance().stop();
    }

    /**
     * 重置 搜索广播 、客户端连接状态
     */
    private void resetSearcher() {
        if (mCurrenMode == Common.CAST_MODE_MIRACAST) return;
        // 设置连接状态
        if (getAppInfo().isServerConnected()) getAppInfo().setServerConnected(false);
        // 发送停止投屏广播
        if (getAppInfo().isStreamRunning()) EventBus.getDefault().post(new CastMessage(MESSAGE_ACTION_STREAMING_STOP));
        // 开启接收udp广播
        stopSearchServer();
        startSearchServer();
    }

    private Object mLock = new Object();

    /**
     * 连接服务器
     *
     * @param ip
     */
    private void connectToServer(String ip, int port) {
        if (getAppInfo().getConnectionManager() != null && getAppInfo().getConnectionManager().isConnect()) {
            return;
        }
        mConnectionInfo = new ConnectionInfo(ip, port);
        getAppInfo().setConnectionManager(OkSocket.open(mConnectionInfo));
        if (getAppInfo().getConnectionManager() == null) {
            return;
        }

        mOkOptions = getAppInfo().getConnectionManager().getOption();

        mOkOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
                .setWritePackageBytes(1024 * 1024)// 设置每个包的长度
                .setReaderProtocol(new NormalProtocol())// 设置自定义包头
                .setReadByteOrder(ByteOrder.LITTLE_ENDIAN)// 设置低位在前 高位在后
                .setPulseFrequency(2000)// 设置心跳间隔/毫秒
                .setPulseFeedLoseTimes(2)
                .setReconnectionManager(new NoneReconnect())
                .setCallbackThreadModeToken(new OkSocketOptions.ThreadModeToken() {
                    @Override
                    public void handleCallbackEvent(ActionDispatcher.ActionRunnable actionRunnable) {
                        mHandler.post(actionRunnable);
                    }
                })
                .build();
        getAppInfo().getConnectionManager().option(mOkOptions);
        getAppInfo().getConnectionManager().unRegisterReceiver(mSocketActionAdapter);
        getAppInfo().getConnectionManager().registerReceiver(mSocketActionAdapter);

        synchronized (mLock) {
            if (!getAppInfo().getConnectionManager().isConnect()) {
                getAppInfo().getConnectionManager().connect();
            }
        }
    }

    /**
     * 处理服务端回复的数据
     *
     * @param data 服务端回复的数据
     *
     *             data包括：
     *             header：len (长度)+ (Ornt 协议包头 4)+ flag(标志 4)长度为 8+body.length
     *             body  ：携带的数据
     *             {@link NormalProtocol}
     */
    private void handleResponse(OriginalData data) {
        // 包头
        byte[] header = data.getHeadBytes();
        // 包体
        byte[] body = data.getBodyBytes();
        // 长度
        int len = BytesUtils.bytesToInt(header, 0);
        // 标志
        int flag = BytesUtils.bytesToInt(header, 8);

        switch (flag) {
            case Common.FLAG_LOGIN_RESPONSE:// 登录回执
                boolean isOk = BytesUtils.bytesToInt(body, 0) == 1;
                if (isOk) {
                    // 登录成功 则请求分辨率
                    getAppInfo().getConnectionManager().send(new SelectRectRequest());
                    // 开始投屏
                    getAppInfo().getConnectionManager().send(new StartCastRequest());
                } else {
                    // 登陆失败

                }
                break;
            case Common.FLAG_HEART_BEAT_RESPONSE:// 心跳回执
                // 收到心跳则喂狗
                //Log.e(TAG,"收到心跳回执");
                getAppInfo().getConnectionManager().getPulseManager().feed();
                break;
            case Common.FLAG_MP3_PARAM_REQUEST:
                // mp3参数请求
                getAppInfo().getConnectionManager().send(new Mp3ParamsResponse(AudioConfig.DEFAULT_CHANNEL_COUNT,
                        AudioConfig.DEFAULT_FREQUENCY, AudioConfig.DEFAULT_MP3_SIMPLE_FORMAT));
                break;
            case Common.FLAG_SCREEN_LARGE_SIZE_RESPONSE:
                int large_width = BytesUtils.bytesToInt(body, 0);
                int large_height = BytesUtils.bytesToInt(body, 4);
                //
                WindowFloatManager.getInstance().initScroll(large_width, large_height);
                // 开始投屏
                getAppInfo().getConnectionManager().send(new StartCastRequest());
                WindowFloatManager.getInstance().setLineStartChangeListener(new WindowFloatManager.LineStartChangeListener() {
                    @Override
                    public void onChange(int left, int top, int right, int bottom) {

                        //Log.d(TAG,WindowFloatManager.getInstance().isROTATION_0()+" Change left "+left+" top "+top+" right "+right+" bottom "+bottom);
                        // 发送选中区域
                        getAppInfo().getConnectionManager().send(new SelectRectResponse(left, top, right, bottom));
                    }

                    @Override
                    public void onPrepare(int left, int top, int right, int bottom) {
                        // 发送选中区域
                        //Log.d(TAG,"Prepare  left "+left+" top "+top+" right "+right+" bottom "+bottom);
                        getAppInfo().getConnectionManager().send(new SelectRectResponse(left, top, right, bottom));
                    }
                });

                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity onStart");
        getAppInfo().setActivityRunning(true);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CastMessage busMessage) {
        switch (busMessage.getMessage()) {
            case MESSAGE_ACTION_STREAMING_TRY_START:
                EventBus.getDefault().removeStickyEvent(CastMessage.class);
//                if (!getAppInfo().isWiFiConnected()) {
//                    ToastUtil.showToast("wifi未连接,请连接wifi！");
//                    return;
//                }
                if (!getAppInfo().isServerConnected()) {
                    ToastUtil.showToast("服务器尚未连接！");
                    return;
                }
                if (getAppInfo().isStreamRunning()) return;

                final MediaProjectionManager projectionManager = getProjectionManager();
                if (projectionManager != null) {

                    if (mResultData != null && mResultCode == RESULT_OK) {
                        if (WindowFloatManager.getInstance().isROTATION_0()) {
                            // 重新获取分辨率
                            WindowFloatManager.getInstance().setScreen();
                        } else {
                            WindowFloatManager.getInstance().setHorizontal();
                        }
                        startRecorderScreen();
                    } else {
                        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE);
                    }

                }
                break;
            case MESSAGE_STATUS_TCP_OK:
                EventBus.getDefault().removeStickyEvent(CastMessage.class);

                break;
            case MESSAGE_STATUS_CAST_GENERATOR_ERROR:
                EventBus.getDefault().removeStickyEvent(CastMessage.class);
                EventBus.getDefault().post(new CastMessage(MESSAGE_ACTION_STREAMING_STOP));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConnectMessage message) {
        connectToServer(message.getIp(), message.getPort());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ModeMessage modeMessage) {
        mCurrenMode = modeMessage.getMode();
        switch (modeMessage.getMode()) {
            case Common.CAST_MODE_WIFI:// wifi

                // 关闭miracast
                stopMiracast();
                // 关闭热点
                stopAp();
                // 打开WiFi
                getAppInfo().getWifiManager().setWifiEnabled(true);
                // 重新开启广播搜索
                resetSearcher();
                break;
            case Common.CAST_MODE_MIRACAST:// miracast
                // 关闭热点
                stopAp();
                // 打开WiFi
                getAppInfo().getWifiManager().setWifiEnabled(true);
                //
                if (getAppInfo().isServerConnected()) {
                    getAppInfo().getConnectionManager().disconnect();
                }
                // 如果正在搜索就停止
                stopSearchServer();
                // 开启miracast
                startMiracast();
                break;
            case Common.CAST_MODE_HOTSPOT:// hotspot
                // 关闭miracast
                stopMiracast();
                // 开启热点
                startAp("Caster","12345678");
                // 重新开启广播搜索
                resetSearcher();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MainActivityPermissionsDispatcher.onActivityResult(this, requestCode);
        switch (requestCode) {
            case REQUEST_CODE_SCREEN_CAPTURE:
                if (resultCode != RESULT_OK) {
                    ToastUtil.showToast("获取权限失败");
                    //
                    onBackPressed();
                    return;
                }
                mResultCode = resultCode;
                mResultData = data;
                startRecorderScreen();

                break;
            default:
                Log.e(TAG, "Unknown request code: " + requestCode);
        }
        //
        onBackPressed();
    }

    /**
     * 开始录屏
     */
    private void startRecorderScreen() {
        final MediaProjectionManager projectionManager = CastScreenService.getProjectionManager();
        if (projectionManager == null)
            return;

        final MediaProjection mediaProjection = projectionManager.getMediaProjection(mResultCode, mResultData);
        if (mediaProjection == null)
            return;

        setMediaProjection(mediaProjection);

        EventBus.getDefault().post(new CastMessage(MESSAGE_ACTION_STREAMING_START));
    }

    @NeedsPermission({Manifest.permission.SYSTEM_ALERT_WINDOW})
    void showSystemWindow() {
        //WindowFloatManager.getInstance().setResource(new int[]{R.mipmap.ic_res,R.mipmap.ic_pen,R.mipmap.ic_res,R.mipmap.ic_pen});
        WindowFloatManager.getInstance().showFloatMenus();
        // 回到Home界面
        onBackPressed();
    }

    @NeedsPermission({Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAPTURE_AUDIO_OUTPUT})
    void getRuntimePermission() {

    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(Intent.ACTION_MAIN,null);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        startActivity(intent);

        MainActivity.this.moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG,"onDestroy");
        getAppInfo().setActivityRunning(false);
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mReceiver);
        getAppInfo().getConnectionManager().unRegisterReceiver(mSocketActionAdapter);
        super.onDestroy();
    }
}
