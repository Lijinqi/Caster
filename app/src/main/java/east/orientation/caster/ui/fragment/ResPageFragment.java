package east.orientation.caster.ui.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vise.xsnow.http.callback.ACallback;
import com.vise.xsnow.http.mode.DownProgress;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.EmptyWrapper;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import east.orientation.caster.R;
import east.orientation.caster.cnjy21.model.document.Document;
import east.orientation.caster.cnjy21.request.ApiCommonService;
import east.orientation.caster.evevtbus.GetDocumentsMessage;
import east.orientation.caster.evevtbus.SyncMessage;
import east.orientation.caster.evevtbus.UpdateFilesMessage;
import east.orientation.caster.extract.ExtractManager;
import east.orientation.caster.extract.IExtractListener;
import east.orientation.caster.local.Common;
import east.orientation.caster.util.SharePreferenceUtil;
import east.orientation.caster.util.ToastUtil;

import static east.orientation.caster.cnjy21.request.ApiCommonService.ERROR_NULL_CODE;

/**
 * Created by ljq on 2018/4/25.
 */

public class ResPageFragment extends BaseFragment {

    private Handler mHandler = new Handler();
    private View mRootView;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRvDocument;
    private CommonAdapter<Document> mDocumentAdapter;
    private EmptyWrapper mDocumentEmptyWrapper;
    private LoadMoreWrapper mDocumentLoadMoreWrapper;
    private List<Document> mDocuments = new ArrayList<>();
    // 用于存储下载状态
    private Map<Long,Integer> mDownMap = new HashMap<>();

    private int mPage;// 页码
    private int mStage;// 学段
    private int mSubjectId;// 科目id
    private int mVersionId;// 版本id
    private String mBookId;// 册别id
    private String mChapterId;// 章节id
    //private String mKnowledgeId;// 知识点id

    private File[] mFiles;

    public static ResPageFragment newInstance() {

        Bundle args = new Bundle();

        ResPageFragment fragment = new ResPageFragment();
        // eventBus
        EventBus.getDefault().register(fragment );
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFiles = initFiles();

        Bundle bundle = getArguments();
        mStage = bundle.getInt("stage");
        mSubjectId = bundle.getInt("subjectId");
        Log.e("@@","ResPageFragment onCreate stage: "+mStage+" subjectId: "+mSubjectId);
    }

    private File[] initFiles() {
        String account = SharePreferenceUtil.get(getContext(),Common.KEY_ACCOUNT,"");
        if (TextUtils.isEmpty(account)){
            account = "unknow";
        }
        File[] files = new File[0];
        File file = new File(Common.DEFAULT_DIR+File.separator+account);
        if (!file.exists()){
            file.mkdir();
        }
        if (file.isDirectory()){
            files = file.listFiles();
        }
        return files;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        if (mRootView != null){
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (null != parent) {
                parent.removeView(mRootView);
            }
        }else {
            mRootView = inflater.inflate(R.layout.fragment_res_page,container,false);
            mRefreshLayout = mRootView.findViewById(R.id.srl_res_page);
            mRvDocument = mRootView.findViewById(R.id.rv_res_page);

            // 初始化视图
            initView();
            //
            updateData();
        }

        return mRootView;
    }

    private void initView(){
        // refresh
        mRefreshLayout.setOnRefreshListener(()->{
            // 更新数据
            updateData();
        });

        // rv
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRvDocument.setLayoutManager(layoutManager);
        // adapter
        mDocumentAdapter = new CommonAdapter<Document>(getContext(),R.layout.layout_intent_res_item,mDocuments) {
            @Override
            protected void convert(ViewHolder holder, Document document, int position) {
                holder.setText(R.id.tv_item_title,document.getTitle());
                holder.setText(R.id.tv_item_intro,document.getIntro());
                holder.setText(R.id.tv_item_type,document.getTypeName());
                holder.setText(R.id.tv_item_size,document.getFormatSize());
//                if (!mDownMap.containsKey(document.getItemId())){
//                    mDownMap.put(document.getItemId(),document.getDownLoadState());
//                }
                mDownMap.put(document.getItemId(),document.getDownLoadState());
                if (mDownMap.containsKey(document.getItemId())){
                    mDocuments.get(position).setDownLoadState(mDownMap.get(document.getItemId()));
                }

                switch (mDocuments.get(position).getDownLoadState()){
                    case 0:
                        holder.setText(R.id.btn_item_download,"下载");
                        break;
                    case 1:
                        holder.setText(R.id.btn_item_download,"下载中");
                        break;
                    case 2:
                        holder.setText(R.id.btn_item_download,"已下载");
                        break;
                    default:

                        break;
                }

                holder.getView(R.id.btn_item_download).setOnClickListener(view -> {
                    switch (mDocuments.get(position).getDownLoadState()){
                        case 0:
                            mDocuments.get(position).setDownLoadState(1);
                            mDocumentLoadMoreWrapper.notifyDataSetChanged();
                            download(document,position);
                            break;
                        case 1:
                            ToastUtil.showToast("正在下载");
                            break;
                        case 2:
                            ToastUtil.showToast("已下载");
                            break;
                        default:

                            break;
                    }

                });
            }
        };
        // empty wrapper
        mDocumentEmptyWrapper = new EmptyWrapper(mDocumentAdapter);
        mDocumentEmptyWrapper.setEmptyView(R.layout.default_empty_view);

        // loadMore wrapper
        mDocumentLoadMoreWrapper = new LoadMoreWrapper<>(mDocumentEmptyWrapper);
        mDocumentLoadMoreWrapper.setLoadMoreView(R.layout.default_loading);
        mDocumentLoadMoreWrapper.setOnLoadMoreListener(()->{
            mHandler.postDelayed(()->{
                // 加载更多
                loadMore();
            },1000);

        });

        mRvDocument.setAdapter(mDocumentLoadMoreWrapper);
    }

    private void download(Document document,int position) {
        ApiCommonService.getDocumentDownurl(document.getItemId(), new ACallback<Document>() {
            @Override
            public void onSuccess(Document data) {
                if(!TextUtils.isEmpty(data.getDownloadUrl())){
                    Log.e("@@","开始下载 "+data.getTitle());
                    String url = data.getDownloadUrl();
                    String fileName = data.getTitle()+getSuffix(url);
                    String account = SharePreferenceUtil.get(getContext(),Common.KEY_ACCOUNT,"");
                    if (TextUtils.isEmpty(account)){
                        account = Common.DEFAULT_ACOUNT;
                    }

                    String finalAccount = account;
                    ApiCommonService.downLoad(data.getDownloadUrl(),fileName,account,new ACallback<DownProgress>() {
                        @Override
                        public void onSuccess(DownProgress downProgress) {
                            if (downProgress == null || downProgress.isDownComplete()){
                                mDocuments.get(position).setDownLoadState(2);
                                mDocumentLoadMoreWrapper.notifyDataSetChanged();
                                Log.e("@@",data.getTitle()+"下载完成 ");
                                // 下载后压缩文件
                                File file = new File(Common.DEFAULT_DIR+File.separator+ finalAccount +File.separator+fileName);
                                // 解压文件夹
                                File extractDir = new File(Common.DEFAULT_DIR+File.separator+
                                        finalAccount +File.separator+fileName.substring(0,fileName.lastIndexOf(".")));
                                if (!extractDir.exists()){
                                    extractDir.mkdir();
                                }
                                ExtractManager.getInstance().onExtract(file.getAbsolutePath(), extractDir.getAbsolutePath(), "", new IExtractListener() {
                                    @Override
                                    public void onStartExtract() {
                                        Log.e("@@","开始解压");
                                    }

                                    @Override
                                    public void onExtractProgress(int current, int total) {
                                        Log.e("@@",total+" 解压 "+current);
                                    }

                                    @Override
                                    public void onEndExtract() {
                                        Log.e("@@","解压结束");
                                        if (file.delete()){
                                            Log.e("@@","删除成功");
                                            // 上传到同步服务器
                                            upload(extractDir);
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFail(int errCode, String errMsg) {
                            ToastUtil.showToast("下载失败");
                            mDocuments.get(position).setDownLoadState(0);
                            mDocumentLoadMoreWrapper.notifyDataSetChanged();
                            Log.e("@@","download err - "+errMsg);
                        }
                    });
                }else {
                    ToastUtil.showToast("url empty");
                    mDocuments.get(position).setDownLoadState(0);
                    mDocumentLoadMoreWrapper.notifyDataSetChanged();
                }
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                ToastUtil.showToast("获取下载地址失败");
                mDocuments.get(position).setDownLoadState(0);
                mDocumentLoadMoreWrapper.notifyDataSetChanged();
            }
        });
    }

    private void upload(File file) {
        // 通知页面更新
        EventBus.getDefault().post(new UpdateFilesMessage());
        // 通知SyncService上传
        EventBus.getDefault().post(new SyncMessage(Common.CMD_FILE_UP,file.getAbsolutePath()));
    }

    private boolean checkContain(File[] files,File file){
        for (File f:files){
            if (f.equals(file)){
                return true;
            }
        }
        return false;
    }

    // 更新
    private void updateData(){
        mPage = 1;
        ApiCommonService.getDocuments(mStage, mSubjectId, mChapterId, mPage, 20, new ACallback<List<Document>>() {
            @Override
            public void onSuccess(List<Document> documents) {

                mDocuments.clear();
                mDocuments.addAll(documents);
                for (Document document:mDocuments){
                    if (mDownMap.containsKey(document.getItemId())){
                        document.setDownLoadState(mDownMap.get(document.getItemId()));
                    }
                }

                mDocumentLoadMoreWrapper.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                Log.e("update @@","stage: "+mStage+" subjectId: "+mSubjectId+" chapterId: "+mChapterId);
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                if (errCode == ERROR_NULL_CODE){
                    mDocuments.clear();
                    mDocumentLoadMoreWrapper.notifyDataSetChanged();
                }
                mRefreshLayout.setRefreshing(false);
                Log.e("update @@","stage: "+mStage+" subjectId: "+mSubjectId+" chapterId: "+mChapterId+"err:" +errMsg);
            }
        });
    }

    // 加载
    private void loadMore(){
        ApiCommonService.getDocuments(mStage, mSubjectId, mChapterId, mPage++, 20, new ACallback<List<Document>>() {
            @Override
            public void onSuccess(List<Document> documents) {
                mDocuments.addAll(documents);
                mDocumentLoadMoreWrapper.notifyDataSetChanged();
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                mDocumentLoadMoreWrapper.notifyDataSetChanged();
                Log.e("loadMore @@","stage: "+mStage+" subjectId: "+mSubjectId+"err:" +errMsg);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnvent(GetDocumentsMessage message){
        Bundle bundle = message.getBundle();
        mStage = bundle.getInt("stage");
        int subjectId = bundle.getInt("subjectId");
        mVersionId = bundle.getInt("versionId");
        mBookId = bundle.getString("bookId");
        mChapterId = bundle.getString("chapterId");
        //mKnowledgeId = bundle.getString("knowledgeId");
        if (mSubjectId == subjectId){
            if (TextUtils.isEmpty(mChapterId)){
                mChapterId = String.valueOf(mVersionId);
                Log.e("@@","版本ID FOR SEARCH"+mVersionId);
                if (TextUtils.isEmpty(mBookId)){
                    mChapterId = String.valueOf(mVersionId);
                }else {
                    mChapterId = mBookId;
                }
            }
            updateData();
        }
    }

    /**
     * 获取后缀
     *
     * @param url
     * @return
     */
    private String getSuffix(String url) {
        int endIndex = url.indexOf("?");
        if (endIndex == -1) {
            endIndex = url.length();
        }
        int startIndex = url.lastIndexOf(".", endIndex);
        return url.substring(startIndex, endIndex);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
