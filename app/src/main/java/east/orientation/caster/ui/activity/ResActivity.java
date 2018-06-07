package east.orientation.caster.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import east.orientation.caster.R;
import east.orientation.caster.ui.fragment.BackHandlerHelper;
import east.orientation.caster.ui.fragment.ResInternetFragment;
import east.orientation.caster.ui.fragment.ResLocalFragment;
import east.orientation.caster.ui.fragment.ResSyncFragment;


/**
 * Created by ljq on 2018/4/23.
 *
 * 资源
 */

public class ResActivity extends FragmentActivity {

    private ResInternetFragment mResInternetFragment;
    private ResSyncFragment mResSyncFragment;
    private ResLocalFragment mResLocalFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_res);
        if (savedInstanceState == null){
            // 初始化
            init();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mResInternetFragment = (ResInternetFragment) getSupportFragmentManager().findFragmentByTag(ResInternetFragment.class.getName());
        mResSyncFragment = (ResSyncFragment) getSupportFragmentManager().findFragmentByTag(ResSyncFragment.class.getName());
        mResLocalFragment = (ResLocalFragment) getSupportFragmentManager().findFragmentByTag(ResLocalFragment.class.getName());
    }

    private void init() {
        mResInternetFragment = ResInternetFragment.newInstance();
        mResSyncFragment = ResSyncFragment.newInstance();
        mResLocalFragment = ResLocalFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.layout_content,mResInternetFragment,ResInternetFragment.class.getName())
                .add(R.id.layout_content,mResSyncFragment,ResSyncFragment.class.getName())
                .add(R.id.layout_content,mResLocalFragment,ResLocalFragment.class.getName())
                .hide(mResInternetFragment)
                .hide(mResSyncFragment)
                .hide(mResLocalFragment)
                .commit();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.tv_item_internet:
                // 网络资源
                showInternet();
                break;
            case R.id.tv_item_sync:
                // 同步资源
                showSync();
                break;
            case R.id.tv_item_local:
                // 本地资源
                showLocal();
                break;
        }
    }

    public void showInternet(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.show(mResInternetFragment).hide(mResSyncFragment).hide(mResLocalFragment).commit();
    }

    public void showSync(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.show(mResSyncFragment).hide(mResInternetFragment).hide(mResLocalFragment).commit();
    }

    public void showLocal(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.show(mResLocalFragment).hide(mResInternetFragment).hide(mResSyncFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)&&mResSyncFragment.isHidden()&&mResInternetFragment.isHidden()
                &&mResLocalFragment.isHidden()){
            super.onBackPressed();
        }
    }
}
