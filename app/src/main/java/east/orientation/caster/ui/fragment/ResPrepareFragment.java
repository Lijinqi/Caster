package east.orientation.caster.ui.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import east.orientation.caster.R;
import east.orientation.caster.local.Common;
import east.orientation.caster.util.FileUtil;
import east.orientation.caster.util.ToastUtil;

/**
 * Created by ljq on 2018/6/4.
 */

public class ResPrepareFragment extends BaseFragment {
    private View mRootView;
    private SwipeRefreshLayout mFileRefreshLayout;
    private RecyclerView mFileRecyclerView;
    private CommonAdapter<File> mFileAdapter;
    private List<File> mFileList = new ArrayList<>();
    private File[] mFiles ;
    private String mCurrentPath;// 当前路径

    public static ResPrepareFragment newInstance() {

        Bundle args = new Bundle();

        ResPrepareFragment fragment = new ResPrepareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                +File.separator+Common.SAVE_DIR_NAME;
        updateFiles(mCurrentPath);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_res_prepare,container,false);
        mFileRefreshLayout = mRootView.findViewById(R.id.srl_file_list);
        mFileRecyclerView = mRootView.findViewById(R.id.rv_file_list);
        //
        initView();
        return mRootView;
    }

    private void initView() {
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
    }

    private void updateFiles(String filePath){
        mCurrentPath = filePath;
        File file = new File(filePath);
        if (file.exists() ){
            if (file.isDirectory()){
                mFiles = file.listFiles();
            }
        }else {
            ToastUtil.showToast( "文件夹不存在");
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
        Fragment fragment = getFragmentManager().findFragmentByTag(this.getTag());

        getFragmentManager().beginTransaction().hide(fragment).commit();
        return super.onBackPressed();
    }
}
