package east.orientation.caster.ui;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.xuhao.android.libsocket.sdk.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.SocketActionAdapter;
import com.xuhao.android.libsocket.sdk.bean.IPulseSendable;
import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.sdk.bean.OriginalData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import east.orientation.caster.R;
import east.orientation.caster.cast.CastScreenService;
import east.orientation.caster.evevtbus.CastMessage;
import east.orientation.caster.util.ToastUtil;

import static com.xuhao.android.libsocket.sdk.OkSocket.open;
import static east.orientation.caster.CastApplication.getAppInfo;
import static east.orientation.caster.cast.CastScreenService.getProjectionManager;
import static east.orientation.caster.cast.CastScreenService.setMediaProjection;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_STOP;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_TRY_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_CAST_GENERATOR_ERROR;

import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_TCP_OK;
import static east.orientation.caster.local.Common.REQUEST_CODE_SCREEN_CAPTURE;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";

    private ConstraintLayout mContent;
    private ImageView mIvTheme;

    private int mResultCode;// 请求录屏权限返回的 code
    private Intent mResultData;//请求录屏权限返回的 intent

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
            Log.e(TAG,"onSocketIOThreadShutdown");
        }

        @Override
        public void onSocketDisconnection(Context context, ConnectionInfo info, String action, Exception e) {
            super.onSocketDisconnection(context, info, action, e);
            getAppInfo().setServerConnected(false);
            Log.e(TAG,"断开连接");
        }

        @Override
        public void onSocketConnectionSuccess(Context context, ConnectionInfo info, String action) {
            super.onSocketConnectionSuccess(context, info, action);
            getAppInfo().setServerConnected(true);

            Log.e(TAG,"连接成功");
        }

        @Override
        public void onSocketConnectionFailed(Context context, ConnectionInfo info, String action, Exception e) {
            super.onSocketConnectionFailed(context, info, action, e);
            Log.e(TAG,"连接失败");
        }

        @Override
        public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(context, info, action, data);
        }

        @Override
        public void onSocketWriteResponse(Context context, ConnectionInfo info, String action, ISendable data) {
            super.onSocketWriteResponse(context, info, action, data);
        }

        @Override
        public void onPulseSend(Context context, ConnectionInfo info, IPulseSendable data) {
            super.onPulseSend(context, info, data);
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
    }
    /**
     *
     * 初始化
     */
    private void init() {
        mConnectionInfo = new ConnectionInfo("192.168.0.101", 28888);

        mOkOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
                .setSinglePackageBytes(1024)
                .setBackgroundLiveMinute(-1)// 后台久活
                .build();
        getAppInfo().setConnectionManager(open(mConnectionInfo, mOkOptions));

        if (getAppInfo().getConnectionManager() == null) {
            return;
        }
        getAppInfo().getConnectionManager().unRegisterReceiver(mSocketActionAdapter);
        getAppInfo().getConnectionManager().registerReceiver(mSocketActionAdapter);

        if (!getAppInfo().getConnectionManager().isConnect()) {
            getAppInfo().getConnectionManager().connect();
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
                ToastUtil.show(this,"开发ing !");
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
                System.exit(0);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
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

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        getAppInfo().setActivityRunning(false);
        super.onStop();
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
}
