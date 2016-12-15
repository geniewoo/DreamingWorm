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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Youngs on 2016-08-03.
 */
public class AddMeFriendActivity extends AppCompatActivity {
    String id;
    CountDownLatch latch;
    private ListView addMeFriend_Table_ListView;
    private AddFriendListAdapter adapter;
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
    private String nickname;
    EditText searchFriend_Edt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmefriend);
        raw_Image = BitmapFactory.decodeResource(getResources(), R.drawable.worm_sumnail);
        txt = new ArrayList<>();
        txt2 = new ArrayList<>();
        txt3 = new ArrayList<>();
        fileName = new ArrayList<>();
        image_bitmap = new ArrayList<>();
        default_Image = new ArrayList<>();
        friend = new String();
        result = new String();
        if (!NetworkConn.isNetworkConnected(AddMeFriendActivity.this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(AddMeFriendActivity.this);
            networkConnDialog.show();
            return;
        }
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("environ", 0);
        id = preferences.getString("ID", "");

        addMeFriend_Table_ListView = (ListView) findViewById(R.id.addmefriend_Table_ListView);
        searchFriend_Edt = (EditText) findViewById(R.id.addmefriend_SearchName_Edt);


        adapter = new AddFriendListAdapter();
        addMeFriend_Table_ListView.setAdapter(adapter);

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

        addMeFriend_Table_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!NetworkConn.isNetworkConnected(AddMeFriendActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(AddMeFriendActivity.this);
                    networkConnDialog.show();
                    return;
                }

                AddFriendListItem friendListItem = (AddFriendListItem) adapter.getItem(position);
                nickname = friendListItem.getNickname();
                new PutPost().execute();
            }
        });

        new SendPost().execute();

    }


    private class SendPost extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txt.clear();
            txt2.clear();
            txt3.clear();
            fileName.clear();
            default_Image.clear();
            image_bitmap.clear();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                int countLine = 0;
                URL url = new URL("http://pama.dothome.co.kr/getAddFriend.php");
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
                    Log.d("twat addfriend", line);
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
                Toast.makeText(getApplicationContext(), "친구목록 불러오기 실패", Toast.LENGTH_SHORT).show();
        }

    }

    private class GetPost extends AsyncTask<String, Integer, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(AddMeFriendActivity.this);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
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
                adapter.addItem(image_bitmap.get(i), txt.get(i), txt2.get(i));
                adapter.notifyDataSetChanged();
            }
            asyncProgressDialog.dismiss();
        }
    }

    private class PutPost extends AsyncTask<String, Integer, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(AddMeFriendActivity.this);
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
                    Log.d("twat adding",line);
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
            } else
                Toast.makeText(getApplicationContext(), "친구 등록 실패", Toast.LENGTH_SHORT).show();
            asyncProgressDialog.dismiss();
        }

    }
}

