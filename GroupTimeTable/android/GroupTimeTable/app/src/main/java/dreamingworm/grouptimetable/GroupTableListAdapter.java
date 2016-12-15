package dreamingworm.grouptimetable;

/**
 * Created by Youngs on 2016-08-03.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupTableListAdapter extends BaseAdapter {
    private ArrayList<GroupTableListItem> listItemArrayList = new ArrayList<GroupTableListItem>();

    public GroupTableListAdapter() {

    }

    @Override
    public int getCount() {
        return listItemArrayList.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final int pos = i;
        final Context context = viewGroup.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_grouptable, viewGroup, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) view.findViewById(R.id.groupTable_List_Image);
        TextView nameTxt = (TextView) view.findViewById(R.id.groupTable_Name_Txt);
        TextView nicknameTxt = (TextView) view.findViewById(R.id.groupTable_Nickname_Txt);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final GroupTableListItem listViewItem = listItemArrayList.get(pos);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageBitmap(listViewItem.getIcon());
        nameTxt.setText(listViewItem.getName());
        nicknameTxt.setText(listViewItem.getNickname());

        if(listViewItem.getFlag() == '0'){
            iconImageView.setVisibility(View.GONE);
            nameTxt.setVisibility(View.GONE);
            nicknameTxt.setVisibility(View.GONE);
        }else{
            iconImageView.setVisibility(View.VISIBLE);
            nameTxt.setVisibility(View.VISIBLE);
            nicknameTxt.setVisibility(View.VISIBLE);
        }

        if (listViewItem.getName() == "친구목록" || listViewItem.getName() == "설정" || listViewItem.getName() == "나가기") {
            nameTxt.setTextSize(TimeTableInfo.dpToPx(context,15));
        }

        return view;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return listItemArrayList.get(position);
    }

    public void addItem(Bitmap icon, String name, String nickname, char flag) {
        GroupTableListItem item = new GroupTableListItem();
        item.setIcon(icon);
        item.setName(name);
        item.setNickname(nickname);
        item.setFlag(flag);
        listItemArrayList.add(item);
    }

    public void clear() {
        listItemArrayList.clear();
    }
    public void changeflag(int position){
        if (listItemArrayList.get(position).getFlag() == '0') {
            listItemArrayList.get(position).setFlag('1');
        }else{
            listItemArrayList.get(position).setFlag('0');

        }
    }
}
