package east.orientation.caster.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vise.xsnow.http.callback.ACallback;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import east.orientation.caster.R;
import east.orientation.caster.cnjy21.constant.APIConstant;
import east.orientation.caster.cnjy21.model.common.Book;
import east.orientation.caster.cnjy21.model.common.Chapter;
import east.orientation.caster.cnjy21.model.common.Subject;
import east.orientation.caster.cnjy21.model.common.Version;
import east.orientation.caster.cnjy21.request.ApiCommonService;
import east.orientation.caster.evevtbus.GetDocumentsMessage;
import east.orientation.caster.ui.adapter.ViewPagerAdapter;
import east.orientation.caster.util.ToastUtil;
import east.orientation.caster.view.RecyclerTabLayout;

/**
 * Created by ljq on 2018/4/24.
 *
 * 网络资源
 */

public class ResInternetFragment extends BaseFragment {

    private View mRootView;
    /** 左侧菜单相关 */
    private TextView mTvGrade;
    private RecyclerView mRvSatge;
    private CommonAdapter<String> mStageAdapter;
    private List<String> mStages = new ArrayList<>();

    /** 右侧内容相关 */
    private RecyclerTabLayout mSubjectTabLayout;// 科目
    private ViewPager mViewPager;
    private ViewPagerAdapter mPagerAdapter;// viewpager adapter
    private List<Subject> mSubjects = new ArrayList<>(); // 科目列表
    private List<String> mTitles = new ArrayList<>();// viewpage title
    private List<ResPageFragment> mPageFragments = new ArrayList<>();// fragments
    // 标签
    private RecyclerView mVersionLayout;// 版本
    private RecyclerView mBookLayout;// 册别
    private RecyclerView mChapterLayout;// 章节
    //private RecyclerView mKnowLedgeLayout;// 知识点
    private CommonAdapter<Version> mVersionAdapter;
    private CommonAdapter<Book> mBookAdapter;
    private CommonAdapter<Chapter> mChapterAdapter;
    //private CommonAdapter<KnowledgePoint> mKnowLedgeAdapter;
    private List<Version> mVersions  = new ArrayList<>();
    private List<Book> mBooks = new ArrayList<>();
    private List<Chapter> mChapters = new ArrayList<>();
    //private List<KnowledgePoint> mKnowledgePoints = new ArrayList<>();

    private int mStage;// 学段 小、初、高
    private Subject mSubject;// 学科
    private Chapter mChapter;// 章节
    private Version mVersion;// 选中版本
    private Book mBook;// 选中册别
    //private KnowledgePoint mKnowledgePoint;// 选中知识点

    public static ResInternetFragment newInstance() {
        Bundle args = new Bundle();
        ResInternetFragment fragment = new ResInternetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        if (mRootView != null){
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (null != parent) {
                parent.removeView(mRootView);
            }
        }else {
            mRootView = inflater.inflate(R.layout.fragment_res_internet,container,false);
            mTvGrade = mRootView.findViewById(R.id.tv_grade_select);
            mRvSatge = mRootView.findViewById(R.id.rv_stage_list);
            mSubjectTabLayout = mRootView.findViewById(R.id.tb_menu);
            mViewPager = mRootView.findViewById(R.id.vp_page);
            mVersionLayout = mRootView.findViewById(R.id.rv_version_tab);
            mBookLayout = mRootView.findViewById(R.id.rv_book_tab);
            mChapterLayout = mRootView.findViewById(R.id.rv_chapter_tab);
            //mKnowLedgeLayout = mRootView.findViewById(R.id.rv_knowLedge_tab);

            // 初始化数据
            initView();
        }

        return mRootView;
    }

    private void initView(){
        // 初始化左侧菜单
        initLeftMenu();
        // 初始化右侧内容
        initRightContent();
    }

    private void initLeftMenu(){
        mStages.add("小学");
        mStages.add("初中");
        mStages.add("高中");

        //
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRvSatge.setLayoutManager(layoutManager);
        mStageAdapter = new CommonAdapter<String>(getContext(),R.layout.layout_stage_menu_item,mStages) {
            @Override
            protected void convert(ViewHolder holder, String title, int position) {
                holder.setText(R.id.item_title,title);
            }
        };
        mRvSatge.setAdapter(mStageAdapter);
        mStageAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                String text = "";
                switch (position){
                    case 0:
                        mStage = APIConstant.STAGE_XIAOXUE;
                        text = "小学";
                        break;
                    case 1:
                        mStage = APIConstant.STAGE_CHUZHONG;
                        text = "初中";
                        break;
                    case 2:
                        mStage = APIConstant.STAGE_GAOZHONG;
                        text = "高中";
                        break;
                }
                mTvGrade.setText(text);
                //
                initTab();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        // 默认选中小学
        mStage = APIConstant.STAGE_XIAOXUE;
        mTvGrade.setText("小学");
    }

    private void initRightContent() {
        mPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mPagerAdapter.setFragments(mPageFragments);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSubject = mSubjects.get(position);
                Log.e("@@","---onPageSelected");
                // 获取不同版本
                initTabsData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // 使 TabLayout 和 ViewPager 相关联
        mSubjectTabLayout.setUpWithViewPager(mViewPager);
        // 学科
        initTab();
        // 筛选（版本 知识点）
        initRecycle();
    }

    private void initTab(){
        ApiCommonService.getSubjects(mStage, new ACallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                if (subjects == null || subjects.size() <= 0)
                    return;
                mSubjects.clear();
                mSubjects.addAll(subjects);
                // 添加fragment
                mPageFragments.clear();
                mTitles.clear();
                for (int i = 0; i < subjects.size(); i++) {
                    mTitles.add(subjects.get(i).getSubjectName());
                    ResPageFragment fragment = ResPageFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putInt("stage",mStage);
                    bundle.putInt("subjectId",subjects.get(i).getSubjectId());
                    fragment.setArguments(bundle);
                    mPageFragments.add(fragment);
                }
                mPagerAdapter.setTitles(mTitles);
                mPagerAdapter.notifyDataSetChanged();

                mSubjectTabLayout.setCurrentItem(0,true);
                mViewPager.setCurrentItem(0);
                mSubject = mSubjects.get(0);

                // 更新资源列表
                //updateBroadcast();

                // 获取教材版本信息
                initTabsData();
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                ToastUtil.showToast("获取科目列表失败！"+errMsg);
            }
        });

    }

    private void initRecycle(){
        // 版本
        LinearLayoutManager versionLayoutManager = new LinearLayoutManager(getContext());
        versionLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mVersionLayout.setLayoutManager(versionLayoutManager);
        mVersionAdapter = new CommonAdapter<Version>(getContext(),R.layout.layout_tab_title_item,mVersions) {

            @Override
            protected void convert(ViewHolder holder, Version version, int position) {
                holder.setText(R.id.tv_item_title,version.getVersionName());
                if (version.isSelected()){
                    holder.getConvertView().setBackgroundResource(R.drawable.sp_tab_item_selected);
                }else {
                    holder.getConvertView().setBackgroundColor(getResources().getColor(R.color.white));
                }
                holder.getConvertView().setOnClickListener(view ->{
                    if (version != mVersion) {
                        if (mVersion != null) {
                            mVersion.setSelected(false);
                        }
                    }
                    version.setSelected(true);
                    mVersion = version;
                    // 获取册别
                    getBooks();
                    notifyDataSetChanged();
                });
            }
        };
        mVersionLayout.setAdapter(mVersionAdapter);

        // 册别
        LinearLayoutManager bookLayoutManager = new LinearLayoutManager(getContext());
        bookLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBookLayout.setLayoutManager(bookLayoutManager);
        mBookAdapter = new CommonAdapter<Book>(getContext(),R.layout.layout_tab_title_item,mBooks) {

            @Override
            protected void convert(ViewHolder holder, Book book, int position) {
                holder.setText(R.id.tv_item_title,book.getBookName());
                if (book.isSelected()){
                    holder.getConvertView().setBackgroundResource(R.drawable.sp_tab_item_selected);
                }else {
                    holder.getConvertView().setBackgroundColor(getResources().getColor(R.color.white));
                }
                holder.getConvertView().setOnClickListener(view ->{
                    if (book != mBook) {
                        if (mBook != null) {
                            mBook.setSelected(false);
                        }
                    }
                    book.setSelected(true);
                    mBook = book;
                    // 获取章节
                    getChapters();
                    notifyDataSetChanged();
                });
            }
        };
        mBookLayout.setAdapter(mBookAdapter);

        // 章节
        LinearLayoutManager chapterLayoutManager = new LinearLayoutManager(getContext());
        chapterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mChapterLayout.setLayoutManager(chapterLayoutManager);
        mChapterAdapter = new CommonAdapter<Chapter>(getContext(),R.layout.layout_tab_title_item,mChapters) {
            @Override
            protected void convert(ViewHolder holder, Chapter chapter, int position) {
                holder.setText(R.id.tv_item_title,chapter.getName());

                if (chapter.isSelected()){
                    holder.getConvertView().setBackgroundResource(R.drawable.sp_tab_item_selected);
                }else {
                    holder.getConvertView().setBackgroundColor(getResources().getColor(R.color.white));
                }
                holder.getConvertView().setOnClickListener(view ->{
                    if (chapter != mChapter) {
                        if (mChapter != null) {
                            mChapter.setSelected(false);
                        }
                    }
                    chapter.setSelected(true);
                    mChapter = chapter;
                    //
                    updateBroadcast();
                    notifyDataSetChanged();
                });
            }
        };
        mChapterLayout.setAdapter(mChapterAdapter);

        // 知识点
//        LinearLayoutManager knowLayoutManager = new LinearLayoutManager(getContext());
//        knowLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        mKnowLedgeLayout.setLayoutManager(knowLayoutManager);
//        mKnowLedgeAdapter = new CommonAdapter<KnowledgePoint>(getContext(),R.layout.layout_tab_title_item,mKnowledgePoints) {
//
//            @Override
//            protected void convert(ViewHolder holder, KnowledgePoint knowledgePoint, int position) {
//                holder.setText(R.id.tv_item_title,knowledgePoint.getName());
//
//                if (knowledgePoint.isSelected()){
//                    holder.getConvertView().setBackgroundResource(R.drawable.sp_tab_item_selected);
//                }else {
//                    holder.getConvertView().setBackgroundColor(getResources().getColor(R.color.white));
//                }
//                holder.getConvertView().setOnClickListener(view ->{
//                    if (knowledgePoint != mKnowledgePoint) {
//                        if (mKnowledgePoint != null) {
//                            mKnowledgePoint.setSelected(false);
//                        }
//                    }
//                    knowledgePoint.setSelected(true);
//                    mKnowledgePoint = knowledgePoint;
//                    //
//                    updateBroadcast();
//                    notifyDataSetChanged();
//                });
//            }
//        };
//        mKnowLedgeLayout.setAdapter(mKnowLedgeAdapter);
    }

    private void initTabsData(){
        // 获取版本
        ApiCommonService.getVersions(mStage, mSubject.getSubjectId(), new ACallback<List<Version>>() {
            @Override
            public void onSuccess(List<Version> versions) {
                mVersions.clear();
                mVersions.addAll(versions);

                if (mVersions.size()>0){
                    // 默认选中第一项
                    mVersions.get(0).setSelected(true);
                    mVersion = mVersions.get(0);
                    // 根据版本获取册别
                    getBooks();
                }

                mVersionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                ToastUtil.showToast("获取版本失败"+errMsg);
            }
        });

        // 获取知识点
//        ApiCommonService.getKnowledgePoints(mStage, mSubject.getSubjectId(), new ACallback<List<KnowledgePoint>>() {
//            @Override
//            public void onSuccess(List<KnowledgePoint> knowledgePoints) {
//                mKnowledgePoints.clear();
//                mKnowledgePoints.addAll(knowledgePoints);
//
//                if (mKnowledgePoints.size()>0){
//                    // 默认选中第一项
//                    mKnowledgePoints.get(0).setSelected(true);
//                    mKnowledgePoint = mKnowledgePoints.get(0);
//                }
//                // 更新资源列表
//                updateBroadcast();
//
//                mKnowLedgeAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFail(int errCode, String errMsg) {
//                mKnowledgePoints.clear();
//                mKnowledgePoint = null;
//
//                //
//                updateBroadcast();
//                mKnowLedgeAdapter.notifyDataSetChanged();
//            }
//        });
    }

    private void getBooks(){
        // 根据版本获取册别
        ApiCommonService.getBooks(mVersion.getVersionId(), new ACallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> books) {
                mBooks.clear();
                mBooks.addAll(books);

                if (mBooks.size()>0){
                    // 默认选中第一项
                    mBooks.get(0).setSelected(true);
                    mBook = mBooks.get(0);
                    // 根据册别获取章节
                    getChapters();
                }

                mBookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                mBooks.clear();
                mBook = null;

                mChapters.clear();
                mChapter = null;
                mChapterAdapter.notifyDataSetChanged();
                mBookAdapter.notifyDataSetChanged();
                //
                updateBroadcast();
            }
        });
    }

    private void getChapters(){

        ApiCommonService.getChapters(mBook.getBookId(), new ACallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                mChapters.clear();
                mChapters.addAll(chapters);

                if (mChapters.size()>0){
                    // 默认选中第一项
                    mChapters.get(0).setSelected(true);
                    mChapter = mChapters.get(0);
                }
                // 更新资源列表
                updateBroadcast();
                mChapterAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                mChapters.clear();
                mChapter = null;
                //
                updateBroadcast();
                mChapterAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateBroadcast(){
        Bundle bundle = new Bundle();
        bundle.putInt("stage",mStage);
        if (mSubject != null){
            bundle.putInt("subjectId",mSubject.getSubjectId());
        }

        if (mVersion != null){
            bundle.putInt("versionId",mVersion.getVersionId());
        }

        if (mBook != null){
            bundle.putString("bookId",String.valueOf(mBook.getBookId()));
        }else {
            bundle.putString("bookId","");
        }

//        if (mKnowledgePoint != null){
//            bundle.putString("knowledgeId",String.valueOf(mKnowledgePoint.getId()));
//        }else {
//            bundle.putString("knowledgeId","");
//        }

        if (mChapter != null){
            bundle.putString("chapterId",String.valueOf(mChapter.getId()));
        }else {
            bundle.putString("chapterId","");
        }

        EventBus.getDefault().post(new GetDocumentsMessage(bundle));
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentByTag(this.getTag());

        getFragmentManager().beginTransaction().hide(fragment).commit();
        return super.onBackPressed();
    }

}
