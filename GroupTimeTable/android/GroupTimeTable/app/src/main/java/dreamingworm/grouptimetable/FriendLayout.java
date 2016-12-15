package dreamingworm.grouptimetable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
 * Created by sungwoo on 2016-07-15.
 */
public class FriendLayout extends LinearLayout {
    boolean isFirst=true;
    boolean isButton=false;
    boolean isProgressDialog=false;
    AsyncProgressDialog asyncProgressDialog;
    Context contexT;
    String id;
    boolean vali;
    boolean lock = false;
    CountDownLatch latch;
    private ListView friend_Table_ListView;
    private FriendListAdapter adapter;
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
    EditText searchFriend_Edt;
    FloatingActionButton fab;

    public FriendLayout(Context context) {
        super(context);
        contexT = context;
        init();
    }

    public FriendLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        contexT = context;
        init();
    }

    public FriendLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contexT = context;
        init();
    }

    private void init() {
        asyncProgressDialog=new AsyncProgressDialog(contexT);

        raw_Image = BitmapFactory.decodeResource(getResources(),R.drawable.worm_sumnail);
        txt = new ArrayList<>();
        txt2 = new ArrayList<>();
        txt3 = new ArrayList<>();
        fileName = new ArrayList<>();
        image_bitmap = new ArrayList<>();
        default_Image = new ArrayList<>();
        friend = new String();
        result = new String();
        vali = false;
    }


    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
        if(getVisibility()==GONE){
            if(!isFirst){
                isProgressDialog=false;
                return;
            }else {
                isFirst = false;
            }
        }
        if(isButton){
            isButton=false;
            return;
        }
        if(lock){
            return;
        }
        if(isProgressDialog){
            isProgressDialog=false;
            return;
        }
        lock = true;
        LayoutInflater inflater = (LayoutInflater.from(contexT));
        inflater.inflate(R.layout.layout_friend, this, true);
        if (!NetworkConn.isNetworkConnected(contexT)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
            networkConnDialog.show();
            return;
        }
        txt.clear();
        txt2.clear();
        txt3.clear();
        fileName.clear();
        default_Image.clear();
        image_bitmap.clear();

        SharedPreferences preferences = contexT.getSharedPreferences("environ", 0);
        id = preferences.getString("ID", "");

        fab = (FloatingActionButton)findViewById(R.id.friend_AddFriend_Fab);
        friend_Table_ListView = (ListView) findViewById(R.id.friend_Table_ListView);
        searchFriend_Edt = (EditText)findViewById(R.id.friend_SearchName_Edt);


        adapter = new FriendListAdapter();
        friend_Table_ListView.setAdapter(adapter);

        latch = new CountDownLatch(1);

        searchFriend_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = searchFriend_Edt.getText().toString();
                adapter.filter(text);
            }
        });

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isButton=true;
                Intent intent = new Intent(contexT,AddFriendActivity.class);
                contexT.startActivity(intent);
                ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });

        new SendPost().execute();
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
                    Log.d("twat",line);
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
                }
                latch.countDown();
                new GetPost().execute();
            } else
                Toast.makeText(contexT, "친구목록 불러오기 실패", Toast.LENGTH_SHORT).show();
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
                for(int i = 0 ; i < Integer.parseInt(result); i++) {
                    if (!default_Image.get(i)) {
                        if (!ImageFileStorage.checkFile(fileName.get(i)) | !vali) {
                            URL url = new URL("http://pama.dothome.co.kr/" + fileName.get(i) + "_sumnail.jpg");
                            HttpURLConnection http = (HttpURLConnection) url.openConnection();
                            http.setDefaultUseCaches(false);
                            http.setDoInput(true);
                            http.connect();
                            InputStream is = http.getInputStream();
                            image_bitmap.add(BitmapFactory.decodeStream(is));
                            http.disconnect();
                            ImageFileStorage.saveBitmaptoJpeg(image_bitmap.get(i), fileName.get(i));
                        }
                        else{
                            image_bitmap.add(ImageFileStorage.loadJpegtoBitmap(fileName.get(i)));
                        }
                    }else{
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
            adapter.clear();
            adapter.notifyDataSetChanged();
            for (int i = 0; i < Integer.parseInt(result); i++) {
                adapter.addItem(image_bitmap.get(i), txt.get(i), txt2.get(i), txt3.get(i),fileName.get(i));
                adapter.notifyDataSetChanged();
            }
            SharedPreferences preferences= contexT.getSharedPreferences("environ",0);
            SharedPreferences.Editor editor=preferences.edit();
            editor.putBoolean("Validate",true);
            editor.commit();
            lock = false;
            asyncProgressDialog.dismiss();
            isProgressDialog=true;
        }
    }
}
