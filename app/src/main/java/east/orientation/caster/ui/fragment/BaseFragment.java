package east.orientation.caster.ui.fragment;

import android.app.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by ljq on 2018/4/24.
 */

public abstract class BaseFragment extends Fragment implements BackPressedHandler{


    protected Activity mActivity;

    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return AnimationUtils.loadAnimation(mActivity, android.R.anim.slide_in_left);
        } else {
            return AnimationUtils.loadAnimation(mActivity, android.R.anim.slide_out_right);
        }
    }

    @Override
    public boolean onBackPressed() {
        return BackHandlerHelper.handleBackPress(this);
    }
}
