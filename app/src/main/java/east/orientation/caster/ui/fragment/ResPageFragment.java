package east.orientation.caster.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import east.orientation.caster.R;

/**
 * Created by ljq on 2018/4/25.
 */

public class ResPageFragment extends BaseFragment {

    public static ResPageFragment newInstance() {

        Bundle args = new Bundle();

        ResPageFragment fragment = new ResPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_res_page,container,false);
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(getArguments().getString("key")+getArguments().getString("key1"));
        return view;
    }
}
