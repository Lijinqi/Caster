package east.orientation.caster.cast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import east.orientation.caster.CastApplication;
import east.orientation.caster.R;

import east.orientation.caster.evevtbus.CastMessage;
import east.orientation.caster.local.Common;
import east.orientation.caster.ui.MainActivity;
import east.orientation.caster.util.ToastUtil;
import east.orientation.caster.view.WindowFloatManager;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static com.xuhao.android.libsocket.sdk.OkSocket.open;
import static east.orientation.caster.CastApplication.getAppInfo;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_STOP;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_STREAMING_TRY_START;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_ACTION_TCP_RESTART;
import static east.orientation.caster.evevtbus.CastMessage.MESSAGE_STATUS_TCP_OK;
import static east.orientation.caster.local.Common.NOTIFICATION_START_STREAMING;
import static east.orientation.caster.local.Common.NOTIFICATION_STOP_STREAMING;


/**
 * Created by ljq on 2018/1/9.
 *
 * 投屏服务
 */

public class CastScreenService extends Service {
    private static final String TAG = "CastScreenService";

    private static CastScreenService sServiceInstance;

    private Context mAppContext;

    private MediaProjectionManager mMediaProjectionManager;

    private MediaProjection mMediaProjection;

    private MediaProjection.Callback mProjectionCallback;

    private Handler mHandler;

    private HandlerThread mHandlerThread;

    private BroadcastReceiver mLocalNotificationReceiver;

    private BroadcastReceiver mBroadcastReceiver;

    private boolean isServicePrepared;

    private CastServiceHandler mCastServiceHandler;

    private NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "录屏服务启动");
        sServiceInstance = this;
        mAppContext = getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Common.NOTIFICATION_CHANNEL_ID, "cast service", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mProjectionCallback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                serviceStopStreaming();
            }
        };

        getAppInfo().getScreenVideoStream().clear();
        getAppInfo().getAudioStream().clear();

        // Starting thread Handler 开启handler线程
        mHandlerThread = new HandlerThread(
                CastScreenService.class.getSimpleName(),
                Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mHandlerThread.start();
        mCastServiceHandler = new CastServiceHandler(mHandlerThread.getLooper());

        //Local notifications
        IntentFilter localNotificationIntentFilter = new IntentFilter();
        localNotificationIntentFilter.addAction(Common.ACTION_START_STREAM);
        localNotificationIntentFilter.addAction(Common.ACTION_STOP_STREAM);
        localNotificationIntentFilter.addAction(Common.ACTION_EXIT);

        mLocalNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final String action = intent.getAction();
                switch (action) {
                    case Common.ACTION_START_STREAM:
                        if (getAppInfo().isServerConnected()) {
                            if (!getAppInfo().isActivityRunning()) {
                                // 打开主页面
                                startActivity(new Intent(context, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                            CastMessage stickyEvent = EventBus.getDefault().getStickyEvent(CastMessage.class);
                            if (stickyEvent == null || MESSAGE_STATUS_TCP_OK.equals(stickyEvent.getMessage())) {
                                EventBus.getDefault().postSticky(new CastMessage(MESSAGE_ACTION_STREAMING_TRY_START));
                            }
                            Log.d(TAG, "start stream");
                        }else {
                            ToastUtil.show(mAppContext,"未连接服务器,请连接服务器！");
                        }

                        break;
                    case Common.ACTION_STOP_STREAM:
                        Log.d(TAG, "stop stream");
                        serviceStopStreaming();
                        break;
                    case Common.ACTION_EXIT:
                        Log.d(TAG, "exit app");
                        stopService();
                        CastApplication.getAppContext().AppExit();

                        break;
                }
            }
        };
        registerReceiver(mLocalNotificationReceiver, localNotificationIntentFilter);

        // Registering receiver for WiFi changes
        IntentFilter screenOnOffAndWiFiFilter = new IntentFilter();
        screenOnOffAndWiFiFilter.addAction(ACTION_SCREEN_OFF);
        screenOnOffAndWiFiFilter.addAction(WIFI_STATE_CHANGED_ACTION);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                switch (action){
                    case ACTION_SCREEN_OFF:
                        serviceStopStreaming();
                        break;
                    case WIFI_STATE_CHANGED_ACTION:
                        //获取当前的wifi状态int类型数据
                        int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                        switch (mWifiState ) {
                            case WifiManager.WIFI_STATE_ENABLED:
                                //已打开

                                break;
                            case WifiManager.WIFI_STATE_ENABLING:
                                //打开中
                                if (getAppInfo().getConnectionManager() != null ){
                                    getAppInfo().getConnectionManager().connect();
                                }
                                break;
                            case WifiManager.WIFI_STATE_DISABLED:
                                //已关闭
                                ToastUtil.show(getApplicationContext(),"WIFI未连接！");
                                if (getAppInfo().isStreamRunning()){
                                    serviceStopStreaming();
                                }
                                break;
                            case WifiManager.WIFI_STATE_DISABLING:
                                //关闭中
                                getAppInfo().getConnectionManager().disConnect();
                                break;
                            case WifiManager.WIFI_STATE_UNKNOWN:
                                //未知

                                break;
                        }
                        break;
                }
            }
        };
        registerReceiver(mBroadcastReceiver, screenOnOffAndWiFiFilter);

        // 显示悬浮框
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //WindowFloatManager.getInstance().setResource(new int[]{R.mipmap.ic_res,R.mipmap.ic_pen,R.mipmap.ic_res,R.mipmap.ic_pen});
                WindowFloatManager.getInstance().showFloatMenus();
            }
        },500);

        // 开启发送帧数据线程
        FrameSender.start();

        EventBus.getDefault().register(this);
    }

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CastScreenService.class)
                .putExtra(Common.EXTRA_DATA, Common.SERVICE_MESSAGE_PREPARE_STREAMING);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isServicePrepared) {
            startForeground(NOTIFICATION_START_STREAMING, getCustomNotification(NOTIFICATION_START_STREAMING));
        }
        isServicePrepared = true;

        return START_NOT_STICKY;
    }

    /**
     * 开始录屏
     */
    private void serviceStartStreaming() {
        if (getAppInfo().isStreamRunning()) return;
        stopForeground(true);
        mCastServiceHandler.obtainMessage(mCastServiceHandler.HANDLER_START_STREAMING).sendToTarget();
        startForeground(NOTIFICATION_STOP_STREAMING, getCustomNotification(NOTIFICATION_STOP_STREAMING));
        if (mMediaProjection != null) mMediaProjection.registerCallback(mProjectionCallback, null);
    }

    /**
     * 停止录屏
     */
    private void serviceStopStreaming() {
        if (!getAppInfo().isStreamRunning()) return;
        //stopForeground(true);
        mCastServiceHandler.obtainMessage(CastServiceHandler.HANDLER_STOP_STREAMING).sendToTarget();
        startForeground(NOTIFICATION_START_STREAMING, getCustomNotification(NOTIFICATION_START_STREAMING));
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
            mMediaProjection.stop();
        }
        getAppInfo().getScreenVideoStream().clear();
        getAppInfo().getAudioStream().clear();
    }


    @Subscribe
    public void onMessageEvent(CastMessage busMessage) {
        switch (busMessage.getMessage()) {
            case MESSAGE_ACTION_STREAMING_START:
                serviceStartStreaming();
                break;
            case MESSAGE_ACTION_STREAMING_STOP:
                serviceStopStreaming();
                break;
            case MESSAGE_ACTION_TCP_RESTART:
                // TODO tcp server restart

                break;
            default:
                break;
        }
    }

    /**
     * 创建 Notification
     *
     * @param notificationType 类型 start stop
     * @return 返回 notification 对象
     */
    private Notification getCustomNotification(int notificationType) {
        PendingIntent pendingIntent = PendingIntent.getActivity(mAppContext, 0, new Intent(mAppContext, MainActivity.class)/*.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)*/, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mAppContext, Common.NOTIFICATION_CHANNEL_ID);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setCategory(Notification.CATEGORY_SERVICE);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setSmallIcon(R.mipmap.app_launcher);
        builder.setWhen(0);

        switch (notificationType) {
            case NOTIFICATION_START_STREAMING:// 开启
                PendingIntent startIntent = PendingIntent.getBroadcast(this, 0,
                        new Intent(Common.ACTION_START_STREAM),
                        0);


                PendingIntent exitIntent = PendingIntent.getBroadcast(this, 1,
                        new Intent(Common.ACTION_EXIT),
                        0);


                RemoteViews smallView_start = new RemoteViews(getPackageName(), R.layout.start_notification_small);
                smallView_start.setOnClickPendingIntent(R.id.linearLayoutStartNotificationSmall, pendingIntent);
                smallView_start.setImageViewResource(R.id.imageViewStartNotificationSmallIconMain, R.mipmap.app_launcher);
                smallView_start.setImageViewResource(R.id.imageViewStartNotificationSmallIconStart, R.drawable.ic_service_start_24dp);
                smallView_start.setOnClickPendingIntent(R.id.imageViewStartNotificationSmallIconStart, startIntent);
                builder.setCustomContentView(smallView_start);

                RemoteViews bigView_start = new RemoteViews(getPackageName(), R.layout.start_notification_big);
                bigView_start.setOnClickPendingIntent(R.id.linearLayoutStartNotificationBig, pendingIntent);
                bigView_start.setImageViewResource(R.id.imageViewStartNotificationBigIconMain, R.mipmap.app_launcher);
                bigView_start.setImageViewResource(R.id.imageViewStartNotificationBigIconStart, R.drawable.ic_service_start_24dp);
                bigView_start.setImageViewResource(R.id.imageViewStartNotificationBigIconExit, R.drawable.ic_service_exit_24dp);
                bigView_start.setOnClickPendingIntent(R.id.linearLayoutStartNotificationBigStart, startIntent);
                bigView_start.setOnClickPendingIntent(R.id.linearLayoutStartNotificationBigExit, exitIntent);
                builder.setCustomBigContentView(bigView_start);
                break;

            case NOTIFICATION_STOP_STREAMING:// 停止
                PendingIntent stopIntent = PendingIntent.getBroadcast(mAppContext, 2,
                        new Intent(Common.ACTION_STOP_STREAM),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                RemoteViews smallView_stop = new RemoteViews(getPackageName(), R.layout.stop_notification_small);
                smallView_stop.setOnClickPendingIntent(R.id.linearLayoutStopNotificationSmall, pendingIntent);
                smallView_stop.setImageViewResource(R.id.imageViewStopNotificationSmallIconMain, R.mipmap.app_launcher);
                smallView_stop.setImageViewResource(R.id.imageViewStopNotificationSmallIconStop, R.drawable.ic_service_stop_24dp);
                smallView_stop.setOnClickPendingIntent(R.id.imageViewStopNotificationSmallIconStop, stopIntent);
                builder.setCustomContentView(smallView_stop);

                RemoteViews bigView_stop = new RemoteViews(getPackageName(), R.layout.stop_notification_big);
                bigView_stop.setOnClickPendingIntent(R.id.linearLayoutStopNotificationBig, pendingIntent);
                bigView_stop.setImageViewResource(R.id.imageViewStopNotificationBigIconMain, R.mipmap.app_launcher);
                bigView_stop.setImageViewResource(R.id.imageViewStopNotificationBigIconStop, R.drawable.ic_service_stop_24dp);
                bigView_stop.setOnClickPendingIntent(R.id.linearLayoutStopNotificationBigStop, stopIntent);
                builder.setCustomBigContentView(bigView_stop);
                break;

            default:
                Log.e(TAG, "no define type");
                break;
        }

        return builder.build();
    }

    public static void stopService() {
        Log.d(TAG, "录屏服务结束");
        sServiceInstance.stopSelf();
    }

    public static void setMediaProjection(final MediaProjection mediaProjection) {
        sServiceInstance.mMediaProjection = mediaProjection;
    }

    @Nullable
    public static MediaProjectionManager getProjectionManager() {
        return sServiceInstance == null ? null : sServiceInstance.mMediaProjectionManager;
    }

    @Nullable
    public static MediaProjection getMediaProjection() {
        return sServiceInstance == null ? null : sServiceInstance.mMediaProjection;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mLocalNotificationReceiver);
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
            mMediaProjection.stop();
        }
        mHandlerThread.quit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.deleteNotificationChannel(Common.NOTIFICATION_CHANNEL_ID);
        }

        FrameSender.stop();

        if (getAppInfo().getConnectionManager() != null){
            getAppInfo().getConnectionManager().disConnect();
        }

        EventBus.getDefault().unregister(this);
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }
}
