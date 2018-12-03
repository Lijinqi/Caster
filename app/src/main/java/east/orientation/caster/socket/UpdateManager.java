package east.orientation.caster.socket;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.LinkedBlockingQueue;


import east.orientation.caster.R;
import east.orientation.caster.socket.request.AppFiledownRequest;
import east.orientation.caster.socket.request.AppsQueryRequest;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by ljq on 2018/11/5.
 */

public class UpdateManager {
    private static final String TAG = "UpdateManager";

    public static final String TYPE_APK = "0";
    public static final int DEFAULT_PACKAGE_SIZE = 1024 * 1024;
    public static final String CMD_QUERY_UPDATE = "filequery_type";
    public static final String CMD_DOWNLOAD_UPDATE = "filedown_type";

    private static final int FLAG_HAVE_UPDATE = 1;
    private static final int FLAG_NOT_HAVE_UPDATE = 2;
    private static final int FLAG_DOWNLOAD_START = 3;
    private static final int FLAG_DOWNLOAD_PROGRESS = 4;
    private static final int FLAG_DOWNLOAD_END = 5;
    private static final int DEFAULT_NOTIFY_ID = 11;
    private static final String DEFAULT_CHANNEL_ID = "Apk_update";
    private static final String DEFAULT_CHANNEL_NAME = "更新";

    private static Apk sApk = new Apk();
    private static long sOffset;

    private static NotificationManager sNotificationManager;
    private static NotificationCompat.Builder sNotificationBuilder;
    private static Handler sHandler;
    private static WriteToFileThread sWriteToFileThread;
    private static UpdateAdapter sUpdateAdapter;
    private static DownloadAdapter sDownloadAdapter;

    private UpdateManager() {

    }

    public static UpdateManager getInstance() {
        return Load.INSTANCE;
    }

    private static class Load {
        private static final UpdateManager INSTANCE = new UpdateManager();
    }

    public static void init(Context context, UpdateAdapter updateAdapter, DownloadAdapter downloadAdapter) {
        sUpdateAdapter = updateAdapter;
        sDownloadAdapter = downloadAdapter;

        sHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case FLAG_HAVE_UPDATE:
                        Apk apk = (Apk) msg.obj;
                        // 有更新
                        if (sUpdateAdapter != null) sUpdateAdapter.update(apk);
                        break;
                    case FLAG_NOT_HAVE_UPDATE:
                        // 无更新
                        if (sUpdateAdapter != null) sUpdateAdapter.noUpdate();
                        break;
                    case FLAG_DOWNLOAD_START:
                        // 开始下载
                        String apkName = (String) msg.obj;
                        if (sDownloadAdapter != null) sDownloadAdapter.start(apkName);
                        break;
                    case FLAG_DOWNLOAD_PROGRESS:
                        // 下载中
                        DownloadInfo downloadInfo = (DownloadInfo) msg.obj;
                        if (sDownloadAdapter != null)
                            sDownloadAdapter.progress(downloadInfo.offset, downloadInfo.size);
                        break;
                    case FLAG_DOWNLOAD_END:
                        // 下载完成
                        String filePath = (String) msg.obj;
                        if (sDownloadAdapter != null) sDownloadAdapter.finish(filePath);
                        break;
                }
            }
        };

        sNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, context.getString(R.string.app_name) + DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            sNotificationManager.createNotificationChannel(channel);
        }
    }

    public static void getUpdate(String type, String appKey) {
        SocketManager.getInstance().send(new AppsQueryRequest(type, appKey));
    }

    public static void download(String type, String apkName, long offset, int length) {
        if (sOffset == 0) {// offset 为0 则表示是第一次下载
            sHandler.obtainMessage(FLAG_DOWNLOAD_START, apkName).sendToTarget();
        }
        SocketManager.getInstance().send(new AppFiledownRequest(type, apkName, offset, length));
    }

    public static void handleUpdate(boolean isSuccess, String response) {
        if (isSuccess) {
            // 第3个逗号的位置
            int indexT = response.indexOf(",", response.indexOf(",", response.indexOf(",") + 1) + 1);
            if (indexT == response.length() - 1) {// 判断是否有列表
                // 没有新的apk
                Log.i(TAG, "no apk update");
                sHandler.obtainMessage(FLAG_NOT_HAVE_UPDATE).sendToTarget();

            } else {
                // 新的apk文件
                String listStr = response.substring(indexT + 1, response.length());

                String[] array = listStr.split(";");

                String[] arrayContent = array[0].split(",");
                if (arrayContent.length < 3) {
                    sHandler.obtainMessage(FLAG_NOT_HAVE_UPDATE).sendToTarget();
                    return;
                }
                sApk.apkName = arrayContent[0];
                String version = sApk.apkName.substring(sApk.apkName.indexOf("_") + 1, sApk.apkName.indexOf("."));
                sApk.versionCode = Integer.valueOf(version);
                sApk.apkSize = arrayContent[1];
                sApk.apkTime = arrayContent[2];

                sHandler.obtainMessage(FLAG_HAVE_UPDATE, sApk).sendToTarget();
            }
        } else {
            Log.e(TAG, "apk update receive error");
        }
    }

    public static void handleDownload(boolean isSuccess, String response, byte[] bytes) {
        if (isSuccess) {
            int indexS = response.indexOf(",", response.indexOf(",") + 1); // 第二个逗号的位置
            if (indexS < 0 || indexS == response.length() - 1) {// 判断是否有列表
                // 没有新的apk
                Log.i(TAG, "apk download no list");
            } else {
                try {
                    int a0 = response.indexOf(",");
                    int a1 = response.indexOf(",", a0 + 1);
                    int a2 = response.indexOf(",", a1 + 1);
                    int a3 = response.indexOf(",", a2 + 1);
                    int a4 = response.indexOf(",", a3 + 1);
                    int a5 = response.indexOf(",", a4 + 1);
                    int a6 = response.indexOf(",", a5 + 1);

                    DownloadInfo downloadInfo = new DownloadInfo();
                    downloadInfo.type = response.substring(a1 + 1, a2);
                    downloadInfo.fileName = response.substring(a2 + 1, a3);
                    downloadInfo.size = Long.valueOf(response.substring(a3 + 1, a4));
                    downloadInfo.offset = Long.valueOf(response.substring(a4 + 1, a5));
                    downloadInfo.length = Integer.valueOf(response.substring(a5 + 1, a6));

                    String head = response.substring(0, a6 + 1);
                    Log.i(TAG, head);
                    int len = head.getBytes("gbk").length;

                    int cLen = bytes.length - len;
                    byte[] content = new byte[cLen];
                    System.arraycopy(bytes, bytes.length - cLen, content, 0, cLen);

                    downloadInfo.content = content;

                    // 如果offset等于0,这是第一次 启动写入文件线程
                    if (sOffset == 0) {
                        sWriteToFileThread = new WriteToFileThread(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), downloadInfo.fileName, downloadInfo.size);
                        sWriteToFileThread.start();
                    }
                    // offset 增加本次获取长度
                    sOffset += downloadInfo.content.length;
                    downloadInfo.offset = sOffset;

                    Log.d(TAG, "offset - " + sOffset + "-" + downloadInfo.content.length);

                    // 加入队列
                    if (downloadInfo.content.length > 0)
                        sWriteToFileThread.add(downloadInfo.content);

                    // 通知下载进度
                    if (sOffset < downloadInfo.size) {// offset 不等于总长则还没下载完成

                        download(TYPE_APK, downloadInfo.fileName, sOffset, DEFAULT_PACKAGE_SIZE);
                        sHandler.obtainMessage(FLAG_DOWNLOAD_PROGRESS, downloadInfo).sendToTarget();

                    } else if (sOffset == downloadInfo.size) {// offset 等于总长则下载完成

                        sHandler.obtainMessage(FLAG_DOWNLOAD_PROGRESS, downloadInfo).sendToTarget();
                        File file = new File(sWriteToFileThread.getDownloadPath(), downloadInfo.fileName);
                        sHandler.obtainMessage(FLAG_DOWNLOAD_END, file.getAbsolutePath()).sendToTarget();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(TAG, "download receive error");
        }
    }

    public static void createNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        sNotificationBuilder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID);
        Notification notification = sNotificationBuilder
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle("下载更新中...")
                .setSmallIcon(R.mipmap.app_icon)
                .setProgress(100, 0, false)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .build();
        manager.notify(DEFAULT_NOTIFY_ID, notification);
    }

    public static void updateNotification(long progress, long totalSize) {
        int percent = (int) (progress * 100 / totalSize);
        sNotificationBuilder.setProgress(100, percent, false);
        sNotificationManager.notify(DEFAULT_NOTIFY_ID, sNotificationBuilder.build());
    }

    public static void finishNotification(Context context, File file) {
        sNotificationBuilder.setContentTitle("下载完成");

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这里加入flag
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, "east.orientation.caster.provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        sNotificationBuilder.setContentIntent(resultPendingIntent);
        sNotificationManager.notify(DEFAULT_NOTIFY_ID, sNotificationBuilder.build());
    }

    public static void install(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这里加入flag
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, "east.orientation.caster.provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这里加入flag
        intent.setDataAndType(uri, "application/vnd.android.package-archive");

        context.startActivity(intent);

    }

    public static void cancelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sNotificationManager.deleteNotificationChannel(DEFAULT_CHANNEL_ID);
        sNotificationManager.cancel(DEFAULT_NOTIFY_ID);
    }

    public static void onCancel() {
        sWriteToFileThread.shutdown();
        sUpdateAdapter = null;
        sDownloadAdapter = null;
        sHandler.removeCallbacksAndMessages(null);
        sOffset = 0;
    }

    public interface UpdateAdapter {
        void update(Apk apk);

        void noUpdate();
    }

    public interface DownloadAdapter {
        void start(String apkName);

        void progress(long progress, long totalSize);

        void finish(String apkPath);
    }

    public static class Apk {
        String apkName;
        String apkSize;
        String apkTime;
        int versionCode;

        public String getApkName() {
            return apkName;
        }

        public String getApkSize() {
            return apkSize;
        }

        public String getApkTime() {
            return apkTime;
        }

        public int getVersionCode() {
            return versionCode;
        }

        @Override
        public String toString() {
            return apkName + "-" + apkSize + "-" + apkTime + "-" + versionCode;
        }
    }

    public static class DownloadInfo {
        String type;
        String fileName;
        long size;
        long offset;
        int length;
        byte[] content;

        @Override
        public String toString() {
            return type + "-" + fileName + "-" + size + "-" + offset + "-" + length + "-" + content;
        }
    }

    public static class WriteToFileThread implements Runnable {
        public Thread mThread;
        private FileOutputStream mFileOutputStream;
        private String downloadPath;
        private String fileName;
        private long totalSize;
        private long receiveSize;
        private boolean isStop;
        private LinkedBlockingQueue<byte[]> mQueue = new LinkedBlockingQueue<>();

        public WriteToFileThread(String downloadPath, String fileName, long fileSize) {
            this.downloadPath = downloadPath;
            this.fileName = fileName;
            this.totalSize = fileSize;
            isStop = true;
        }

        public synchronized void start() {
            if (isStop) {
                try {
                    mThread = new Thread(this);
                    File file = new File(downloadPath, fileName);
                    if (createFileByDeleteOldFile(file))
                        mFileOutputStream = new FileOutputStream(file);
                    isStop = false;
                    mThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                while (!isStop) {
                    byte[] data = mQueue.take();
                    mFileOutputStream.write(data, 0, data.length);
                    mFileOutputStream.flush();
                    receiveSize += data.length;

                    Log.i(TAG, receiveSize + " <-> " + totalSize);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "error " + e);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "error " + e);
            } finally {
                try {
                    if (mFileOutputStream != null)
                        mFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void add(byte[] bytes) {
            mQueue.add(bytes);
        }

        public String getDownloadPath() {
            return downloadPath;
        }

        public synchronized void shutdown() {
            if (mThread != null && !isStop) {
                isStop = true;
                mThread.interrupt();
                mThread = null;
                mQueue.clear();
            }
        }

        public boolean isShutdown() {
            return isStop;
        }

        /**
         * 判断文件是否存在，存在则在创建之前删除
         *
         * @param file 文件
         * @return {@code true}: 创建成功<br>{@code false}: 创建失败
         */
        public static boolean createFileByDeleteOldFile(File file) {
            if (file == null) {
                return false;
            }
            // 文件存在并且删除失败返回false
            if (file.exists() && file.isFile() && !file.delete()) {
                return false;
            }
            // 创建目录失败返回false
            if (!createOrExistsDir(file.getParentFile())) {
                return false;
            }
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 判断目录是否存在，不存在则判断是否创建成功
         *
         * @param file 文件
         * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
         */
        public static boolean createOrExistsDir(File file) {
            // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
            return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
        }
    }
}
