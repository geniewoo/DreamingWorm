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
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Youngs on 2016-08-04.
 */
public class GroupSettingActivity extends AppCompatActivity {
    AsyncProgressDialog asyncProgressDialog;
    String LOG="groupSettingActivity";
    CountDownLatch latch=new CountDownLatch(1);
    ArrayList<String> clickedNicknames=new ArrayList<String>();
    ArrayList<String> clickedIds=new ArrayList<String>();
    int groupImg;
    String id;
    String groupSN;
    String groupName;
    ArrayList<String> nickname = new ArrayList<String>();
    int currentTabNum;
    int tableNum;
    Button groupSetting_Create_Btn;
    Button groupSetting_Cancel_Btn;
    EditText groupSetting_Name_Edt;
    Button groupSetting_addFriend_Btn;
    LinearLayout groupSetting_GroupImg_LinearLayout;
    LinearLayout groupSetting_TabImg_LinearLayout;
    IMGINFO imginfo = new IMGINFO();
    GroupGroupInfo groupGroupInfo=new GroupGroupInfo();
    ArrayList<ImageView> groupImages=new ArrayList<ImageView>();
    ArrayList<ImageView> tabImages=new ArrayList<ImageView>();
    TextView groupSetting_Tables[]=new TextView[5];
    int selectedImg=0;
    int selectedTimeTable=0;
    int selectedTab=0;
    char isBusy[]=new char[392];
    ArrayList<Integer> tabImg = new ArrayList<Integer>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupsetting);
        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        asyncProgressDialog=new AsyncProgressDialog(GroupSettingActivity.this);

        groupSetting_addFriend_Btn=(Button)findViewById(R.id.groupSetting_AddFriend_Btn);
        groupSetting_GroupImg_LinearLayout=(LinearLayout) findViewById(R.id.groupSetting_GroupImg_LinearLayout);
        groupSetting_TabImg_LinearLayout=(LinearLayout) findViewById(R.id.groupSetting_TabImg_LinearLayout);
        groupSetting_Name_Edt=(EditText)findViewById(R.id.groupSetting_Name_Edt);
        groupSetting_Create_Btn=(Button)findViewById(R.id.groupSetting_Create_Btn);
        groupSetting_Cancel_Btn=(Button)findViewById(R.id.groupSetting_Cancel_Btn);

        id=getIntent().getStringExtra("id");
        groupSN = getIntent().getStringExtra("groupSN");
        currentTabNum=getIntent().getIntExtra("currentTabNum",0);
        groupImg=getIntent().getIntExtra("groupImg",0);
        nickname = getIntent().getStringArrayListExtra("nickname");
        groupName = getIntent().getStringExtra("groupName");
        tableNum = getIntent().getIntExtra("tableNum",0);
        tabImg = getIntent().getIntegerArrayListExtra("tabImg");
        groupSetting_Name_Edt.setText(groupName);
        selectedImg = groupImg;
        selectedTimeTable = tableNum;
        selectedTab = currentTabNum;
        Log.d("twat tabNum : ", String.valueOf(currentTabNum));
        for(int i = 0; i<tabImg.size();i++){
            Log.d("twat tabImg : ", String.valueOf(tabImg.get(i)));
        }
        makegroupSettingTables();
        makeGroupImgView();
        makegroupSettingTabs();

        groupSetting_addFriend_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AddGroupFriendActivity.class);
                intent.putExtra("id",id);
                if(nickname.size()!=0) {
                    intent.putExtra("nickname", nickname);
                }
                if(clickedNicknames.size()!=0){
                    intent.putExtra("nicknames",clickedNicknames);
                    intent.putExtra("ids",clickedIds);
                }
                startActivityForResult(intent,1);
                overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        groupSetting_Create_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(GroupSettingActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(GroupSettingActivity.this);
                    networkConnDialog.show();
                    return;
                }

                new MakeGroupTableInfo().execute();

            }
        });
        groupSetting_Cancel_Btn.setOnClickListener(new View.OnClickListener() {
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
        int nicknameSize;
        String jsValue2;
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
                tableInfoObject.put("groupSN",groupSN);
                jsValue=tableInfoObject.toString();
            } catch (Exception e) {
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
                nicknamesJO.put("groupSN",groupSN);
                jsValue2=nicknamesJO.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            roomName=groupSetting_Name_Edt.getText().toString();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/changeGroupTableInfo.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("json", jsValue)
                        .appendQueryParameter("json2", jsValue2)
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("tableImg", String.valueOf(selectedImg+1))
                        .appendQueryParameter("tabNum", String.valueOf(selectedTab+1))
                        .appendQueryParameter("num",String.valueOf(selectedTimeTable))
                        .appendQueryParameter("roomName",roomName);
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
                    Log.e(LOG,"ChangeGroupTableInfo : " + line );
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
                Log.e(LOG,"ChangeGroupTableInfo error");
            }else{
                Log.e(LOG,"ChangeGroupTableInfo success");
                Intent setIntent = new Intent(getApplicationContext(), GroupSettingActivity.class);
                setIntent.putExtra("currentTabNum", selectedTab);
                setIntent.putExtra("groupName", groupName);
                setIntent.putExtra("groupImg", selectedImg);
                setIntent.putExtra("tableNum", selectedTimeTable);
                setResult(2, setIntent);
                finish();
                overridePendingTransition(R.anim.hold,R.anim.hold);
            }
            asyncProgressDialog.dismiss();
        }
    }
    private class SendPost extends AsyncTask<String,Integer,String> {
        String tablename=new String();
        String result=new String();

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

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
                    Log.d("twat groupsetting",line);
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
                                groupSetting_Tables[i].setText(txt);
                                groupSetting_Tables[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        groupSetting_Tables[selectedTimeTable].setTextColor(Color.argb(100, 100, 100, 100));
                                        selectedTimeTable = v.getId();
                                        groupSetting_Tables[selectedTimeTable].setTextColor(Color.argb(255, 255, 0, 0));
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
                groupSetting_addFriend_Btn.setText("");
                groupSetting_addFriend_Btn.setText(clickedNicknames.size()+"명 추가");
            }else{
                groupSetting_addFriend_Btn.setText("친구 추가");
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
            groupSetting_GroupImg_LinearLayout.addView(linearLayout);
        }
        int tempCurrentImgCode=groupGroupInfo.GROUP.get("GROUP"+(selectedImg+1));
        Bitmap getCurrentBitmap=BitmapFactory.decodeResource(getResources(),tempCurrentImgCode);
        Bitmap tempCurrentBitmap=Bitmap.createBitmap(getCurrentBitmap.getWidth(),getCurrentBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas currentCanvas=new Canvas(tempCurrentBitmap);
        currentCanvas.drawBitmap(getCurrentBitmap,0,0,null);

        groupImages.get(selectedImg).setImageBitmap(tempCurrentBitmap);

    }

    void makegroupSettingTables(){
        TextView textView=(TextView)findViewById(R.id.groupSetting_Table1_Txt);
        textView.setId(0);
        groupSetting_Tables[0]=textView;
        textView=(TextView)findViewById(R.id.groupSetting_Table2_Txt);
        textView.setId(1+0);
        groupSetting_Tables[1]=textView;
        textView=(TextView)findViewById(R.id.groupSetting_Table3_Txt);
        textView.setId(2+0);
        groupSetting_Tables[2]=textView;
        textView=(TextView)findViewById(R.id.groupSetting_Table4_Txt);
        textView.setId(3+0);
        groupSetting_Tables[3]=textView;
        textView=(TextView)findViewById(R.id.groupSetting_Table5_Txt);
        textView.setId(4+0);
        groupSetting_Tables[4]=textView;
        groupSetting_Tables[tableNum].setTextColor(Color.argb(255,255,0,0));
    }

    private void makegroupSettingTabs() {
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TimeTableInfo.dpToPx(getApplicationContext(), 60));
        linearLayout.setLayoutParams(layoutParams);
        for (int j = 0;j < tabImg.size(); j++) {
            LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(TimeTableInfo.dpToPx(getApplicationContext(), 56), TimeTableInfo.dpToPx(getApplicationContext(), 56));
            layoutParams.setMargins(TimeTableInfo.dpToPx(getApplicationContext(), 2), TimeTableInfo.dpToPx(getApplicationContext(), 2), TimeTableInfo.dpToPx(getApplicationContext(), 2), TimeTableInfo.dpToPx(getApplicationContext(), 2));
            final ImageView tempImageView = new ImageView(getApplicationContext());
            tempImageView.setLayoutParams(layoutParams1);
            tempImageView.setBackgroundResource(imginfo.TAB.get("IMG" + tabImg.get(j)));
            linearLayout.addView(tempImageView);
            tempImageView.setId(j);

            tempImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ImageView tempImageView = tabImages.get(selectedTab);
                    tempImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), imginfo.TAB.get("IMG" + tabImg.get(selectedTab))));

                    selectedTab = v.getId();

                    ImageView tempImageView2 = tabImages.get(selectedTab);
                    int tempCurrentImgCode = imginfo.TAB.get("IMG" + tabImg.get(selectedTab));
                    Bitmap getCurrentBitmap = BitmapFactory.decodeResource(getResources(), tempCurrentImgCode);
                    Bitmap tempCurrentBitmap = Bitmap.createBitmap(getCurrentBitmap.getWidth(), getCurrentBitmap.getHeight(), Bitmap.Config.RGB_565);
                    Canvas currentCanvas = new Canvas(tempCurrentBitmap);
                    currentCanvas.drawBitmap(getCurrentBitmap, 0, 0, null);

                    tempImageView2.setImageBitmap(tempCurrentBitmap);

                }
            });
            tabImages.add(tempImageView);
        }
        groupSetting_TabImg_LinearLayout.addView(linearLayout);
        int tempCurrentImgCode = imginfo.TAB.get("IMG" + tabImg.get(selectedTab));
        Bitmap getCurrentBitmap = BitmapFactory.decodeResource(getResources(), tempCurrentImgCode);
        Bitmap tempCurrentBitmap = Bitmap.createBitmap(getCurrentBitmap.getWidth(), getCurrentBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas currentCanvas = new Canvas(tempCurrentBitmap);
        currentCanvas.drawBitmap(getCurrentBitmap, 0, 0, null);

        tabImages.get(selectedTab).setImageBitmap(tempCurrentBitmap);
    }

    static public class IMGINFO{
        int IMGNUM;
        HashMap<String,Integer> TAB;

        IMGINFO(){
            TAB=new HashMap<String,Integer>();
            TAB.put("IMG1",R.drawable.tab1);
            TAB.put("IMG2",R.drawable.tab2);
            TAB.put("IMG3",R.drawable.tab3);
            TAB.put("IMG4",R.drawable.tab4);

            IMGNUM=TAB.size();
        }
    }

}
