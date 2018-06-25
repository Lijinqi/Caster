package east.orientation.caster.sync;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import east.orientation.caster.evevtbus.SyncMessage;
import east.orientation.caster.evevtbus.UpdateFilesMessage;
import east.orientation.caster.local.Common;
import east.orientation.caster.soket.Client;
import east.orientation.caster.soket.SocketTransceiver;
import east.orientation.caster.util.FileUtil;
import east.orientation.caster.util.SharePreferenceUtil;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/6/5.
 */

public class SyncService extends Service {
    private static final String TAG = "SyncService";
    private static final int DEFAULT_RECONNECT_DELAY = 5*1000;
    private boolean isLogining;
    private String mAccount;
    private String mPassword;

    private BroadcastReceiver mNetChangeReceiver;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private Client mClient;
    private SocketTransceiver mSocketTransceiver;
    // 用于存储需要下载文件列表
    private LinkedBlockingQueue<String> mDownloadingQueue = new LinkedBlockingQueue<>();
    // 用于存储正在下载文件字节
    private LinkedBlockingQueue<byte[]> mCurrentDownloadQueue = new LinkedBlockingQueue<>();
    // 是否上传中 上传文件时不能传输其他请求
    private boolean isUploading;
    // 需要上传队列
    private LinkedBlockingQueue<String> mUploadFileQueue = new LinkedBlockingQueue<>();
    // 上传中队列
    private LinkedBlockingQueue<String> mUploadingQueue = new LinkedBlockingQueue<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 添加全局引用
        getAppInfo().setSyncService(this);
        // 注册eventbus
        EventBus.getDefault().register(this);
        // 开启handler线程
        mHandlerThread = new HandlerThread(SyncService.class.getSimpleName(), Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case Common.CMD_LOGIN_ID://登录
                        if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){
                            // 标记已登录
                            isLogining = true;
                            SharePreferenceUtil.put(getApplicationContext(),Common.KEY_ACCOUNT, mAccount,Common.KEY_PASSWD, mPassword);
                            // 把未上传成功的文件加入需要上传的队列
                            mUploadFileQueue.addAll(mUploadingQueue);
                            // 清空正在上传队列
                            mUploadingQueue.clear();
                        }else {

                        }
                        break;
                    case Common.CMD_FILE_UP_ID://上传
                        if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){
                            // 上传成功则去除已上传的
                            mUploadingQueue.poll();
                        }else {

                        }
                        break;
                    case Common.CMD_FILE_QUERY_ID://获取文件列表
                        if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){


                            Log.e(TAG,"获取成功");
                        }else {


                            Log.e(TAG,"获取失败");
                        }
                        break;
                    case Common.CMD_FILE_DOWN_ID://文件下载
                        if (Common.CMD_RESPONSE_SUCCESS == msg.arg1 && Common.CMD_FILE_DOWN_HEAD == msg.arg2){
                            // 下载请求第一次返回
                            Bundle bundle = (Bundle) msg.obj;
                            String fileName = bundle.getString("fileName");
                            int fileSize = bundle.getInt("fileSize");
                            byte[] content = bundle.getByteArray("content");

                            mCurrentDownloadQueue.add(content);
                            new DownloadThread(fileName,fileSize).start();
                        }else if (Common.CMD_RESPONSE_SUCCESS == msg.arg1 && Common.CMD_FILE_DOWN_CONTENT == msg.arg2){
                            // 下载请求返回 第n次 n>1
                            byte[] bytes = (byte[]) msg.obj;
                            mCurrentDownloadQueue.add(bytes);
                        }else if (Common.CMD_RESPONSE_SUCCESS == msg.arg1 &&Common.CMD_FILE_DOWN_FINISH == msg.arg2){
                            // 下载完成
                            File file = (File) msg.obj;
                            fileUpdated_syn(file.getName());
                            // 执行下载操作 完成后 更新页面
                            EventBus.getDefault().post(new UpdateFilesMessage());
                        }else if(Common.CMD_RESPONSE_SUCCESS == msg.arg1 &&Common.CMD_FILE_DOWN_ERROR == msg.arg2){
                            // 传输断开 下载失败 则删除未传输完成文件
                            File file = (File) msg.obj;
                            if (FileUtil.delete(file.getAbsolutePath())){
                                Log.e(TAG,"删除未完成文件 成功");
                            }
                        }

                        break;
                    case Common.CMD_FILE_DEL_ID:
                        if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){

                            Log.e(TAG,"删除成功");
                        }else {


                            Log.e(TAG,"删除失败");
                        }
                        break;

                    case Common.RECONNECT_ID:
                        if (mClient != null && !mClient.isConnected()){
                            mClient.connect(Common.SYNC_SERVER_IP,Common.SYNC_SERVER_PORT);
                        }
                        break;
                }
            }
        };
        // 获取账号密码
        getProviderLogin();
        // 连接服务
        connectToService();
        // 网络变化监听
        initReceiver();
        // 获取退出应用前未上传完成文件队列
        getSaveUploadQueue();
        // 开启上传线程
        new UploadThread().start();
    }

    private void getSaveUploadQueue() {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        if (SharePreferenceUtil.get(getApplicationContext(),Common.KEY_UPLOAD_QUQUE,queue) != null){
            //
            mUploadingQueue.addAll((Collection<? extends String>) SharePreferenceUtil.get(getApplicationContext(),Common.KEY_UPLOAD_QUQUE,queue));
            Log.e(TAG,mUploadingQueue.size()+"--"+mUploadingQueue);
        }
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 监听wifi的打开与关闭，与wifi的连接无关
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    Log.e("TAG", "wifiState:" + wifiState);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLED:

                            break;
                        case WifiManager.WIFI_STATE_DISABLING:

                            // 断开
                            disConnect();

                            break;
                    }
                }
                // 监听wifi的连接状态即是否连上了一个有效无线路由
                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    Parcelable parcelableExtra = intent
                            .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (null != parcelableExtra) {
                        // 获取联网状态的NetWorkInfo对象
                        NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                        //获取的State对象则代表着连接成功与否等状态
                        NetworkInfo.State state = networkInfo.getState();
                        //判断网络是否已经连接
                        boolean isConnected = state == NetworkInfo.State.CONNECTED;
                        Log.e("TAG", "isConnected:" + isConnected);
                        if (isConnected) {
                            // 重连
                            reConnectDelay();
                        }else {
                            // 断开
                            disConnect();
                        }
                    }
                }
                // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    //获取联网状态的NetworkInfo对象
                    NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (info != null) {
                        //如果当前的网络连接成功并且网络连接可用
                        if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                            if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                                // 重连
                                reConnectDelay();
                            }else {
                                // 断开
                                disConnect();
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(mNetChangeReceiver,intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    private void connectToService(){
        mClient = new Client() {
            @Override
            public void onConnect(SocketTransceiver transceiver) {
                mSocketTransceiver = transceiver;
                // 连接则登录
                login();
                // 开启轮询
                startTimer();
                Log.e(TAG,"onConnect");
            }

            @Override
            public void onConnectFailed() {
                // 重连
                reConnectDelay();
                Log.e(TAG,"onConnectFailed");
            }

            @Override
            public void onReceive(SocketTransceiver transceiver, byte[] bytes) {
                Log.e(TAG,"onReceive");
                handleResponse(bytes);
            }

            @Override
            public void onDisconnect(SocketTransceiver transceiver) {
                isLogining = false;

                // 关闭轮询
                stopTimer();
                // 重连
                reConnectDelay();
                Log.e(TAG,"onDisconnect");
            }
        };
        mClient.connect(Common.SYNC_SERVER_IP,Common.SYNC_SERVER_PORT);  //192.168.0.139//119.23.238.102
    }

    private void reConnectDelay(){
        mHandler.sendEmptyMessageDelayed(Common.RECONNECT_ID, DEFAULT_RECONNECT_DELAY);
    }

    private void disConnect(){
        if (mClient != null && mClient.isConnected()){
            mClient.disconnect();
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 不再上传中，则可以发送查询请求
                if (!isUploading){
                    // 查询
                    fileQuery_syn();
                    //fileQuery();
                }
            }
        };
        mTimer.schedule(mTimerTask,1000,10*1000);
    }

    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    /**
     *  处理服务端回复的数据
     */
    private void handleResponse(byte[] bytes) {
        try {
            if (Common.HEAD.equals(new String(bytes,0,4,"gbk"))){
                String response;
                byte zero = 0;
                if (zero == bytes[bytes.length-1]){
                    response = new String(bytes,0,bytes.length-1,"gbk");
                }else {
                    response = new String(bytes,0,bytes.length,"gbk");
                }

                String cmd = response.substring(response.indexOf("=")+1,response.indexOf(","));
                int index = response.indexOf("=" , response.indexOf("=")+1)+1;
                String isOk = response.substring(index , index+1);
                Log.e(TAG,cmd+ " - response :"+response);
                switch (cmd){
                    case Common.CMD_LOGIN://登录
                        if ("1".equals(isOk)){
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_LOGIN_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_LOGIN_ID,Common.CMD_RESPONSE_FAILED,0,response));
                        }
                        break;
                    case Common.CMD_FILE_UP://上传
                        if ("1".equals(isOk)){
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_UP_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_UP_ID,Common.CMD_RESPONSE_FAILED,0,response));
                        }
                        break;
                    case Common.CMD_FILE_QUERY://获取文件列表
                        if ("1".equals(isOk)){

                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_QUERY_ID,Common.CMD_RESPONSE_SUCCESS,0,response));

                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_QUERY_ID,Common.CMD_RESPONSE_FAILED,0,response));
                        }
                        break;
                    case Common.CMD_FILE_DOWN://文件下载
                        if ("1".equals(isOk)){
                            // 下载返回 第一次
                            String[] array = response.split(",");
                            String fileName = array[2];
                            int fileSize = Integer.valueOf(array[3]);

                            int indexs = response.indexOf(",",response.indexOf(",",response.indexOf(",",response.indexOf(",")+1)+1)+1)+1;
                            String head = response.substring(0,indexs);
                            int len = head.getBytes("gbk").length;

                            int cLen = bytes.length-len;
                            byte[] content = new byte[cLen];
                            System.arraycopy(bytes,bytes.length-cLen,content,0,cLen);
                            Log.e(TAG," content len "+content.length);

                            Bundle bundle = new Bundle();
                            bundle.putString("fileName",fileName);
                            bundle.putInt("fileSize",fileSize);
                            bundle.putByteArray("content",content);
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_SUCCESS,Common.CMD_FILE_DOWN_HEAD,bundle));

                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_FAILED,0,bytes));
                        }
                        break;
                    case Common.CMD_FILE_DEL:
                        // 删除
                        if ("1".equals(isOk)){
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DEL_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DEL_ID,Common.CMD_RESPONSE_FAILED,0,response));
                        }
                        break;
                    case Common.CMD_FILE_QUERY_SYN:
                        //
                        if ("1".equals(isOk)){

                            int indexS = response.indexOf(",",response.indexOf(",")+1); // 第二个逗号的位置
                            if (indexS == response.length()-1){// 判断是否有列表
                                mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_QUERY_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
                                break;
                            }
                            String listStr = response.substring(indexS+1,response.length()-1);
                            String[] array = listStr.split(";");
                            for (int i = 0; i < array.length; i++) {
                                String[] arrayContent = array[i].split(",");
                                String fileName = arrayContent[0];
                                if (fileName != null && !TextUtils.isEmpty(fileName)){
                                    if (fileName.startsWith("P_")){
                                        if (fileName.startsWith("P_Del_")){
                                            // 删除
                                            File file = new File(Common.DEFAULT_DIR+File.separator+mAccount+File.separator+fileName.substring("P_Del_".length()));
                                            if (file.exists()){
                                                // 删除本地文件
                                                if (FileUtil.delete(file.getAbsolutePath())){
                                                    // 删除成功 通知服务器
                                                    fileDel_syn(file.getName());
                                                    // 执行删除操作后 更新页面
                                                    EventBus.getDefault().post(new UpdateFilesMessage());
                                                }else {
                                                    Log.e(TAG,fileName.substring("P_Del_".length())+"删除失败 !");
                                                }
                                            }else {
                                                // 不存在 当做已删除 通知服务器
                                                fileDel_syn(file.getName());
                                                Log.e(TAG,fileName.substring("P_Del_".length())+"不存在 !");
                                            }
                                        }else {
                                            // TODO 下载
                                            fileDown(fileName);
                                        }
                                    }
                                }
                            }
                        }else {

                        }
                        break;
                    case Common.CMD_FILE_DEL_SYN:
                        //
                        if ("1".equals(isOk)){

                        }else {

                        }
                        break;
                    case Common.CMD_FILE_UPDATED_SYN:
                        //
                        if ("1".equals(isOk)){

                        }else {

                        }
                        break;
                }
            }else {
                mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_SUCCESS,Common.CMD_FILE_DOWN_CONTENT,bytes));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void getProviderLogin(){
        Uri user_uri = Uri.parse("content://east.orientation.newlanucher/user");
        //
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(user_uri,new String[]{"_id,account,password"},null,null,null);

        if (cursor==null) return;

        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String account = cursor.getString(1);
            String password = cursor.getString(2);
            Log.e(TAG,"query user: "+id+"-" +account+"-"+password);
            mAccount = account;
            mPassword = password;
            SharePreferenceUtil.put(this,Common.KEY_ACCOUNT,mAccount,Common.KEY_PASSWD,mPassword);
        }
        cursor.close();
    }

    private void login(){
        if (!mClient.isConnected() || isLogining) return;
        String login = SyncSender.login(mAccount,mPassword);
        if (mClient.isConnected()){
            mSocketTransceiver.send(login);
        }
    }

    private void fileQuery(){
        if (!isLogining || !mClient.isConnected()) return;
        String fileQuery = SyncSender.filequery();
        if (mClient.isConnected()){
            mSocketTransceiver.send(fileQuery);
        }
    }

    private void fileUp(File file, int fileSize){
        if (!isLogining || !mClient.isConnected()) return;
        String fileUp = SyncSender.fileup(file.getName());
        if (mClient.isConnected()){
            mSocketTransceiver.send(fileUp,fileSize);
        }

    }

    private void fileDown(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String fileDown = SyncSender.filedown(fileName);
        if (mClient.isConnected()){

            mSocketTransceiver.send(fileDown);
        }
    }

    private void fileDel(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String fileDel = SyncSender.filedel(fileName);
        if (mClient.isConnected()){
            Observable.create(e -> {
                mSocketTransceiver.send(fileDel);
            }).subscribeOn(Schedulers.io()).subscribe();
//            mSocketTransceiver.send(fileDel);
        }
    }

    private void fileQuery_syn(){
        if (!isLogining || !mClient.isConnected()) return;
        String filequery_syn = SyncSender.filequery_syn();
        if (mClient.isConnected()){

            mSocketTransceiver.send(filequery_syn);
        }
    }

    private void fileDel_syn(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String filedel_syn = SyncSender.filedel_syn(fileName);
        if (mClient.isConnected()){

            mSocketTransceiver.send(filedel_syn);
        }
    }

    private void fileUpdated_syn(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String fileupdated_syn = SyncSender.fileupdated_syn(fileName);
        if (mClient.isConnected()){

            mSocketTransceiver.send(fileupdated_syn);
        }
    }

    /*上传线程*/
    class UploadThread extends Thread{

        private String mFilePath;
        private File mFile;
        private List<File> mFiles = new ArrayList<>();
        private FileInputStream mFileInputStream;

        @Override
        public void run() {
            try {
                while (true){
                    // 取文件地址
                    mFilePath = mUploadFileQueue.take();
                    // 加入正在上传队列
                    mUploadingQueue.put(mFilePath);
                    if (mFilePath != null){
                        mFile = new File(mFilePath);
                        if (mFile.isDirectory()){
                            // 清空集合
                            mFiles.clear();
                            // 获取文件夹中文件
                            searchFiles(mFile);
                            //
                            for (File file:mFiles){
                                mFileInputStream = new FileInputStream(file);
                                int fileSize = (int) file.length();
                                Log.e(TAG,"fileSize: "+fileSize);
                                isUploading = true;
                                // 上传命令
                                fileUp(file,fileSize);

                                byte[] buf = new byte[1024*1024];
                                int count;

                                while ((count = mFileInputStream.read(buf)) != -1){

                                    Log.e(TAG,fileSize+"buf count "+count);
                                    byte[] content = new byte[count];
                                    System.arraycopy(buf,0,content,0,count);
                                    if (mClient.isConnected()){
                                        mSocketTransceiver.send(content);
                                    }
                                }
                            }
                            isUploading = false;
                        }else {
                            mFileInputStream = new FileInputStream(mFile);
                            int fileSize = (int) mFile.length();
                            Log.e(TAG,"fileSize: "+fileSize);
                            isUploading = true;
                            // 上传命令
                            fileUp(mFile,fileSize);

                            byte[] buf = new byte[1024*1024];
                            int count;

                            while ((count = mFileInputStream.read(buf)) != -1){

                                Log.e(TAG,fileSize+"buf count "+count);
                                byte[] content = new byte[count];
                                System.arraycopy(buf,0,content,0,count);
                                if (mClient.isConnected()){
                                    mSocketTransceiver.send(content);
                                }
                            }
                            isUploading = false;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void searchFiles(File file) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isDirectory()) {
                    searchFiles(f);
                } else {
                    mFiles.add(f);
                }
            }
        }

    }

    /*下载线程*/
    class DownloadThread extends Thread{

        private FileOutputStream mFileOutputStream;

        private String mFileName;
        private int mFileSize;
        public DownloadThread(String fileName, int fileSize){
            this.mFileName = fileName.substring("P_".length());
            this.mFileSize = fileSize;
        }

        @Override
        public void run() {
            try {
                int count =0;
                getAlbumStorageDir(Common.SAVE_DIR_NAME+File.separator+mAccount);
                File file = new File(Common.DEFAULT_DIR + File.separator + mAccount+ File.separator + mFileName);
                mFileOutputStream = new FileOutputStream(file);
                while (count < mFileSize && mClient.isConnected()){
                    byte[] bytes = mCurrentDownloadQueue.take();
                    if (bytes == null){
                        Log.e(TAG,"bytes is null"+mCurrentDownloadQueue.size());
                        continue;
                    }
                    mFileOutputStream.write(bytes);
                    count += bytes.length;
                    Log.e(TAG,"count "+count+" bytes: "+bytes.length);
                }
                if (count == mFileSize){
                    // TODO 下载完成 更新
                    mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_SUCCESS,Common.CMD_FILE_DOWN_FINISH,file));
                }else {
                    // TODO 异常停止传输
                    mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_SUCCESS,Common.CMD_FILE_DOWN_ERROR,file));
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"error "+e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 找到download文件夹
         * @param albumName
         * @return
         */
        public File getAlbumStorageDir(String albumName) {
            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), albumName);
            if (!file.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
            return file;
        }
    }

    @Subscribe()
    public void onMessageEvent(SyncMessage syncMessage) {
        Object object = syncMessage.getObject();

        switch (syncMessage.getAction()){
            case Common.CMD_LOGIN:
                // 登录
                login();
                break;
            case Common.CMD_FILE_UP:
                // 上传
                Log.e(TAG,"path "+(String)object);
                mUploadFileQueue.add((String)object);

                break;
            case Common.CMD_FILE_QUERY:
                // 查询
                fileQuery();
                break;
            case Common.CMD_FILE_DOWN:
                // 下载
                fileDown((String)object);
                break;
            case Common.CMD_FILE_DEL:
                // 删除
                fileDel((String) object);
                break;
            case Common.CMD_FILE_QUERY_SYN:
                //

                break;
            case Common.CMD_FILE_DEL_SYN:
                //

                break;
            case Common.CMD_FILE_UPDATED_SYN:
                //

                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        // 存储未上传完成的文件
        SharePreferenceUtil.put(getApplicationContext(),Common.KEY_UPLOAD_QUQUE,mUploadingQueue);

        mHandlerThread.quit();
        // 关闭轮询
        stopTimer();
        Log.e(TAG,"onDestroy");
        // 注销广播
        unregisterReceiver(mNetChangeReceiver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
