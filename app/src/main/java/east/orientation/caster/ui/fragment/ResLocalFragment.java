package east.orientation.caster.ui.fragment;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import east.orientation.caster.R;
import east.orientation.caster.local.Bean.DirBean;

/**
 * Created by ljq on 2018/4/24.
 *
 * 本地资源
 */

public class ResLocalFragment extends BaseFragment {
    private String[] mDirs ;

    private RecyclerView mRecyclerView;
    private CommonAdapter<DirBean> mAdapter;
    private List<DirBean> mDirList = new ArrayList<>();

    public static ResLocalFragment newInstance() {
        
        Bundle args = new Bundle();
        
        ResLocalFragment fragment = new ResLocalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_res_local,container,false);
        mRecyclerView = view.findViewById(R.id.rv_dirs);

        initData();

        initView();
        return view;
    }

    private void initData() {
        mDirs = new String[]{getResources().getString(R.string.local_res_public),
                getResources().getString(R.string.local_res_movies),
                getResources().getString(R.string.local_res_music),
                getResources().getString(R.string.local_res_pictures),
                getResources().getString(R.string.local_res_document)};

        for (int i = 0; i < mDirs.length; i++) {
            DirBean dirBean = new DirBean();
            dirBean.setId(i);
            dirBean.setName(mDirs[i]);
            mDirList.add(dirBean);
        }
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CommonAdapter<DirBean>(getContext(),R.layout.layout_dir_item,mDirList) {
            private DirBean mSelectedDir;
            @Override
            protected void convert(ViewHolder holder, DirBean dirBean, int position) {
                holder.setText(R.id.item_dir_name,dirBean.getName());

                switch (dirBean.getId()){
                    case 0:holder.setImageResource(R.id.item_dir_icon,R.drawable.ic_sd_storage_24dp);
                        break;
                    case 1:holder.setImageResource(R.id.item_dir_icon,R.drawable.ic_library_video_24dp);
                        break;
                    case 2:holder.setImageResource(R.id.item_dir_icon,R.drawable.ic_library_music_24dp);
                        break;
                    case 3:holder.setImageResource(R.id.item_dir_icon,R.drawable.ic_library_photo_24dp);
                        break;
                    case 4:holder.setImageResource(R.id.item_dir_icon,R.drawable.ic_library_books_24dp);
                        break;
                }

                if (dirBean.isSelected()){
                    holder.getConvertView().setBackgroundColor(getResources().getColor(R.color.lightblue));
                }else {
                    holder.getConvertView().setBackgroundColor(getResources().getColor(R.color.white));
                }

                holder.getConvertView().setOnClickListener(view ->{
                    if (dirBean != mSelectedDir) {
                        if (mSelectedDir != null) {
                            mSelectedDir.setSelected(false);
                        }
                    }
                    dirBean.setSelected(true);
                    mSelectedDir = dirBean;
                    notifyDataSetChanged();
                });
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentByTag(this.getTag());

        getFragmentManager().beginTransaction().hide(fragment).commit();
        return super.onBackPressed();
    }
}
