package dreamingworm.grouptimetable;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.concurrent.CountDownLatch;

/**
 * Created by sungwoo on 2016-07-15.
 */
public class AddFriendActivity extends AppCompatActivity {
    AsyncProgressDialog asyncProgressDialog;
    String id;
    CountDownLatch latch;
    private ListView addfriend_Table_ListView;
    private AddFriendListAdapter adapter;
    private JSONArray jsonArray;
    private String find;
    private String friend;
    private String result;
    private String nickname;
    private String image;
    private String txt;
    private String txt2;
    private Bitmap image_bitmap;
    private boolean default_Image;
    EditText searchFriend_Edt;
    Button searchFriend_Btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        asyncProgressDialog=new AsyncProgressDialog(AddFriendActivity.this);
        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("environ", 0);
        id = preferences.getString("ID", "");

        latch = new CountDownLatch(1);

        addfriend_Table_ListView = (ListView) findViewById(R.id.addfriend_Table_ListView);

        adapter = new AddFriendListAdapter();
        addfriend_Table_ListView.setAdapter(adapter);

        addfriend_Table_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!NetworkConn.isNetworkConnected(AddFriendActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(AddFriendActivity.this);
                    networkConnDialog.show();
                    return;
                }

                AddFriendListItem friendListItem = (AddFriendListItem) adapter.getItem(position);
                nickname = friendListItem.getNickname();
                new PutPost().execute();
            }
        });

        searchFriend_Edt = (EditText) findViewById(R.id.addfriend_SearchName_Edt);
        searchFriend_Btn = (Button) findViewById(R.id.addfriend_SearchName_Btn);
        searchFriend_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(AddFriendActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(AddFriendActivity.this);
                    networkConnDialog.show();
                    return;
                }
                find = searchFriend_Edt.getText().toString();
                new SendPost().execute();
            }
        });
    }

    private class SendPost extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            adapter.clear();
            adapter.notifyDataSetChanged();
            friend = new String();
            result = new String();
            image = new String();
            txt = new String();
            txt2 = new String();
            default_Image = false;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                int countLine = 0;
                URL url = new URL("http://pama.dothome.co.kr/searchFriend.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("find", find);
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
                    Log.d("twat", line);
                    if (countLine == 0) {
                        result += line;
                        countLine++;
                        continue;
                    }
                    friend += line;
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
                        image = order.getString("fid");
                        txt = order.getString("name");
                        txt2 = order.getString("nickname");
                        if (order.getString("image").equals("0")) {
                            default_Image = true;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else
                Toast.makeText(getApplicationContext(), "친구목록 불러오기 실패", Toast.LENGTH_SHORT).show();
            latch.countDown();
            new GetPost().execute();
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
            Log.d("twat", String.valueOf(default_Image));
            image_bitmap = null;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                if (!default_Image) {
                    if (!ImageFileStorage.checkFile(image)) {
                        URL url = new URL("http://pama.dothome.co.kr/" + image + "_sumnail.jpg");
                        HttpURLConnection http = (HttpURLConnection) url.openConnection();
                        http.setDefaultUseCaches(false);
                        http.setDoInput(true);
                        http.connect();
                        InputStream is = http.getInputStream();
                        image_bitmap = BitmapFactory.decodeStream(is);
                        http.disconnect();
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
            if (default_Image) {
                image_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.worm_sumnail);
            } else if (!ImageFileStorage.checkFile(image)) {
                ImageFileStorage.saveBitmaptoJpeg(image_bitmap, image);
            } else {
                image_bitmap = ImageFileStorage.loadJpegtoBitmap(image);
            }
            if(!txt.equals("")) {
                adapter.addItem(image_bitmap, txt, txt2);
                adapter.notifyDataSetChanged();
            }
            asyncProgressDialog.dismiss();
        }
    }

    private class PutPost extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/putFriend.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("nickname", nickname);
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
                    result += line;
                    Log.d("twat addd",line);
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
            if (result.matches("success")) {
                Toast.makeText(getApplicationContext(), nickname + " 친구 등록 성공", Toast.LENGTH_SHORT).show();
            } else if(result.matches("me")) {
                Toast.makeText(getApplicationContext(), nickname + " 자기 자신은 안됩니다.", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "친구 등록 실패", Toast.LENGTH_SHORT).show();
            }
            asyncProgressDialog.dismiss();
        }

    }

}
