package dreamingworm.grouptimetable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Youngs on 2016-07-15.
 */
public class FriendListAdapter extends BaseAdapter {
    private ArrayList<FriendListItem> listViewItemList = new ArrayList<FriendListItem>() ;
    private ArrayList<FriendListItem> all = new ArrayList<FriendListItem>() ;
    public FriendListAdapter() {

    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_friend, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.friend_Image_View) ;
        TextView nameTxt = (TextView) convertView.findViewById(R.id.friend_Name_Txt) ;
        TextView nicknameTxt = (TextView) convertView.findViewById(R.id.friend_Nickname_txt) ;
        Button tagBtn = (Button) convertView.findViewById(R.id.friend_Tag_Btn) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final FriendListItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageBitmap(listViewItem.getIcon());
        nameTxt.setText(listViewItem.getName());
        nicknameTxt.setText(listViewItem.getNickname());
        tagBtn.setText(listViewItem.getTag());
        iconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,FriendImageActivity.class);
                intent.putExtra("fid",listViewItem.getFid());
                context.startActivity(intent);
            }
        });
        tagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences= context.getSharedPreferences("environ",0);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString("nickname",listViewItem.getNickname());
                editor.putString("tag",listViewItem.getTag());
                editor.commit();
                AddTagDialog addTagDialog = new AddTagDialog(context);
                addTagDialog.show();
            }
        });

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    public void addItem(Bitmap icon, String name, String nickname, String tag, String fid) {
        FriendListItem item = new FriendListItem();

        item.setIcon(icon);
        item.setName(name);
        item.setNickname(nickname);
        item.setTag(tag);
        item.setFid(fid);

        listViewItemList.add(item);
        all.add(item);
    }

    public void filter(String text){
        listViewItemList.clear();
        if(text.length() == 0){
            listViewItemList.addAll(all);
        }else{
            for(FriendListItem fl : all){
                if(fl.getName() .contains(text)  || fl.getNickname().contains(text)){
                    listViewItemList.add(fl);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void clear(){
        listViewItemList.clear();
        all.clear();
    }

}
