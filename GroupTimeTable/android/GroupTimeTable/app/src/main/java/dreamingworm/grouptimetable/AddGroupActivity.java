package dreamingworm.grouptimetable;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
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

/**
 * Created by sungwoo on 2016-07-21.
 */
public class AddGroupActivity extends AppCompatActivity {
    AsyncProgressDialog asyncProgressDialog;
    String LOG="AddGroupActivity";
    long timeSN;
    CountDownLatch latch=new CountDownLatch(1);
    ArrayList<String> clickedNicknames=new ArrayList<String>();
    ArrayList<String> clickedIds=new ArrayList<String>();
    String id;
    int currentTabNum;
    Button addGroup_Create_Btn;
    Button addGroup_Cancel_Btn;
    EditText addGroup_Name_Edt;
    Button addGroup_addFriend_Btn;
    LinearLayout addGroup_GroupImg_LinearLayout;
    GroupGroupInfo groupGroupInfo=new GroupGroupInfo();
    ArrayList<ImageView> groupImages=new ArrayList<ImageView>();
    TextView addGroup_Tables[]=new TextView[5];
    int selectedImg=0;
    int selectedTimeTable=0;
    char isBusy[]=new char[392];
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addgroup);
        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        asyncProgressDialog=new AsyncProgressDialog(AddGroupActivity.this);

        addGroup_addFriend_Btn=(Button)findViewById(R.id.addGroupt_AddFriend_Btn);
        addGroup_GroupImg_LinearLayout=(LinearLayout) findViewById(R.id.addGroup_GroupImg_LinearLayout);
        addGroup_Name_Edt=(EditText)findViewById(R.id.addGroup_Name_Edt);
        addGroup_Create_Btn=(Button)findViewById(R.id.addGroup_Create_Btn);
        addGroup_Cancel_Btn=(Button)findViewById(R.id.addGroup_Cancel_Btn);
        makeAddGroupTables();
        makeGroupImgView();

        id=getIntent().getStringExtra("id");
        currentTabNum=getIntent().getIntExtra("currentTabNum",0);

        addGroup_addFriend_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AddGroupFriendActivity.class);
                intent.putExtra("id",id);
                if(clickedNicknames.size()!=0){
                    intent.putExtra("nicknames",clickedNicknames);
                    intent.putExtra("ids",clickedIds);
                }
                startActivityForResult(intent,1);
                overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        addGroup_Create_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(AddGroupActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(AddGroupActivity.this);
                    networkConnDialog.show();
                    return;
                }

                timeSN=System.currentTimeMillis();
                new MakeGroupTableInfo().execute();
                finish();
                overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        addGroup_Cancel_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });

        new SendPost().execute();
    }
    private class MakeGroupTableInfo extends AsyncTask<String, String, String> {
        String tableName=new String();
        String result;

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            latch=new CountDownLatch(1);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                int countLine = 0;
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/getTable.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("num", String.valueOf(selectedTimeTable+1));
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
                    if (countLine == 0) {
                        tableName += line;
                        countLine++;
                        continue;
                    }
                    result += line;
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
            if (tableName.equals("fail")) {
                Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT);
            } else {
                isBusy=new char[392];
                for(int i=0;i<isBusy.length;i++){
                    isBusy[i]='0';
                }
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i = 0 ; i<jsonArray.length();i++){
                        JSONObject js = jsonArray.getJSONObject(i);
                        int row = js.getInt("row");
                        int col = js.getInt("column");
                        int size = js.getInt("size");

                        for(int j=0;j<size;j++){
                            isBusy[col*TimeTableInfo.SIZEY+row+j]='1';
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            latch.countDown();
            new PutGroupTableInfo().execute();
        }
    }
    private class PutGroupTableInfo extends AsyncTask<String,Integer,String>{
        JSONObject tableInfoObject=new JSONObject();
        String jsValue;
        String result="";
        JSONObject tableListJO;
        String jsValue2;
        int nicknameSize;
        String jsValue3;
        JSONArray nicknamesJA=new JSONArray();
        JSONObject nicknamesJO=new JSONObject();
        String roomName;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                latch.await(10, TimeUnit.MILLISECONDS);
                latch=new CountDownLatch(1);
                String isBusyStr=new String(isBusy,0,196);
                String isBusyStr2=new String(isBusy,196,196);
                tableInfoObject.put("tableInfo1",isBusyStr);
                tableInfoObject.put("tableInfo2",isBusyStr2);
                tableInfoObject.put("groupSN",String.valueOf(timeSN));
                jsValue=tableInfoObject.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                tableListJO = new JSONObject();
                tableListJO.put("groupName", addGroup_Name_Edt.getText().toString());
                tableListJO.put("tabPos",currentTabNum);
                tableListJO.put("tableImg",selectedImg+1);
                tableListJO.put("myTableNum",selectedTimeTable);
                tableListJO.put("isFav",false);
                tableListJO.put("groupSN",String.valueOf(timeSN));
                jsValue2=tableListJO.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            nicknameSize=clickedNicknames.size();
            for(int i=0;i<nicknameSize;i++){
                JSONObject nicknameJO=new JSONObject();
                try {
                    nicknameJO.put("nickname",clickedNicknames.get(i));
                    nicknameJO.put("fid",clickedIds.get(i));
                    nicknamesJA.put(nicknameJO);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                nicknamesJO.put("nicknamesIds",nicknamesJA);
                nicknamesJO.put("size",nicknameSize);
                nicknamesJO.put("groupSN",String.valueOf(timeSN));
                jsValue3=nicknamesJO.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            roomName=addGroup_Name_Edt.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/postGroupTableInfo.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("json", jsValue)
                        .appendQueryParameter("json2", jsValue2)
                        .appendQueryParameter("json3", jsValue3)
                        .appendQueryParameter("id", id).appendQueryParameter("type","1")
                        .appendQueryParameter("roomName",roomName).appendQueryParameter("roomImg",String.valueOf(selectedImg+1));
                System.out.println("id : "+ id);
                String query = builder.build().getEncodedQuery();
                OutputStream outputStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br=new BufferedReader(new InputStreamReader(http.getInputStream(),"UTF-8"));
                while(true){
                    String line=br.readLine();
                    if(line==null)break;
                    result+=line;
                    Log.e(LOG,"PostGroupTableInfo : " + line );
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(result.contains("fail")){
                Log.e(LOG,"PutGroupTableInfo error");
            }
            asyncProgressDialog.dismiss();
        }
    }

    private class SendPost extends AsyncTask<String,Integer,String> {
        String tablename=new String();
        String result=new String();
        @Override
        protected String doInBackground(String... urls) {
            try {
                int countLine = 0;
                URL url = new URL("http://pama.dothome.co.kr/getNumName.php");
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
                    if(countLine == 0){
                        result += line;
                        countLine++;
                        continue;
                    }
                    tablename += line;
                }
                http.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            JSONArray jsonArray;

            if (result.matches("[0-5]")) {
                if(Integer.parseInt(result)!=0){
                    if(result == null){
                        Toast.makeText(getApplicationContext(),"실패", Toast.LENGTH_SHORT);
                    }else {
                        try {
                            jsonArray = new JSONArray(tablename);
                            for (int i = 0; i < Integer.parseInt(result); i++) {
                                String txt;
                                JSONObject order = jsonArray.getJSONObject(i);
                                txt = order.getString("tableName");
                                addGroup_Tables[i].setText(txt);
                                addGroup_Tables[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        addGroup_Tables[selectedTimeTable].setTextColor(Color.argb(100, 100, 100, 100));
                                        selectedTimeTable = v.getId();
                                        addGroup_Tables[selectedTimeTable].setTextColor(Color.argb(255, 255, 0, 0));
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            } else
                Toast.makeText(getApplicationContext(), "시간표 불러오기 실패", Toast.LENGTH_SHORT).show();
            asyncProgressDialog.dismiss();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode==1){
            clickedNicknames=data.getStringArrayListExtra("nicknames");
            clickedIds=data.getStringArrayListExtra("ids");
            if(clickedNicknames.size()!=0){
                addGroup_addFriend_Btn.setText("");
                addGroup_addFriend_Btn.setText(clickedNicknames.size()+"명 추가");
            }else{
                addGroup_addFriend_Btn.setText("친구 추가");
            }
        }
    }

    private void makeGroupImgView(){
        for(int i=0;i<=(groupGroupInfo.IMGNUM-1)/6;i++) {
            LinearLayout linearLayout=new LinearLayout(getApplicationContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,TimeTableInfo.dpToPx(getApplicationContext(), 60));
            linearLayout.setLayoutParams(layoutParams);
            for(int j=0;j<(groupGroupInfo.IMGNUM)-i*6&&j<6;j++){
                LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(TimeTableInfo.dpToPx(getApplicationContext(), 56),TimeTableInfo.dpToPx(getApplicationContext(), 56));
                layoutParams.setMargins(TimeTableInfo.dpToPx(getApplicationContext(), 2),TimeTableInfo.dpToPx(getApplicationContext(), 2),TimeTableInfo.dpToPx(getApplicationContext(), 2),TimeTableInfo.dpToPx(getApplicationContext(), 2));
                final ImageView tempImageView=new ImageView(getApplicationContext());
                tempImageView.setLayoutParams(layoutParams1);
                tempImageView.setBackgroundResource(groupGroupInfo.GROUP.get("GROUP"+(i*6+j+1)));
                linearLayout.addView(tempImageView);
                tempImageView.setId(i*6+j);

                tempImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ImageView tempImageView=groupImages.get(selectedImg);
                        tempImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),groupGroupInfo.GROUP.get("GROUP"+(selectedImg+1))));

                        selectedImg=v.getId();

                        ImageView tempImageView2=groupImages.get(selectedImg);
                        int tempCurrentImgCode=groupGroupInfo.GROUP.get("GROUP"+(selectedImg+1));
                        Bitmap getCurrentBitmap=BitmapFactory.decodeResource(getResources(),tempCurrentImgCode);
                        Bitmap tempCurrentBitmap=Bitmap.createBitmap(getCurrentBitmap.getWidth(),getCurrentBitmap.getHeight(), Bitmap.Config.RGB_565);
                        Canvas currentCanvas=new Canvas(tempCurrentBitmap);
                        currentCanvas.drawBitmap(getCurrentBitmap,0,0,null);

                        tempImageView2.setImageBitmap(tempCurrentBitmap);

                    }
                });

                groupImages.add(tempImageView);
            }
            addGroup_GroupImg_LinearLayout.addView(linearLayout);
        }
        int tempCurrentImgCode=groupGroupInfo.GROUP.get("GROUP"+(selectedImg+1));
        Bitmap getCurrentBitmap=BitmapFactory.decodeResource(getResources(),tempCurrentImgCode);
        Bitmap tempCurrentBitmap=Bitmap.createBitmap(getCurrentBitmap.getWidth(),getCurrentBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas currentCanvas=new Canvas(tempCurrentBitmap);
        currentCanvas.drawBitmap(getCurrentBitmap,0,0,null);

        groupImages.get(0).setImageBitmap(tempCurrentBitmap);

    }
    void makeAddGroupTables(){
        TextView textView=(TextView)findViewById(R.id.addGroup_Table1_Txt);
        textView.setTextColor(Color.argb(255,255,0,0));
        textView.setId(0);
        addGroup_Tables[0]=textView;
        textView=(TextView)findViewById(R.id.addGroup_Table2_Txt);
        textView.setId(1+0);
        addGroup_Tables[1]=textView;
        textView=(TextView)findViewById(R.id.addGroup_Table3_Txt);
        textView.setId(2+0);
        addGroup_Tables[2]=textView;
        textView=(TextView)findViewById(R.id.addGroup_Table4_Txt);
        textView.setId(3+0);
        addGroup_Tables[3]=textView;
        textView=(TextView)findViewById(R.id.addGroup_Table5_Txt);
        textView.setId(4+0);
        addGroup_Tables[4]=textView;
    }
}
