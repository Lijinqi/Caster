package east.orientation.caster.ui.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import east.orientation.caster.R;
import east.orientation.caster.local.Bean.ExBean;

/**
 * Created by ljq on 2018/4/24.
 */

public class ExMenuAdapter extends BaseExpandableListAdapter{
    private LayoutInflater mInflater;
    private SparseArray<ExBean> datas = new SparseArray<>();

    public ExMenuAdapter(Context context,SparseArray<ExBean> datas){
        mInflater = LayoutInflater.from(context);
        this.datas = datas;
    }

    @Override
    public int getGroupCount() {
        return datas.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return datas.get(groupPosition).getExChild().getDatas().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return datas.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return datas.get(groupPosition).getExChild().getDatas().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.ex_menu_item_parent,null);
        }
        convertView.setTag(R.layout.ex_menu_item_parent,groupPosition);
        convertView.setTag(R.layout.ex_menu_item_child,-1);
        TextView parentName = convertView.findViewById(R.id.view_title);
        parentName.setText(datas.get(groupPosition).getExParent().getName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.ex_menu_item_child,null);
        }
        convertView.setTag(R.layout.ex_menu_item_parent,groupPosition);
        convertView.setTag(R.layout.ex_menu_item_child,childPosition);
        TextView childName = convertView.findViewById(R.id.view_content);
        childName.setText(datas.get(groupPosition).getExChild().getDatas().get(childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
