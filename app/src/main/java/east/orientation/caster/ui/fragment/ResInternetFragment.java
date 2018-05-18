package east.orientation.caster.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.vise.xsnow.http.callback.ACallback;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import east.orientation.caster.R;
import east.orientation.caster.cnjy21.model.common.KnowledgePoint;
import east.orientation.caster.cnjy21.model.common.Subject;
import east.orientation.caster.cnjy21.model.common.Version;
import east.orientation.caster.cnjy21.request.ApiCommonService;
import east.orientation.caster.local.Bean.ExBean;
import east.orientation.caster.local.Bean.ExChild;
import east.orientation.caster.local.Bean.ExParent;
import east.orientation.caster.local.Common;
import east.orientation.caster.ui.adapter.ExMenuAdapter;
import east.orientation.caster.ui.adapter.ViewPagerAdapter;
import east.orientation.caster.util.SharePreferenceUtil;
import east.orientation.caster.util.ToastUtil;
import east.orientation.caster.view.RecyclerTabLayout;

/**
 * Created by ljq on 2018/4/24.
 *
 * 网络资源
 */

public class ResInternetFragment extends BaseFragment {

    /** 左侧菜单相关 */
    private TextView mTvGrade;
    private ExpandableListView mExListView;
    private ExMenuAdapter mExMenuAdapter;
    private SparseArray<ExBean> mSparseArray = new SparseArray<>();

    /** 右侧内容相关 */
    private RecyclerTabLayout mSubjectTabLayout;// 科目
    // 标签
    private RecyclerView mVersionLayout;// 版本
    private CommonAdapter<Version> mVersionAdapter;
    private RecyclerView mKnowLedgeLayout;// 知识点
    private CommonAdapter<KnowledgePoint> mKnowLedgeAdapter;
    private List<Version> mVersions  = new ArrayList<>();

    private TabLayout rv_knowLedge_tab;

    private ViewPager mViewPager;
    // 科目列表
    private List<Subject> mSubjects = new ArrayList<>();
    // viewpage title
    private List<String> mTitles = new ArrayList<>();
    // fragments
    private List<ResPageFragment> mPageFragments = new ArrayList<>();
    // viewpager adapter
    private ViewPagerAdapter mPagerAdapter;

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
        View view = inflater.inflate(R.layout.fragment_res_internet,container,false);
        mTvGrade = view.findViewById(R.id.tv_grade_select);
        mExListView = view.findViewById(R.id.ex_list);
        mSubjectTabLayout = view.findViewById(R.id.tb_menu);
        mViewPager = view.findViewById(R.id.vp_page);
        mVersionLayout = view.findViewById(R.id.rv_version_tab);
        rv_knowLedge_tab = view.findViewById(R.id.rv_knowLedge_tab);
        // 初始化数据
        initView();

        return view;
    }

    private void initView() {
        // 初始化左侧菜单
        initLeftMenu();
        // 初始化右侧内容
        initRightContent();
    }

    private void initLeftMenu(){
        ExParent exParent1 =  new ExParent();
        exParent1.setName("小学");
        ExParent exParent2 =  new ExParent();
        exParent2.setName("初中");
        ExParent exParent3 =  new ExParent();
        exParent3.setName("高中");

        List<String> list1 = new ArrayList<>();
        list1.add("一年级");
        list1.add("二年级");
        list1.add("三年级");
        list1.add("四年级");
        list1.add("五年级");
        list1.add("六年级");
        ExChild exChild1 = new ExChild();
        exChild1.setDatas(list1);

        List<String> list2 = new ArrayList<>();
        list2.add("初一");
        list2.add("初二");
        list2.add("初三");
        ExChild exChild2 = new ExChild();
        exChild2.setDatas(list2);

        List<String> list3 = new ArrayList<>();
        list3.add("高一");
        list3.add("高二");
        list3.add("高三");
        ExChild exChild3 = new ExChild();
        exChild3.setDatas(list3);

        ExBean exBean1 = new ExBean();
        exBean1.setExParent(exParent1);
        exBean1.setExChild(exChild1);

        ExBean exBean2 = new ExBean();
        exBean2.setExParent(exParent2);
        exBean2.setExChild(exChild2);

        ExBean exBean3 = new ExBean();
        exBean3.setExParent(exParent3);
        exBean3.setExChild(exChild3);

        mSparseArray.put(0,exBean1);
        mSparseArray.put(1,exBean2);
        mSparseArray.put(2,exBean3);

        mExMenuAdapter = new ExMenuAdapter(getContext(),mSparseArray);
        mExListView.setAdapter(mExMenuAdapter);
        mExListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition != SharePreferenceUtil.get(getContext(),Common.KEY_SELECT_GROUP_POS,0)){
                    SharePreferenceUtil.put(getContext(), Common.KEY_SELECT_GROUP_POS,groupPosition,Common.KEY_SELECT_CHILD_POS,childPosition);
                    initTab();

                }
                String text = mSparseArray.get(groupPosition).getExChild().getDatas().get(childPosition);
                SharePreferenceUtil.put(getContext(), Common.KEY_SELECT_GROUP_POS,groupPosition,Common.KEY_SELECT_CHILD_POS,childPosition);
                mTvGrade.setText(text);

                return false;
            }
        });

        //
        mTvGrade.setText(mSparseArray.get(SharePreferenceUtil.get(getContext(),Common.KEY_SELECT_GROUP_POS,0)).
                getExChild().getDatas().get(SharePreferenceUtil.get(getContext(),Common.KEY_SELECT_CHILD_POS,0)));

        // 默认展开
        for (int i = 0; i < mSparseArray.size(); i++) {
            mExListView.expandGroup(i);
        }
    }

    private void initRightContent() {
        mPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mPagerAdapter.setFragments(mPageFragments);
        mViewPager.setAdapter(mPagerAdapter);
        // 使 TabLayout 和 ViewPager 相关联
        mSubjectTabLayout.setUpWithViewPager(mViewPager);
        initTab();
        //
        initRecycle();
    }

    private void initTab(){

        int stage = 1 + SharePreferenceUtil.get(getContext(),Common.KEY_SELECT_GROUP_POS,0);
        ApiCommonService.getSubjects(stage, new ACallback<List<Subject>>() {
            @Override
            public void onSuccess(List<Subject> subjects) {
                if (subjects == null || subjects.size() <= 0)
                    return;
                mSubjects.clear();
                mSubjects.addAll(subjects);
                mTitles.clear();
                mPageFragments.clear();
                for (int i = 0; i < subjects.size(); i++) {
                    mTitles.add(subjects.get(i).getSubjectName());
                    Bundle bundle = new Bundle();
                    bundle.putString("key","index "+i);
                    bundle.putString("key1","stage "+stage);
                    ResPageFragment fragment = ResPageFragment.newInstance();
                    fragment.setArguments(bundle);
                    mPageFragments.add(fragment);
                }
                mPagerAdapter.setTitles(mTitles);
                mPagerAdapter.notifyDataSetChanged();

                mSubjectTabLayout.setCurrentItem(0,true);
                mViewPager.setCurrentItem(0);

                // 获取教材版本信息
                initTabsData();
            }

            @Override
            public void onFail(int errCode, String errMsg) {
                ToastUtil.show(getContext(),"获取科目列表失败！"+errMsg);
            }
        });

    }

    private void initRecycle(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mVersionLayout.setLayoutManager(layoutManager);
        mVersionAdapter = new CommonAdapter<Version>(getContext(),R.layout.layout_tab_title_item,mVersions) {
            @Override
            protected void convert(ViewHolder holder, Version item, int position) {
                holder.setText(R.id.tv_item_title,item.getVersionName());

            }
        };

        mVersionLayout.setAdapter(mVersionAdapter);

    }

    private void initTabsData(){
        int stage = 1 + SharePreferenceUtil.get(getContext(),Common.KEY_SELECT_GROUP_POS,0);
        int subjectTd = mSubjects.get(mViewPager.getCurrentItem()).getSubjectId();
        ApiCommonService.getVersions(stage, subjectTd, new ACallback<List<Version>>() {
            @Override
            public void onSuccess(List<Version> versions) {
                mVersions.clear();
                mVersions.addAll(versions);
                mVersionAdapter.notifyDataSetChanged();

                rv_knowLedge_tab.removeAllTabs();

                for (int i = 0; i < versions.size(); i++) {
                    rv_knowLedge_tab.addTab(rv_knowLedge_tab.newTab().setText(versions.get(i).getVersionName()));
                }

            }

            @Override
            public void onFail(int errCode, String errMsg) {
                ToastUtil.show(getContext(),"获取版本失败"+errMsg);
            }
        });


    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentByTag(this.getTag());

        getFragmentManager().beginTransaction().hide(fragment).commit();
        return super.onBackPressed();
    }

}
