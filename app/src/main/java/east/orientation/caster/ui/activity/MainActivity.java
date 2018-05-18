package east.orientation.caster.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.xuhao.android.libsocket.sdk.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.OkSocket;
import com.xuhao.android.libsocket.sdk.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.SocketActionAdapter;
import com.xuhao.android.libsocket.sdk.bean.IPulseSendable;
import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.sdk.bean.OriginalData;
import com.xuhao.android.libsocket.utils.BytesUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.ByteOrder;

import east.orientation.caster.R;
import east.orientation.caster.cast.CastScreenService;
import east.orientation.caster.cast.Waiter;
import east.orientation.caster.evevtbus.CastMessage;
import east.orientation.caster.evevtbus.ConnectMessage;
import east.orientation.caster.local.AudioConfig;
import east.orientation.caster.local.Common;
import east.orientation.caster.cast.protocol.NormalHeaderProtocol;
import east.orientation.caster.cast.request.LoginRequest;
import east.orientation.caster.cast.request.Mp3ParamsResponse;
import east.orientation.caster.cast.request.Pluse;
import east.orientation.caster.cast.request.SelectRectRequest;
import east.orientation.caster.cast.request.SelectRectResponse;
import east.orientation.caster.cast.request.StartCastRequest;
import east.orientation.caster.util.ToastUtil;
import east.orientation.caster.view.WindowFloatManager;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static com.xuhao.android.libsocket.sdk.OkSocket.open;
import static east.orientation.caster.CastApplication.getAppContext;
import static east.orientation.caster.CastApplication.getAppInfo;
import static east.orientation.caster.cast.CastScreenService.getProjectionManager;
import static east.orientation.caster.cast.CastScreenService.setMediaProjection;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_STOP;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_TRY_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_CAST_GENERATOR_ERROR;

import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_TCP_OK;
import static east.orientation.caster.local.VideoConfig.REQUEST_CODE_SCREEN_CAPTURE;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";

    private Handler mHandler;
    private ConstraintLayout mContent;
    private ImageView mIvTheme;

    private boolean isVertical;
    private int mResultCode;// 请求录屏权限返回的 code
    private Intent mResultData;//请求录屏权限返回的 intent

    // 屏幕旋转监听
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int rotation = WindowFloatManager.getWindowManager().getDefaultDisplay().getRotation();
            Log.e("@@","屏 :"+rotation);
            if (ROTATION_270 == rotation || ROTATION_90 == rotation){
                // 竖屏
                isVertical = true;
                WindowFloatManager.getInstance().setScreen();
            }else if (ROTATION_180 == rotation || ROTATION_0 ==rotation){
                // 横屏
                isVertical = false;
                WindowFloatManager.getInstance().setHorizontal();
            }
        }
    };

    private ConnectionInfo mConnectionInfo;
    private OkSocketOptions mOkOptions;
    private SocketActionAdapter mSocketActionAdapter = new SocketActionAdapter() {
        @Override
        public void onSocketIOThreadStart(Context context, String action) {
            super.onSocketIOThreadStart(context, action);
            Log.e(TAG,"onSocketIOThreadStart");
        }

        @Override
        public void onSocketIOThreadShutdown(Context context, String action, Exception e) {
            super.onSocketIOThreadShutdown(context, action, e);
            Log.e(TAG,"onSocketIOThreadShutdown"+e);
        }

        @Override
        public void onSocketDisconnection(Context context, ConnectionInfo info, String action, Exception e) {
            super.onSocketDisconnection(context, info, action, e);
            // 设置连接状态
            getAppInfo().setServerConnected(false);

            sendBroadcast(new Intent(Common.ACTION_STOP_STREAM));
            Log.e(TAG,"onSocketDisconnection"+e);
        }

        @Override
        public void onSocketConnectionSuccess(Context context, ConnectionInfo info, String action) {
            super.onSocketConnectionSuccess(context, info, action);
            // 设置连接状态
            getAppInfo().setServerConnected(true);
            // 连接成功则发送心跳
            getAppInfo().getConnectionManager().getPulseManager().setPulseSendable(new Pluse(1)).pulse();
            // 连接成功则发送登陆请求
            getAppInfo().getConnectionManager().send(new LoginRequest(Common.LOGIN_TYPE_TEACHER));
            Log.e(TAG,"onSocketConnectionSuccess");
            ToastUtil.show(getAppContext(),"已连接服务器", Toast.LENGTH_LONG,0);
        }

        @Override
        public void onSocketConnectionFailed(Context context, ConnectionInfo info, String action, Exception e) {
            super.onSocketConnectionFailed(context, info, action, e);
            Log.e(TAG,"onSocketConnectionFailed"+e);
        }

        @Override
        public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(context, info, action, data);
            // 处理回执
            handleResponse(data);
        }

        @Override
        public void onSocketWriteResponse(Context context, ConnectionInfo info, String action, ISendable data) {
            super.onSocketWriteResponse(context, info, action, data);
            //Log.e(TAG,"发送 ："+data.parse().length);
        }

        @Override
        public void onPulseSend(Context context, ConnectionInfo info, IPulseSendable data) {
            super.onPulseSend(context, info, data);
            //Log.e(TAG,"心跳已发送");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContent = findViewById(R.id.content);
        mIvTheme = findViewById(R.id.iv_theme);

        // 初始化
        init();
        // 注册eventbus
        EventBus.getDefault().register(this);
        //
        onBackPressed();
    }
    /**
     *
     * 初始化
     */
    private void init() {
        //connectToServer("192.168.0.114",8888);
        Waiter.getInstance().setSearchListener((ip,port) ->{
            EventBus.getDefault().post(new ConnectMessage(ip,port));
        });
        Waiter.getInstance().start();
        registerReceiver(mReceiver,new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }

    /**
     * 连接服务器
     * @param ip
     */
    private void connectToServer(String ip,int port){
        mConnectionInfo = new ConnectionInfo(ip, port);
        getAppInfo().setConnectionManager(OkSocket.open(mConnectionInfo));

        OkSocket.setBackgroundSurvivalTime(-1);

        if (getAppInfo().getConnectionManager() == null) {
            return;
        }

        mOkOptions = getAppInfo().getConnectionManager().getOption();

        mOkOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
                .setWritePackageBytes(1024*1024)// 设置每个包的长度
                .setHeaderProtocol(new NormalHeaderProtocol())// 设置自定义包头
                .setReadByteOrder(ByteOrder.LITTLE_ENDIAN)// 设置低位在前 高位在后
                .setPulseFrequency(30000)// 设置心跳间隔/毫秒
                .build();
        getAppInfo().getConnectionManager().option(mOkOptions);

        getAppInfo().getConnectionManager().unRegisterReceiver(mSocketActionAdapter);
        getAppInfo().getConnectionManager().registerReceiver(mSocketActionAdapter);

        if (!getAppInfo().getConnectionManager().isConnect()) {
            getAppInfo().getConnectionManager().connect();
        }
    }

    /**
     *  处理服务端回复的数据
     *
     *  @param data  服务端回复的数据
     *
     *  data包括：
     *  header：len (长度)+ (Ornt 协议包头 4)+ flag(标志 4)
     *  长度为 8+body.length
     *
     *  body  ：携带的数据
     *  {@link NormalHeaderProtocol}
     */
    private void handleResponse(OriginalData data) {
        // 包头
        byte[] header = data.getHeadBytes();
        // 包体
        byte[] body = data.getBodyBytes();
        // 长度
        int len = BytesUtils.bytesToInt(header,0);
        // 标志
        int flag = BytesUtils.bytesToInt(header,8);

        switch (flag){
            case Common.FLAG_LOGIN_RESPONSE:// 登录回执
                boolean isOk = BytesUtils.bytesToInt(body,0) == 1;
                if (isOk) {
                    // 登录成功 则请求分辨率
                    getAppInfo().getConnectionManager().send(new SelectRectRequest());
                    // 开始投屏
                    getAppInfo().getConnectionManager().send(new StartCastRequest());
                } else{
                    // 登陆失败

                }
                break;
            case Common.FLAG_HEART_BEAT_RESPONSE:// 心跳回执
                // 收到心跳则喂狗
                Log.e(TAG,"收到心跳回执");
                getAppInfo().getConnectionManager().getPulseManager().feed();
                break;
            case Common.FLAG_MP3_PARAM_REQUEST:
                // mp3参数请求
                getAppInfo().getConnectionManager().send(new Mp3ParamsResponse(AudioConfig.DEFAULT_CHANNEL_COUNT,
                        AudioConfig.DEFAULT_FREQUENCY,AudioConfig.DEFAULT_MP3_SIMPLE_FORMAT));
                break;
            case Common.FLAG_SCREEN_LARGE_SIZE_RESPONSE:
                int large_width = BytesUtils.bytesToInt(body,0);
                int large_height = BytesUtils.bytesToInt(body,4);

                WindowFloatManager.getInstance().setLineStartChangeListener(new WindowFloatManager.LineStartChangeListener() {
                    @Override
                    public void onChange(int left, int top, int right, int bottom) {
                        // 发送选中区域
                        getAppInfo().getConnectionManager().send(new SelectRectResponse(left,top,right,bottom));
                    }

                    @Override
                    public void onPrepare(int left, int top, int right, int bottom) {
                        // 发送选中区域
                        getAppInfo().getConnectionManager().send(new SelectRectResponse(left,top,right,bottom));
                    }
                });
                //
                Log.e(TAG,"----initScroll----- ");
                WindowFloatManager.getInstance().initScroll(large_width,large_height);
                // 开始投屏
                getAppInfo().getConnectionManager().send(new StartCastRequest());
                break;
            default:
                break;
        }
    }

    /**
     * 点击事件
     *
     * @param v
     */
    public void onBtnClick(View v) {
        switch (v.getId()){
            case R.id.btn_draw:// 画板
                startActivity(new Intent(this,WriteActivity.class));
                break;
            case R.id.btn_res:// 资源
                startActivity(new Intent(this,ResActivity.class));
                break;
            case R.id.btn_castToLarge:// 投到大屏幕
                CastMessage stickyEvent = EventBus.getDefault().getStickyEvent(CastMessage.class);
                if (stickyEvent == null || MESSAGE_STATUS_TCP_OK.equals(stickyEvent.getMessage())) {
                    EventBus.getDefault().postSticky(new CastMessage(MESSAGE_ACTION_STREAMING_TRY_START));
                }
                break;
            case R.id.btn_castToAll:// 投到所有学生
                ToastUtil.show(this,"开发ing !");
                break;
            case R.id.btn_setting:// 设置
                startActivity(new Intent(this,SettingActivity.class));
                break;
            case R.id.btn_castStuScreen: // 投射某个学生的画面
                ToastUtil.show(this,"开发ing !");
                break;
            case R.id.btn_exit:// 退出
                getAppContext().AppExit();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,"MainActivity onStart");

        getAppInfo().setActivityRunning(true);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CastMessage busMessage) {
        switch (busMessage.getMessage()) {
            case MESSAGE_ACTION_STREAMING_TRY_START:
                EventBus.getDefault().removeStickyEvent(CastMessage.class);
                if (!getAppInfo().isWiFiConnected()) {
                    ToastUtil.show(getApplicationContext(),"wifi未连接,请连接wifi！");
                    return;
                }
                if(getAppInfo().isStreamRunning()) return;

                final MediaProjectionManager projectionManager = getProjectionManager();
                if (projectionManager != null) {

                    if(mResultData != null && mResultCode == RESULT_OK){
                        if (isVertical){
                            // 重新获取分辨率
                            WindowFloatManager.getInstance().setScreen();
                        }else {
                            WindowFloatManager.getInstance().setHorizontal();
                        }
                        startRecorderScreen();
                    }else {
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
    public void onEvent(ConnectMessage message){
        connectToServer(message.getIp(),message.getPort());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCREEN_CAPTURE:
                if (resultCode != RESULT_OK) {
                    ToastUtil.show(this, "获取权限失败");
                    return;
                }
                mResultCode = resultCode;
                mResultData = data;
                startRecorderScreen();

                break;
            default:
                Log.e(TAG,"Unknown request code: " + requestCode);
        }
    }

    /**
     * 开始录屏
     *
     */
    private void startRecorderScreen(){
        final MediaProjectionManager projectionManager = CastScreenService.getProjectionManager();
        if (projectionManager == null)
            return;

        final MediaProjection mediaProjection = projectionManager.getMediaProjection(mResultCode, mResultData);
        if (mediaProjection == null)
            return;

        setMediaProjection(mediaProjection);

        EventBus.getDefault().post(new CastMessage(MESSAGE_ACTION_STREAMING_START));
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        getAppInfo().setActivityRunning(false);
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
