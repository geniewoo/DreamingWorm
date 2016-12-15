package dreamingworm.grouptimetable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by SungWoo on 2016-07-22.
 */
public class AddGroupFriendActivity extends AppCompatActivity {
    AsyncProgressDialog asyncProgressDialog;


    CountDownLatch latch;
    String id;
    boolean vali;
    private ListView addGroup_AddFriend_ListView;
    private AddGroupFriendAdapter adapter;
    private JSONArray jsonArray;
    private String friend;
    private String result;
    private ArrayList<String> txt;
    private ArrayList<String> txt2;
    private ArrayList<String> txt3;
    private ArrayList<String> fileName;
    private ArrayList<Bitmap> image_bitmap;
    private Bitmap raw_Image;
    private ArrayList<Boolean> default_Image;
    private ArrayList<String> clickedNicknames = new ArrayList<String>();
    private ArrayList<String> nickname = new ArrayList<String>();
    private ArrayList<String> clickedIds = new ArrayList<String>();
    private EditText addGroup_AddFriend_Edt;
    private TextView addGroup_AddFriend_Txt;
    private Button addGroup_AddFriend_Btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addgroup_friend);
        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        asyncProgressDialog=new AsyncProgressDialog(AddGroupFriendActivity.this);

        addGroup_AddFriend_ListView = (ListView) findViewById(R.id.addGroup_AddFriend_ListView);
        addGroup_AddFriend_Edt = (EditText) findViewById(R.id.addGroup_AddFriend_Edt);
        addGroup_AddFriend_Btn = (Button) findViewById(R.id.addGroup_AddFriend_Btn);
        addGroup_AddFriend_Txt = (TextView) findViewById(R.id.addGroup_AddFriend_Txt);

        adapter = new AddGroupFriendAdapter();
        addGroup_AddFriend_ListView.setAdapter(adapter);

        raw_Image = BitmapFactory.decodeResource(getResources(),R.drawable.worm_sumnail);
        latch = new CountDownLatch(1);
        txt = new ArrayList<>();
        txt2 = new ArrayList<>();
        txt3 = new ArrayList<>();
        fileName = new ArrayList<>();
        image_bitmap = new ArrayList<>();
        default_Image = new ArrayList<>();
        friend = new String();
        result = new String();

        addGroup_AddFriend_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("nicknames", clickedNicknames);
                intent.putExtra("ids", clickedIds);
                setResult(1, intent);
                finish();
                overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });

        addGroup_AddFriend_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = addGroup_AddFriend_Edt.getText().toString();
                adapter.filter(text);
            }
        });
        addGroup_AddFriend_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (clickedNicknames.contains(adapter.getListItem(position).getNickname())) {
                    view.setBackgroundColor(Color.argb(0, 0, 0, 0));
                    clickedIds.remove(adapter.getListItem(position).getFid());
                    clickedNicknames.remove(adapter.getListItem(position).getNickname());
                    adapter.getListItem(position).setIsClicked(false);
                    changeFriendTxt();
                } else if(nickname.contains(adapter.getListItem(position).getNickname())){
                    view.setBackgroundColor(Color.argb(50, 50, 50, 50));
                } else {
                    view.setBackgroundColor(Color.argb(100, 100, 100, 100));
                    clickedNicknames.add(adapter.getListItem(position).getNickname());
                    clickedIds.add(adapter.getListItem(position).getFid());
                    adapter.getListItem(position).setIsClicked(true);
                    changeFriendTxt();
                }
                new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                }.run();

            }
        });

        id = getIntent().getStringExtra("id");
        if (getIntent().getStringArrayListExtra("nickname") != null) {
            nickname = getIntent().getStringArrayListExtra("nickname");
            for (int i = 0; i < nickname.size(); i++) {
                addGroup_AddFriend_Txt.append(nickname.get(i) + " ");
            }
        }
        if (getIntent().getStringArrayListExtra("nicknames") != null) {
            clickedNicknames = getIntent().getStringArrayListExtra("nicknames");
            clickedIds = getIntent().getStringArrayListExtra("ids");
            for (int i = 0; i < clickedNicknames.size(); i++) {
                addGroup_AddFriend_Txt.append(clickedNicknames.get(i) + " ");
            }
        }
        new SendPost().execute();
    }

    private void changeFriendTxt() {
        addGroup_AddFriend_Txt.setText("");
        for (int i = 0; i < clickedNicknames.size(); i++) {
            addGroup_AddFriend_Txt.append(" " + clickedNicknames.get(i));
        }
    }

    private class SendPost extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                int countLine = 0;
                URL url = new URL("http://pama.dothome.co.kr/getFriend.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id);
                String query = builder.build().getEncodedQuery();
                OutputStream outStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                    Log.d("twat addGroup", line);
                    if (countLine == 0) {
                        result = line;
                        countLine++;
                        continue;
                    }
                    friend = line;
                }
                http.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (result.matches("[0-9]*")) {
                if (Integer.parseInt(result) != 0) {
                        try {
                            jsonArray = new JSONArray(friend);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                }
                for (int i = 0; i < Integer.parseInt(result); i++) {
                    try {
                        JSONObject order = jsonArray.getJSONObject(i);
                        txt.add(order.getString("name"));
                        txt2.add(order.getString("nickname"));
                        fileName.add(order.getString("fid"));
                        if (order.getString("tag").matches("null")) {
                            txt3.add("");
                        } else {
                            txt3.add(order.getString("tag"));
                        }
                        if (order.getString("image").equals("0")) {
                            default_Image.add(true);
                        } else {
                            default_Image.add(false);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                latch.countDown();
                new GetPost().execute();
            } else
                Toast.makeText(getApplicationContext(), "친구목록 불러오기 실패", Toast.LENGTH_SHORT).show();
        }

    }

    private class GetPost extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                for (int i = 0; i < Integer.parseInt(result); i++) {
                    if (!default_Image.get(i)) {
                        if (!ImageFileStorage.checkFile(fileName.get(i))) {
                            URL url = new URL("http://pama.dothome.co.kr/" + fileName.get(i) + "_sumnail.jpg");
                            HttpURLConnection http = (HttpURLConnection) url.openConnection();
                            http.setDefaultUseCaches(false);
                            http.setDoInput(true);
                            http.connect();
                            InputStream is = http.getInputStream();
                            image_bitmap.add(BitmapFactory.decodeStream(is));
                            http.disconnect();
                            ImageFileStorage.saveBitmaptoJpeg(image_bitmap.get(i), fileName.get(i));
                        } else {
                            image_bitmap.add(ImageFileStorage.loadJpegtoBitmap(fileName.get(i)));
                        }
                    } else {
                        image_bitmap.add(raw_Image);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            for (int i = 0; i < Integer.parseInt(result); i++) {
                adapter.addItem(image_bitmap.get(i), txt.get(i), txt2.get(i), txt3.get(i), fileName.get(i));
                adapter.notifyDataSetChanged();
            }
            asyncProgressDialog.dismiss();
        }
    }

    public class AddGroupFriendAdapter extends BaseAdapter {
        private ArrayList<AddGroupFriendListItem> addGroupFriendListItems = new ArrayList<AddGroupFriendListItem>();
        private ArrayList<AddGroupFriendListItem> all = new ArrayList<AddGroupFriendListItem>();

        public AddGroupFriendAdapter() {

        }

        @Override
        public int getCount() {
            return addGroupFriendListItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_addgroup_friend, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.addGroup_FriendImage_View);
            TextView nameTxt = (TextView) convertView.findViewById(R.id.addGroup_FriendName_Txt);
            TextView nicknameTxt = (TextView) convertView.findViewById(R.id.addGroup_FriendNickname_Txt);
            TextView tagTxt = (TextView) convertView.findViewById(R.id.addGroup_FriendTag_Txt);

            // Data Set(addGroupFriendListItems)에서 position에 위치한 데이터 참조 획득
            final AddGroupFriendListItem listViewItem = addGroupFriendListItems.get(position);
            // 아이템 내 각 위젯에 데이터 반영
            iconImageView.setImageBitmap(listViewItem.getIcon());
            nameTxt.setText(listViewItem.getName());
            nicknameTxt.setText(listViewItem.getNickname());
            tagTxt.setText(listViewItem.getTag());

            if (clickedNicknames.contains(listViewItem.getNickname())) {
                convertView.setBackgroundColor(Color.argb(100, 100, 100, 100));
            } else if(nickname.contains(listViewItem.getNickname())) {
                convertView.setBackgroundColor(Color.argb(50, 50, 50, 50));
                convertView.setClickable(false);
            } else
             {
                convertView.setBackgroundColor(Color.argb(0, 0, 0, 0));
            }

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return addGroupFriendListItems.get(position);
        }

        public AddGroupFriendListItem getListItem(int position) {
            return addGroupFriendListItems.get(position);
        }

        public void addItem(Bitmap icon, String name, String nickname, String tag, String fid) {
            AddGroupFriendListItem item = new AddGroupFriendListItem();

            item.setIcon(icon);
            item.setName(name);
            item.setNickname(nickname);
            item.setTag(tag);
            item.setFid(fid);
            addGroupFriendListItems.add(item);
            all.add(item);
        }

        public void filter(String text) {
            addGroupFriendListItems.clear();
            if (text.length() == 0) {
                addGroupFriendListItems.addAll(all);
            } else {
                for (AddGroupFriendListItem fl : all) {
                    if (fl.getName().contains(text) || fl.getNickname().contains(text)) {
                        addGroupFriendListItems.add(fl);
                    }
                }
            }
            notifyDataSetChanged();
        }

        public void clear() {
            addGroupFriendListItems.clear();
            all.clear();
        }


        public class AddGroupFriendListItem {
            private String fid;

            private Bitmap icon;
            private String name;
            private String nickname;
            private String tag;
            private boolean isClicked = false;

            public boolean getIsClicked() {
                return isClicked;
            }

            public void setIsClicked(boolean isClicked) {
                this.isClicked = isClicked;
            }

            public void setIcon(Bitmap icon) {
                this.icon = icon;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setNickname(String nickname) {
                this.nickname = nickname;
            }

            public void setTag(String tag) {
                this.tag = tag;
            }

            public Bitmap getIcon() {
                return this.icon;
            }

            public String getName() {
                return this.name;
            }

            public String getNickname() {
                return this.nickname;
            }

            public String getTag() {
                return this.tag;
            }

            public String getFid() {
                return fid;
            }

            public void setFid(String fid) {
                this.fid = fid;
            }
        }
    }
}