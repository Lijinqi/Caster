package east.orientation.caster.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import east.orientation.caster.R;
import east.orientation.caster.evevtbus.SyncMessage;
import east.orientation.caster.evevtbus.UpdateFilesMessage;
import east.orientation.caster.local.Common;
import east.orientation.caster.util.CommonUtil;
import east.orientation.caster.util.FileUtil;
import east.orientation.caster.util.SharePreferenceUtil;
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

    public static ResSyncFragment newInstance() {
        Bundle args = new Bundle();

        ResSyncFragment fragment = new ResSyncFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init EventBus
        EventBus.getDefault().register(this);
        // 账户名
        mAccount = SharePreferenceUtil.get(getContext(),Common.KEY_ACCOUNT,Common.DEFAULT_ACOUNT);
        // 初始路径
        mCurrentPath = DEFAULT_DIR +File.separator + mAccount;
        // 加入队列
        mCurrentPathStack.add(mCurrentPath);
        // 更新列表
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
        mTvUser.setText(mAccount);
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
                showDel(position);
                return false;
            }
        });
        mFileRecyclerView.setAdapter(mFileAdapter);
    }

    private void showDel(int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("确认删除")
                .setPositiveButton("确认", (dialogInterface, i) -> {
                    // 如果是文件夹  则删除文件夹中所有文件
                    if (mFileList.get(position).isDirectory()){
                        // 得到文件夹中所有文件
                        mDirFiles.clear();
                        // 检索文件夹中文件
                        searchFiles(mFileList.get(position));
                        // 循环通知
                        for (File file:mDirFiles){
                            EventBus.getDefault().post(new SyncMessage(Common.CMD_FILE_DEL,file.getName()));
                        }
                        // 删除文件夹
                        if (FileUtil.delete(mFileList.get(position).getAbsolutePath())){
                            updateFiles(mCurrentPath);
                        }
                    }else {
                        // 通知
                        EventBus.getDefault().post(new SyncMessage(Common.CMD_FILE_DEL,mFileList.get(position).getName()));
                        // 删除文件夹
                        if (FileUtil.delete(mFileList.get(position).getAbsolutePath())){
                            updateFiles(mCurrentPath);
                        }
                    }


                })
                .setNegativeButton("取消", (dialogInterface, i) ->  {

                })
                .show();
    }

    private List<File> mDirFiles = new ArrayList<>();
    private void searchFiles(File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                searchFiles(f);
            } else {
                mDirFiles.add(f);
            }
        }
    }

    private void updateFiles(String filePath){
        mCurrentPath = filePath;
        File file = new File(filePath);
        if (!file.exists() ){
            file.mkdir();
        }

        if (file.isDirectory()){
            mFiles = file.listFiles();
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

    @Override
    public boolean onBackPressed() {
        String accountDir = DEFAULT_DIR +File.separator + mAccount;
        if (accountDir.equals(mCurrentPathStack.peek())){
            Fragment fragment = getFragmentManager().findFragmentByTag(this.getTag());
            getFragmentManager().beginTransaction().hide(fragment).commit();
        }else {
            mCurrentPathStack.pop();
            updateFiles(mCurrentPathStack.peek());
        }

        return super.onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateFilesMessage message){
        // 更新界面
        updateFiles(mCurrentPath);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister
        EventBus.getDefault().unregister(this);
    }
}
