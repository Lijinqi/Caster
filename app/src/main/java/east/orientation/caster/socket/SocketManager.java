package east.orientation.caster.socket;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import java.nio.ByteOrder;

import east.orientation.caster.socket.request.StringRequest;


/**
 * Created by ljq on 2018/8/3.
 */

public class SocketManager {
    private static final String TAG = "SocketManager";
    private boolean isInit;

    private static final String IP = "119.23.238.102";//"192.168.0.142" "119.23.238.102"
    private static final int PORT = 8888;
    private HandlerThread sHandlerThread;
    private ResponseHandler sResponseHandler;
    private SocketListener SocketListener;
    private IConnectionManager mConnectionManager;
    private ConnectionInfo mConnectionInfo;
    private OkSocketOptions mOkOptions;
    private SocketActionAdapter mSocketActionAdapter = new SocketActionAdapter() {
        @Override
        public void onSocketIOThreadStart(String action) {
            super.onSocketIOThreadStart(action);
            Log.d(TAG, "onSocketIOThreadStart");
        }

        @Override
        public void onSocketIOThreadShutdown(String action, Exception e) {
            super.onSocketIOThreadShutdown(action, e);
            Log.d(TAG, "onSocketIOThreadShutdown" + e);
        }

        @Override
        public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
            super.onSocketDisconnection(info, action, e);
            if (SocketListener != null) SocketListener.disconnected();
            Log.d(TAG, "onSocketDisconnection" + e);
        }

        @Override
        public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
            super.onSocketConnectionSuccess(info, action);
            if (SocketListener != null) SocketListener.connected();
            // 连接成功则发送心跳
            //mConnectionManager.getPulseManager().setPulseSendable(new Pluse(1)).pulse();
            Log.d(TAG, "onSocketConnectionSuccess");

        }

        @Override
        public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
            super.onSocketConnectionFailed(info, action, e);
            if (SocketListener != null) SocketListener.connectFailed();
            Log.d(TAG, "onSocketConnectionFailed" + e);
        }

        @Override
        public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(info, action, data);
            byte[] bytes = data.getBodyBytes();
            //Log.d(TAG, "receive size:" + bytes.length);
            sResponseHandler.obtainMessage(ResponseHandler.SUCCESS_RESPONSE, bytes).sendToTarget();
        }

        @Override
        public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
            super.onSocketWriteResponse(info, action, data);
            //Log.d(TAG,"发送 ："+data.parse().length);

        }

        @Override
        public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
            super.onPulseSend(info, data);
            //Log.d(TAG,"心跳已发送");
        }
    };

    private SocketManager() {

    }

    public static SocketManager getInstance() {
        return Load.INSTANCE;
    }

    private static class Load {
        private static final SocketManager INSTANCE = new SocketManager();
    }

    public void init() {
        // 连接信息
        mConnectionInfo = new ConnectionInfo(IP, PORT);
        // 连接参数配置
        mOkOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
                .setWritePackageBytes(1024 * 1024)// 设置每个包的长度
                .setReadByteOrder(ByteOrder.LITTLE_ENDIAN)// 设置低位在前 高位在后
                .build();
        // 开启通道
        mConnectionManager = OkSocket.open(mConnectionInfo);
        // 设置当前连接的参数配置
        mConnectionManager.option(mOkOptions);
        // 注册回调
        mConnectionManager.registerReceiver(mSocketActionAdapter);
        // 发起连接
        if (!mConnectionManager.isConnect()) {
            mConnectionManager.connect();
        }

        // Starting thread Handler 开启handler线程
        sHandlerThread = new HandlerThread(SocketManager.class.getSimpleName(), Process.THREAD_PRIORITY_MORE_FAVORABLE);
        sHandlerThread.start();
        sResponseHandler = new ResponseHandler(sHandlerThread.getLooper());

        isInit = true;
    }

    public boolean isInit() {
        return isInit;
    }

    public void registerSocketListener(SocketListener socketListener) {
        SocketListener = socketListener;
    }

    public void unregisterSocketListener() {
        if (SocketListener != null) SocketListener = null;
    }

    public void send(StringRequest request) {
        if (mConnectionManager != null)
            mConnectionManager.send(request);
    }

    public boolean isConnect() {
        if (mConnectionManager != null)
            return mConnectionManager.isConnect();
        return false;
    }

    public void disConnect() {
        if (mConnectionManager != null) {
            mConnectionManager.disconnect();
            mConnectionManager.unRegisterReceiver(mSocketActionAdapter);
        }
    }

    public void connect() {
        if (mConnectionManager != null)
            mConnectionManager.connect();
    }
}
