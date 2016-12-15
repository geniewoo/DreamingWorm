package dreamingworm.grouptimetable;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    String LOG="MainActivity";
    private CountDownLatch latch=new CountDownLatch(1);
    private String result;
    private Button main_Register_Btn;
    private Button main_Login_Btn;
    private Button main_FindID_Btn;
    private EditText main_Id_Edt;
    private EditText main_Password_Edt;
    private CheckBox main_ID_Chk;
    private CheckBox main_Login_Chk;
    private Intent reIntent;
    private String id;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        main_Register_Btn = (Button) findViewById(R.id.main_Register_Btn);
        main_Login_Btn = (Button) findViewById(R.id.main_Login_Btn);
        main_FindID_Btn = (Button) findViewById(R.id.main_FindID_Btn);
        main_Id_Edt = (EditText) findViewById(R.id.main_Id_Edt);
        main_Password_Edt = (EditText) findViewById(R.id.main_Password_Edt);
        main_ID_Chk = (CheckBox) findViewById(R.id.main_ID_Chk);
        main_Login_Chk = (CheckBox) findViewById(R.id.main_Login_Chk);

        reIntent = new Intent(getApplicationContext(), RegisterActivity.class);

        main_Register_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(reIntent);
                overridePendingTransition(R.anim.hold, R.anim.hold);
            }
        });
        main_Login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(MainActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(MainActivity.this);
                    networkConnDialog.show();
                    return;
                }

                id = main_Id_Edt.getText().toString();
                password = main_Password_Edt.getText().toString();

                SharedPreferences preferences = getApplicationContext().getSharedPreferences("environ", 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ID", id);
                if (main_Login_Chk.isChecked()) {
                    editor.putBoolean("LoginChk", true);
                    editor.putString("Password", password);
                }
                if (main_ID_Chk.isChecked()) {
                    editor.putBoolean("IDChk", true);
                    editor.putString("Password", password);
                }
                editor.commit();
                new SendPost().execute();
            }
        });
        main_FindID_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FindIDDialog findIDDialog = new FindIDDialog(MainActivity.this);
                findIDDialog.show();
            }
        });

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("environ", 0);
        System.out.println("LoginChk2 : " + preferences.getBoolean("LoginChk", false));
        if (preferences.getBoolean("LoginChk", false)) {
            System.out.println("LoginCHK");
            main_Login_Chk.setChecked(true);
            id = preferences.getString("ID", "");
            password = preferences.getString("Password", "");
            System.out.println("id : " + id + " password : " + password);
            main_ID_Chk.setChecked(true);
            main_Login_Chk.setChecked(true);
            if (!id.equals("") && !password.equals("")) {
                new SendPost().execute();
            }
        } else if (preferences.getBoolean("IDChk", false)) {
            main_ID_Chk.setChecked(true);
            main_Id_Edt.setText(preferences.getString("ID", ""));
        }

    }

    private class SendPost extends AsyncTask<String,Integer,String>{
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/login.php");
                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id",id)
                        .appendQueryParameter("password",password);
                String query = builder.build().getEncodedQuery();
                OutputStream outStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(),"UTF-8"));
                while(true){
                    String line = br.readLine();
                    if(line == null) break;
                    Log.d("get",line);
                    result += line;
                }
                http.disconnect();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("get",result);
            if(result.equals("success")){
                Toast.makeText(getApplicationContext(),"로그인 성공",Toast.LENGTH_SHORT).show();

                latch.countDown();
                new GetInvitedList().execute();
            }
            else Toast.makeText(getApplicationContext(),"로그인 실패",Toast.LENGTH_SHORT).show();
            asyncProgressDialog.dismiss();
        }
    }

    class GetInvitedList extends AsyncTask<String,String,String>{
        String result=new String();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                latch.await(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url=new URL("http://pama.dothome.co.kr/getInvitedList.php");
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setDefaultUseCaches(false);

                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST");

                Uri.Builder builder=new Uri.Builder().appendQueryParameter("id",id);

                String query=builder.build().getEncodedQuery();
                OutputStream outputStream= httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                bufferedWriter.write(query);
                bufferedWriter.flush();
                bufferedWriter.close();
                httpURLConnection.connect();

                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));
                while(true){
                    String line=bufferedReader.readLine();
                    if(line==null)break;
                    result+=line;
                    System.out.println("getInvitedList : "+line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                if(!result.startsWith("fail")){
                    final ArrayList<InvitedItem> invitedItems=new ArrayList<>();
                    JSONArray jsonArray=new JSONArray(result);
                    for(int i=0;i<jsonArray.length();i++){
                        Log.e(LOG,"jasonObject : "+jsonArray.length());
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        String hostName=jsonObject.getString("hostName");
                        String hostNickname=jsonObject.getString("hostNickname");
                        String roomName=jsonObject.getString("roomName");
                        String groupSN=jsonObject.getString("groupSN");
                        int groupImg=Integer.parseInt(jsonObject.getString("roomImg"));
                        InvitedItem tempInvitedItem=new InvitedItem(groupSN,hostName,hostNickname,roomName,groupImg);
                        invitedItems.add(tempInvitedItem);
                    }
                    if(invitedItems.size()>0) {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()  {
                            public void run() {
                                Intent invitedIntent = new Intent(getApplicationContext(),InvitedActivity.class);
                                invitedIntent.putExtra("id",id);
                                for(int i=0;i<invitedItems.size();i++) {
                                    Log.e(LOG,"invited Items : " +invitedItems.get(i).getGroupSN() + " " + invitedItems.get(i).getHostName() + " " + invitedItems.get(i).getHostNickname() + " " + invitedItems.get(i).getRoomName());
                                }
                                invitedIntent.putExtra("invitedItems",invitedItems);
                                startActivity(invitedIntent);
                                overridePendingTransition(R.anim.hold,R.anim.hold);
                            }
                        }, 1000);
                    }else{
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()  {
                            public void run() {
                                Intent LoginIntent = new Intent(getApplicationContext(),SwitcherActivity.class);
                                LoginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                LoginIntent.putExtra("id",id);
                                startActivity(LoginIntent);
                                overridePendingTransition(R.anim.hold,R.anim.hold);
                            }
                        }, 1000);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}