package east.orientation.caster.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

import east.orientation.caster.ui.fragment.ResPageFragment;

/**
 * Created by ljq on 2018/4/27.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<ResPageFragment> mFragments;
    private List<String> mTitles;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public List<ResPageFragment> getFragments() {
        return mFragments;
    }

    public void setFragments(List<ResPageFragment> fragments) {
        mFragments = fragments;
    }

    public List<String> getTitles() {
        return mTitles;
    }

    public void setTitles(List<String> titles) {
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

}
