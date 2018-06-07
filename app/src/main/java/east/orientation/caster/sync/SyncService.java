package east.orientation.caster.sync;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import east.orientation.caster.evevtbus.SyncMessage;
import east.orientation.caster.local.Bean.SyncFileBean;
import east.orientation.caster.local.Common;
import east.orientation.caster.soket.Client;
import east.orientation.caster.soket.SocketTransceiver;
import east.orientation.caster.util.SharePreferenceUtil;
import east.orientation.caster.util.ToastUtil;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ljq on 2018/6/5.
 */

public class SyncService extends Service {
    private static final String TAG = "SyncService";
    private boolean isLogining;
    private String mAccount;
    private String mPassword;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private Client mClient;
    private SocketTransceiver mSocketTransceiver;
    private LinkedBlockingQueue<byte[]> mCurrentDownloadQueue = new LinkedBlockingQueue<>();
    private List<SyncFileBean> mFiles = new ArrayList<>();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //
        EventBus.getDefault().register(this);
        // 获取账号密码
        getProviderLogin();
        // 连接服务
        connectToService();
        // Starting thread Handler 开启handler线程
        mHandlerThread = new HandlerThread(
                SyncService.class.getSimpleName(),
                Process.THREAD_PRIORITY_MORE_FAVORABLE);
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
                            SharePreferenceUtil.put(SyncService.this,Common.KEY_ACCOUNT, mAccount,Common.KEY_PASSWD, mPassword);

                        }else {

                        }
                        break;
                    case Common.CMD_FILE_UP_ID://上传
                        if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){

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
                            Bundle bundle = (Bundle) msg.obj;
                            String fileName = bundle.getString("fileName");
                            int fileSize = bundle.getInt("fileSize");
                            byte[] content = bundle.getByteArray("content");

                            mCurrentDownloadQueue.add(content);
                            new DownloadThread(fileName,fileSize).start();
                        }else if (Common.CMD_RESPONSE_SUCCESS == msg.arg1 && Common.CMD_FILE_DOWN_CONTENT == msg.arg2){
                            // 下载返回 第n次 n>1
                            byte[] bytes = (byte[]) msg.obj;
                            mCurrentDownloadQueue.add(bytes);
                        }else if (Common.CMD_FILE_DOWN_FINISH == msg.arg2){

                            String filePath = (String) msg.obj;

                        }

                        break;
                }
            }
        };
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

                Log.e(TAG,"onConnectFailed");
            }

            @Override
            public void onReceive(SocketTransceiver transceiver, byte[] bytes) {
                Log.e(TAG,"onReceive");
                handleResponse(bytes);
            }

            @Override
            public void onDisconnect(SocketTransceiver transceiver) {
                Log.e(TAG,"onDisconnect");
            }
        };
        mClient.connect(Common.SYNC_SERVER_IP,Common.SYNC_SERVER_PORT);  //192.168.0.139//119.23.238.102
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 查询
                fileQuery_syn();
                //fileQuery();
            }
        };
        mTimer.schedule(mTimerTask,1000,10*1000);
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
                            //String response = (String) msg.obj;
                            mFiles.clear();
                            int indexS = response.indexOf(",",response.indexOf(",")+1); // 第二个逗号的位置
                            if (indexS == response.length()-1){// 判断是否有列表
                                mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_QUERY_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
                                break;
                            }
                            String listStr = response.substring(indexS+1,response.length()-1);
                            String[] array = listStr.split(";");
                            for (int i = 0; i < array.length; i++) {
                                String[] arrayContent = array[i].split(",");
                                SyncFileBean bean = new SyncFileBean();

                                bean.setName(arrayContent[0]);
                                bean.setLength(Long.valueOf(arrayContent[1]));
                                bean.setTime(arrayContent[2]);
                                bean.setDownLoad(false);
                                mFiles.add(bean);
                            }
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

                        break;
                    case Common.CMD_FILE_QUERY_SYN:
                        //
                        if ("1".equals(isOk)){

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
            Observable.create(e ->  {
                mSocketTransceiver.send(login);
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private void fileQuery(){
        if (!isLogining || !mClient.isConnected()) return;
        String fileQuery = SyncSender.filequery();
        if (mClient.isConnected()){
            Observable.create(e ->  {
                mSocketTransceiver.send(fileQuery);
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private void fileUp(File file, int fileSize){
        if (!isLogining || !mClient.isConnected()) return;
        String fileUp = SyncSender.fileup(file.getName());
        if (mClient.isConnected()){
            Observable.create(e -> {
                mSocketTransceiver.send(fileUp,fileSize);
            }).subscribeOn(Schedulers.io()).subscribe();
        }

    }

    private void fileDown(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String fileDown = SyncSender.filedown(fileName);
        if (mClient.isConnected()){
            Observable.create(e -> {
                mSocketTransceiver.send(fileDown);
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private void fileDel(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String fileDel = SyncSender.filedel(fileName);
        if (mClient.isConnected()){
            Observable.create(e -> {
                mSocketTransceiver.send(fileDel);
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private void fileQuery_syn(){
        if (!isLogining || !mClient.isConnected()) return;
        String filequery_syn = SyncSender.filequery_syn();
        if (mClient.isConnected()){
            Observable.create(e -> {
                Log.e(TAG,"send fileQuery_syn");
                mSocketTransceiver.send(filequery_syn);
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private void fileDel_syn(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String filedel_syn = SyncSender.filedel_syn(fileName);
        if (mClient.isConnected()){
            Observable.create(e -> {

                mSocketTransceiver.send(filedel_syn);
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private void fileUpdated_syn(String fileName){
        if (!isLogining || !mClient.isConnected()) return;
        String fileupdated_syn = SyncSender.fileupdated_syn(fileName);
        if (mClient.isConnected()){
            Observable.create(e -> {

                mSocketTransceiver.send(fileupdated_syn);
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    /*上传线程*/
    class UploadThread extends Thread{

        private String mFilePath;
        private File mFile;
        private List<File> mFiles = new ArrayList<>();
        private FileInputStream mFileInputStream;
        public UploadThread(String filePath){
            mFilePath = filePath;
        }


        @Override
        public void run() {
            try {
                mFile = new File(mFilePath);

                if (mFile.isDirectory()){
                    // 获取文件夹中文件
                    searchFiles(mFile);
                    //
                    for (File file:mFiles){
                        mFileInputStream = new FileInputStream(file);
                        int fileSize = (int) file.length();
                        Log.e(TAG,"fileSize: "+fileSize);
                        // 上传命令
                        fileUp(file,fileSize);

                        byte[] buf = new byte[1024*1024];
                        int count;

                        while ((count = mFileInputStream.read(buf)) != -1){
                            Log.e(TAG,fileSize+"buf count "+count);
                            byte[] content = new byte[count];
                            System.arraycopy(buf,0,content,0,count);
                            if (mClient.isConnected()){
                                Observable.create(e1->{
                                    mSocketTransceiver.send(content);
                                }).subscribeOn(Schedulers.io()).subscribe();
                            }
                        }
                    }
                }else {
                    mFileInputStream = new FileInputStream(mFile);
                    int fileSize = (int) mFile.length();
                    Log.e(TAG,"fileSize: "+fileSize);
                    // 上传命令
                    fileUp(mFile,fileSize);

                    byte[] buf = new byte[1024*1024];
                    int count;

                    while ((count = mFileInputStream.read(buf)) != -1){
                        Log.e(TAG,fileSize+"buf count "+count);
                        byte[] content = new byte[count];
                        System.arraycopy(buf,0,content,0,count);
                        if (mClient.isConnected()){
                            Observable.create(e1->{
                                mSocketTransceiver.send(content);
                            }).subscribeOn(Schedulers.io()).subscribe();
                        }
                    }
                }
            } catch (IOException e) {
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
            this.mFileName = fileName;
            this.mFileSize = fileSize;
        }

        @Override
        public void run() {
            try {
                int count =0;
                getAlbumStorageDir(Common.SAVE_DIR_NAME);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+
                        File.separator + Common.SAVE_DIR_NAME + File.separator + mFileName);
                mFileOutputStream = new FileOutputStream(file);
                while (count < mFileSize){
                    byte[] bytes = mCurrentDownloadQueue.poll();
                    if (bytes == null){
                        Log.e(TAG,"bytes is null"+mCurrentDownloadQueue.size());
                        continue;
                    }
                    mFileOutputStream.write(bytes);
                    count += bytes.length;
                    Log.e(TAG,"count "+count+" bytes: "+bytes.length);
                }
                // TODO 下载完成 更新

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"error "+e);
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

    @Subscribe
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
                new UploadThread((String)object).start();
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
            default:break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        Log.e(TAG,"onDestroy");
        EventBus.getDefault().unregister(this);
    }
}
