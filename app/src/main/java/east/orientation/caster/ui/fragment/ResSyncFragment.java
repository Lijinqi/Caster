package east.orientation.caster.ui.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import east.orientation.caster.R;
import east.orientation.caster.local.Common;
import east.orientation.caster.util.CommonUtil;
import east.orientation.caster.util.FileUtil;
import east.orientation.caster.util.ToastUtil;

import static east.orientation.caster.local.Common.DEFAULT_DIR;

/**
 * Created by ljq on 2018/4/24.
 *
 * 同步资源
 */

public class ResSyncFragment extends BaseFragment{
    private static final String TAG = "ResSyncFragment";
    private AlertDialog mDialog;
    private String mAccount;
    private String mPassword;
    private boolean isLogining;

    private ImageView mBackView;
    private TextView mTvUser;
    private SwipeRefreshLayout mFileRefreshLayout;
    private RecyclerView mFileRecyclerView;
    private CommonAdapter<File> mFileAdapter;
    private List<File> mFileList = new ArrayList<>();
    private File[] mFiles ;
    private String mCurrentPath;// 当前路径
    private Stack<String> mCurrentPathStack = new Stack<>();// 路径栈
//    private SwipeRefreshLayout mRefreshLayout;
//    private RecyclerView mRecyclerView;
//    private CommonAdapter<SyncFileBean> mAdapter;
//    private List<SyncFileBean> mFiles = new ArrayList<>();

    private LinkedBlockingQueue<byte[]> mCurrentDownloadQueue = new LinkedBlockingQueue<>();

//    private Handler mHandler = new Handler(Looper.getMainLooper()){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case Common.CMD_LOGIN_ID://登录
//                    if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){
//                        ToastUtil.show(getContext(),"登录成功");
//                        SharePreferenceUtil.put(getContext(),Common.KEY_ACCOUNT, mAccount,Common.KEY_PASSWD, mPassword);
//                        mTvUser.setText(mAccount);
//                        isLogining = true;
//                        if (mDialog !=null) mDialog.dismiss();
//                        //refreshList();
//                    }else {
//                        ToastUtil.show(getContext(),"登录失败");
//                    }
//                    break;
//                case Common.CMD_FILE_UP_ID://上传
//                    if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){
//                        ToastUtil.show(getContext(),"上传成功");
//                    }else {
//                        ToastUtil.show(getContext(),"上传失败");
//                    }
//                    break;
//                case Common.CMD_FILE_QUERY_ID://获取文件列表
//                    if (Common.CMD_RESPONSE_SUCCESS == msg.arg1){
//
//                        //mAdapter.notifyDataSetChanged();
//                        ToastUtil.show(getContext(),"获取成功");
//                        Log.e(TAG,"获取成功");
//                    }else {
//
//                        ToastUtil.show(getContext(),"获取失败");
//                        Log.e(TAG,"获取失败");
//                    }
//                    //mRefreshLayout.setRefreshing(false);
//                    break;
//                case Common.CMD_FILE_DOWN_ID://文件下载
//                    if (Common.CMD_RESPONSE_SUCCESS == msg.arg1 && Common.CMD_FILE_DOWN_HEAD == msg.arg2){
//                        Bundle bundle = (Bundle) msg.obj;
//                        String fileName = bundle.getString("fileName");
//                        int fileSize = bundle.getInt("fileSize");
//                        byte[] content = bundle.getByteArray("content");
//
//                        mCurrentDownloadQueue.add(content);
//                        new DownloadThread(fileName,fileSize).start();
//                    }else if (Common.CMD_RESPONSE_SUCCESS == msg.arg1 && Common.CMD_FILE_DOWN_CONTENT == msg.arg2){
//                        // 下载返回 第n次 n>1
//                        byte[] bytes = (byte[]) msg.obj;
//                        mCurrentDownloadQueue.add(bytes);
//                    }else if (Common.CMD_FILE_DOWN_FINISH == msg.arg2){
//                        ToastUtil.show(getContext(),"下载完成");
//                        String filePath = (String) msg.obj;
//
//                        FileUtil.openFiles(getContext(),filePath);
//                    }
//
//                    break;
//            }
//        }
//    };
//
//    private Client mClient;
//    private SocketTransceiver mSocketTransceiver;

    public static ResSyncFragment newInstance() {
        Bundle args = new Bundle();

        ResSyncFragment fragment = new ResSyncFragment();
        fragment.setArguments(args);
        return fragment;
    }

//    void getSaveLogin(){
//        Uri user_uri = Uri.parse("content://east.orientation.newlanucher/user");
//        //
//        ContentResolver resolver = getContext().getContentResolver();
//        Cursor cursor = resolver.query(user_uri,new String[]{"_id,account,password"},null,null,null);
//
//        if (cursor==null) return;
//
//        while (cursor.moveToNext()){
//            int id = cursor.getInt(0);
//            String account = cursor.getString(1);
//            String password = cursor.getString(2);
//            Log.e(TAG,"query user: "+id+"-" +account+"-"+password);
//            mAccount = account;
//            mPassword = password;
//            SharePreferenceUtil.put(getContext(),Common.KEY_ACCOUNT,mAccount,Common.KEY_PASSWD,mPassword);
//        }
//        cursor.close();
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        // 连接
//        connectToService();
//        // 获取外部账号密码
//        getSaveLogin();
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始路径
        mCurrentPath = DEFAULT_DIR;
        // 加入队列
        mCurrentPathStack.add(mCurrentPath);

        updateFiles(mCurrentPath);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_res_sync,container,false);
        mBackView = view.findViewById(R.id.iv_back);
        mTvUser = view.findViewById(R.id.tv_user);
        mFileRefreshLayout = view.findViewById(R.id.srl_file_list);
        mFileRecyclerView = view.findViewById(R.id.rv_file_list);

        // 初始化视图
        initView();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //checkForLogin();
    }

    private void initView() {
        mBackView.setOnClickListener(view ->onBackPressed());
        mFileRefreshLayout.setOnRefreshListener(()->{
            updateFiles(mCurrentPath);
            mFileRefreshLayout.setRefreshing(false);
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mFileRecyclerView.setLayoutManager(layoutManager);
        mFileAdapter = new CommonAdapter<File>(getContext(),R.layout.layout_file_list,mFileList) {
            @Override
            protected void convert(ViewHolder holder, File file, int position) {
                if (file.isDirectory()){
                    // 文件夹
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.folder);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("audio")){
                    // 音频
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.audio);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("application/vnd.ms-excel")){
                    // excel 表格
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.excel);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("image")){
                    // 图片
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.image);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("video")){
                    // 视频
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.video);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("application/vnd.ms-powerpoint")){
                    // ppt
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.ppt);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("application/pdf")){
                    // pdf
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.pdf);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("text/plain")){
                    // txt
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.txt);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("application/msword")){
                    // word
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.word);
                }else if (FileUtil.getMIMEType(file.getName()).startsWith("application/zip")){
                    // zip
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.zip);
                }else {
                    holder.setImageResource(R.id.item_file_icon,R.mipmap.ic_doc_generic_am);
                }
                // 名称
                holder.setText(R.id.item_file_name,file.getName());
                // 大小
                holder.setText(R.id.item_file_size, FileUtil.getFormatFileSize(file));
                // 修改日期
                holder.setText(R.id.item_file_date,FileUtil.getTime(file.lastModified()));
            }
        };
        mFileAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (mFileList.get(position).isDirectory()){
                    mCurrentPathStack.add(mFileList.get(position).getAbsolutePath());
                    updateFiles(mFileList.get(position).getAbsolutePath());
                }else {
                    FileUtil.openFiles(getContext(),mFileList.get(position).getAbsolutePath());
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                // TODO 长按删除

                return false;
            }
        });

        mFileRecyclerView.setAdapter(mFileAdapter);

//        mRefreshLayout.setOnRefreshListener(()->{
//            fileQuery();
//        });
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        mRecyclerView.setLayoutManager(linearLayoutManager);
//        mAdapter = new CommonAdapter<SyncFileBean>(getContext(),R.layout.layout_sync_item,mFiles) {
//            @Override
//            protected void convert(ViewHolder holder, SyncFileBean syncFileBean, int position) {
//                holder.setText(R.id.item_sync_name,syncFileBean.getName());
//                holder.setText(R.id.item_sync_len,syncFileBean.getLength());
//                holder.setText(R.id.item_sync_date,syncFileBean.getTime());
//            }
//        };
//        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
//                // item 点击
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+
//                        File.separator + Common.SAVE_DIR_NAME + File.separator + mFiles.get(position).getName());
//                //if (file.exists()){
//                //   FileUtil.openFiles(getContext(),file.getPath());
//                //}else {
//                    ToastUtil.show(getContext(),"开始下载");
//                    // 下载命令
//                    fileDown(mFiles.get(position).getName());
//
//               //}
//            }
//
//            @Override
//            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
//                return false;
//            }
//        });
//        mRecyclerView.setAdapter(mAdapter);

    }

    private void updateFiles(String filePath){
        mCurrentPath = filePath;
        File file = new File(filePath);
        if (file.exists() ){
            if (file.isDirectory()){
                mFiles = file.listFiles();
            }
        }else {
            ToastUtil.showToast("文件夹不存在");
        }
        if (mFiles == null) return;

        mFileList.clear();
        for (File f:mFiles ){
            mFileList.add(f);
        }
        if (mFileAdapter != null){
            mFileAdapter.notifyDataSetChanged();
        }
    }

//    private void checkForLogin(){
//        if (!isLogining && !isHidden() && TextUtils.isEmpty(SharePreferenceUtil.get(getContext(), Common.KEY_ACCOUNT,""))){
//            // 如果是第一次登录
//            showDialog();
//        }else {
//            // 登录命令
//            mAccount = SharePreferenceUtil.get(getContext(),Common.KEY_ACCOUNT,"");
//            mPassword = SharePreferenceUtil.get(getContext(),Common.KEY_PASSWD,"");
//            login();
//        }
//    }

//    private void refreshList(){
//        if (isLogining ){
//            //mRefreshLayout.setRefreshing(true);
//            mRefreshLayout.post(()-> {
//                mRefreshLayout.setRefreshing(true);
//                fileQuery();
//            });
//        }
//    }

    private void showDialog() {
        mDialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_login,null);

        TextInputEditText editUSer = view.findViewById(R.id.et_User);// 用户名输入框
        TextInputEditText editPwd = view.findViewById(R.id.et_Pwd);// 密码输入框
        Button btnLoad = view.findViewById(R.id.btn_load);// 登录按钮

        btnLoad.setOnClickListener(v ->{
            //hideInput();
            if(!CommonUtil.isNetWorkConnected(getContext())){
                ToastUtil.showToast("网络未连接");
                return;
            }
            mAccount = editUSer.getText().toString();
            mPassword = editPwd.getText().toString();
            if(!TextUtils.isEmpty(mAccount)){
                if(!TextUtils.isEmpty(mPassword)){
                    btnLoad.setText(R.string.tx_loading);
                    // 登录命令
                    //login();

                }else {
                    ToastUtil.showToast("密码不能为空");
                }
            }else {
                ToastUtil.showToast("账号不能为空");
            }
        });
        mDialog.setView(view);
        mDialog.show();
    }

//    private void connectToService(){
//        mClient = new Client() {
//            @Override
//            public void onConnect(SocketTransceiver transceiver) {
//                mSocketTransceiver = transceiver;
//                Log.e(TAG,"onConnect");
//            }
//
//            @Override
//            public void onConnectFailed() {
//                Log.e(TAG,"onConnectFailed");
//            }
//
//            @Override
//            public void onReceive(SocketTransceiver transceiver, byte[] bytes) {
//                Log.e(TAG,"onReceive");
//                handleResponse(bytes);
//            }
//
//            @Override
//            public void onDisconnect(SocketTransceiver transceiver) {
//                Log.e(TAG,"onDisconnect");
//            }
//        };
//        mClient.connect("119.23.238.102",8888);  //192.168.0.139//119.23.238.102
//    }
//
//    /**
//     *  处理服务端回复的数据
//     */
//    private void handleResponse(byte[] bytes) {
//        try {
//            if (Common.HEAD.equals(new String(bytes,0,4,"gbk"))){
//                String response;
//                byte zero = 0;
//                if (zero == bytes[bytes.length-1]){
//                    response = new String(bytes,0,bytes.length-1,"gbk");
//                }else {
//                    response = new String(bytes,0,bytes.length,"gbk");
//                }
//
//                String cmd = response.substring(response.indexOf("=")+1,response.indexOf(","));
//                int index = response.indexOf("=" , response.indexOf("=")+1)+1;
//                String isOk = response.substring(index , index+1);
//                Log.e(TAG,cmd+ " - response :"+response);
//                switch (cmd){
//                    case Common.CMD_LOGIN://登录
//                        if ("1".equals(isOk)){
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_LOGIN_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
//                        }else {
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_LOGIN_ID,Common.CMD_RESPONSE_FAILED,0,response));
//                        }
//                        break;
//                    case Common.CMD_FILE_UP://上传
//                        if ("1".equals(isOk)){
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_UP_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
//                        }else {
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_UP_ID,Common.CMD_RESPONSE_FAILED,0,response));
//                        }
//                        break;
//                    case Common.CMD_FILE_QUERY://获取文件列表
//                        if ("1".equals(isOk)){
//                            //String response = (String) msg.obj;
////                            mFiles.clear();
////                            int indexS = response.indexOf(",",response.indexOf(",")+1); // 第二个逗号的位置
////                            if (indexS == response.length()-1){// 判断是否有列表
////                                mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_QUERY_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
////                                break;
////                            }
////                            String listStr = response.substring(indexS+1,response.length()-1);
////                            String[] array = listStr.split(";");
////                            for (int i = 0; i < array.length; i++) {
////                                String[] arrayContent = array[i].split(",");
////                                SyncFileBean bean = new SyncFileBean();
////
////                                bean.setName(arrayContent[0]);
////                                bean.setLength(Long.valueOf(arrayContent[1]));
////                                bean.setTime(arrayContent[2]);
////                                bean.setDownLoad(false);
////                                mFiles.add(bean);
////
////                            }
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_QUERY_ID,Common.CMD_RESPONSE_SUCCESS,0,response));
//
//                        }else {
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_QUERY_ID,Common.CMD_RESPONSE_FAILED,0,response));
//                        }
//                        break;
//                    case Common.CMD_FILE_DOWN://文件下载
//                        if ("1".equals(isOk)){
//                            // 下载返回 第一次
//                            String[] array = response.split(",");
//                            String fileName = array[2];
//                            int fileSize = Integer.valueOf(array[3]);
//
//                            int indexs = response.indexOf(",",response.indexOf(",",response.indexOf(",",response.indexOf(",")+1)+1)+1)+1;
//                            String head = response.substring(0,indexs);
//                            int len = head.getBytes("gbk").length;
//
//                            int cLen = bytes.length-len;
//                            byte[] content = new byte[cLen];
//                            System.arraycopy(bytes,bytes.length-cLen,content,0,cLen);
//                            Log.e(TAG," content len "+content.length);
//
//                            Bundle bundle = new Bundle();
//                            bundle.putString("fileName",fileName);
//                            bundle.putInt("fileSize",fileSize);
//                            bundle.putByteArray("content",content);
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_SUCCESS,Common.CMD_FILE_DOWN_HEAD,bundle));
//
//                        }else {
//                            mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_FAILED,0,bytes));
//                        }
//                        break;
//                }
//            }else {
//                mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_SUCCESS,Common.CMD_FILE_DOWN_CONTENT,bytes));
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }

//    private void login(){
//        if (isLogining) return;
//
//        String login = String.format("Orntcmd=login,data=%s,%s\0\0", mAccount, mPassword);
//        if (mClient.isConnected()){
//            Observable.create(e ->  {
//                mSocketTransceiver.send(login);
//            }).subscribeOn(Schedulers.io()).subscribe();
//        }
//    }
//
//    private void fileQuery(){
//        if (!isLogining || !mClient.isConnected()) return;
//        String fileQuery = "Orntcmd=filequery";//String.format("Orntcmd=filequery,data=%s",System.currentTimeMillis());
//        if (mClient.isConnected()){
//            Observable.create(e ->  {
//                mSocketTransceiver.send(fileQuery);
//            }).subscribeOn(Schedulers.io()).subscribe();
//        }
//    }
//
//    private void fileUp(File file,int fileSize){
//        String fileUp = String.format("Orntcmd=fileup,data=%s,",file.getName());
//
//        if (mClient.isConnected()){
//            Observable.create(e -> {
//                mSocketTransceiver.send(fileUp,fileSize);
//            }).subscribeOn(Schedulers.io()).subscribe();
//        }
//
//    }
//
//    private void fileDown(String fileName){
//        String fileDown = String.format("Orntcmd=filedown,data=%s",fileName);
//        if (mClient.isConnected()){
//            Observable.create(e -> {
//                mSocketTransceiver.send(fileDown);
//            }).subscribeOn(Schedulers.io()).subscribe();
//        }
//    }
//
//    /*上传线程*/
//    class UploadThread extends Thread{
//
//        private String mFilePath;
//        private File mFile;
//        private FileInputStream mFileInputStream;
//        public UploadThread(String filePath){
//            mFilePath = filePath;
//        }
//
//
//        @Override
//        public void run() {
//            try {
//                mFile = new File(mFilePath);
//                mFileInputStream = new FileInputStream(mFile);
//                int fileSize = (int) mFile.length();
//                Log.e(TAG,"fileSize: "+fileSize);
//                // 上传命令
//                fileUp(mFile,fileSize);
//
//                byte[] buf = new byte[1024*1024];
//                int count;
//
//                while ((count = mFileInputStream.read(buf)) != -1){
//                    Log.e(TAG,fileSize+"buf count "+count);
//                    byte[] content = new byte[count];
//                    System.arraycopy(buf,0,content,0,count);
//                    if (mClient.isConnected()){
//                        Observable.create(e1->{
//                            mSocketTransceiver.send(content);
//                        }).subscribeOn(Schedulers.io()).subscribe();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    /*下载线程*/
//    class DownloadThread extends Thread{
//
//        private FileOutputStream mFileOutputStream;
//
//        private String mFileName;
//        private int mFileSize;
//        public DownloadThread(String fileName, int fileSize){
//            this.mFileName = fileName;
//            this.mFileSize = fileSize;
//        }
//
//        @Override
//        public void run() {
//            try {
//                int count =0;
//                getAlbumStorageDir(Common.SAVE_DIR_NAME);
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+
//                        File.separator + Common.SAVE_DIR_NAME + File.separator + mFileName);
//                mFileOutputStream = new FileOutputStream(file);
//                while (count < mFileSize){
//                    byte[] bytes = mCurrentDownloadQueue.poll();
//                    if (bytes == null){
//                        Log.e(TAG,"bytes is null"+mCurrentDownloadQueue.size());
//                        continue;
//                    }
//                    mFileOutputStream.write(bytes);
//                    count += bytes.length;
//                    Log.e(TAG,"count "+count+" bytes: "+bytes.length);
//                }
//                mHandler.sendMessage(mHandler.obtainMessage(Common.CMD_FILE_DOWN_ID,Common.CMD_RESPONSE_SUCCESS,Common.CMD_FILE_DOWN_FINISH,file.getAbsolutePath()));
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e(TAG,"error "+e);
//            }
//        }

//        /**
//         * 找到download文件夹
//         * @param albumName
//         * @return
//         */
//        public File getAlbumStorageDir(String albumName) {
//            // Get the directory for the user's public pictures directory.
//            File file = new File(Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_DOWNLOADS), albumName);
//            if (!file.mkdirs()) {
//                Log.e(TAG, "Directory not created");
//            }
//            return file;
//        }
//
//
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            Uri uri = data.getData();
//            String path;
//            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
//                path = uri.getPath();
//            }else {
//                path = getPath(getContext(), uri);
//            }
//            new UploadThread(path).start();
//            Log.e(TAG,uri.getScheme()+" -path: "+path);
//        }
//    }

//    /**
//     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
//     */
//    public String getPath(final Context context, final Uri uri) {
//
//        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
//
//        // DocumentProvider
//        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    return Environment.getExternalStorageDirectory() + "/" + split[1];
//                }
//            }
//            // DownloadsProvider
//            else if (isDownloadsDocument(uri)) {
//
//
//                final String id = DocumentsContract.getDocumentId(uri);
//                final Uri contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//
//
//                return getDataColumn(context, contentUri, null, null);
//            }
//            // MediaProvider
//            else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//
//                final String selection = "_id=?";
//                final String[] selectionArgs = new String[]{split[1]};
//
//                return getDataColumn(context, contentUri, selection, selectionArgs);
//            }
//        }
//        // MediaStore (and general)
//        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            return getDataColumn(context, uri, null, null);
//        }
//        // File
//        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//        return null;
//    }
//
//
//    /**
//     * Get the value of the data column for this Uri. This is useful for
//     * MediaStore Uris, and other file-based ContentProviders.
//     *
//     * @param context       The context.
//     * @param uri           The Uri to query.
//     * @param selection     (Optional) Filter used in the query.
//     * @param selectionArgs (Optional) Selection arguments used in the query.
//     * @return The value of the _data column, which is typically a file path.
//     */
//    public String getDataColumn(Context context, Uri uri, String selection,
//                                String[] selectionArgs) {
//
//        Cursor cursor = null;
//        final String column = "_data";
//        final String[] projection = {column};
//        try {
//            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
//                    null);
//            if (cursor != null && cursor.moveToFirst()) {
//                final int column_index = cursor.getColumnIndexOrThrow(column);
//                return cursor.getString(column_index);
//            }
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//        return null;
//    }
//
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is ExternalStorageProvider.
//     */
//    public boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }
//
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is DownloadsProvider.
//     */
//    public boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }
//
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is MediaProvider.
//     */
//    public boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * 隐藏输入法
//     */
//    public void hideInput(){
//        //隐藏软键盘
//        InputMethodManager im = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        try {
//            if(im.isActive()) {
//                im.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//            }
//        }catch (Exception e){
//
//        }
//
//    }

    @Override
    public boolean onBackPressed() {
        if (DEFAULT_DIR.equals(mCurrentPathStack.peek())){
            Fragment fragment = getFragmentManager().findFragmentByTag(this.getTag());
            getFragmentManager().beginTransaction().hide(fragment).commit();
        }else {
            mCurrentPathStack.pop();
            updateFiles(mCurrentPathStack.peek());
        }

        return super.onBackPressed();
    }
}
