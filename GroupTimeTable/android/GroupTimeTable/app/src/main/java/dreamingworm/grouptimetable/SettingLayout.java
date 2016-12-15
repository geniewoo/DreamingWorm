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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by sungwoo on 2016-07-15.
 */
public class SettingLayout extends LinearLayout{
    boolean isFirst=true;
    boolean isButton = false;
    boolean isProgressDialog=false;
    boolean lock;
    ImageView setting_Profile_View;
    Button setting_GetImage_Btn;
    Button setting_LogOut_Btn;
    Button setting_Invited_Btn;
    Button setting_Add_Btn;
    Button setting_BugReport_Btn;

    Context contexT;
    Bitmap image_bitmap;
    Bitmap raw_Image;
    String id;
    String fileName;
    public SettingLayout(Context context) {
        super(context);
        contexT=context;
    }

    public SettingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        contexT=context;
    }

    public SettingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contexT=context;
    }

    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
        if (!NetworkConn.isNetworkConnected(contexT)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
            networkConnDialog.show();
            return;
        }
        if(getVisibility()==GONE){
            if(!isFirst){
                isProgressDialog=false;
                return;
            }else{
                isFirst=false;
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
        raw_Image = BitmapFactory.decodeResource(getResources(),R.drawable.worm_sumnail);
        LayoutInflater inflater= LayoutInflater.from(contexT);
        inflater.inflate(R.layout.layout_setting,this,true);



        SharedPreferences preferences= contexT.getSharedPreferences("environ",0);
        id = preferences.getString("ID","");
        setting_GetImage_Btn = (Button)findViewById(R.id.setting_GetImage_Btn);
        setting_LogOut_Btn=(Button)findViewById(R.id.setting_LogOut_Btn);
        setting_Invited_Btn=(Button)findViewById(R.id.setting_Invited_Btn);
        setting_Add_Btn=(Button)findViewById(R.id.setting_Add_Btn);
        setting_Profile_View = (ImageView)findViewById(R.id.setting_Profile_View);
        setting_Profile_View.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        setting_BugReport_Btn=(Button)findViewById(R.id.setting_BugReport_Btn);

        setting_GetImage_Btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isButton=true;
                Intent intent = new Intent(contexT,ProfileActivity.class);
                contexT.startActivity(intent);
                ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        setting_LogOut_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isButton=true;
                SharedPreferences preferences= contexT.getSharedPreferences("environ",0);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean("LoginChk",false);
                editor.commit();
                Intent intent=new Intent(contexT,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                contexT.startActivity(intent);
                ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        setting_Invited_Btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isButton=true;
                if (!NetworkConn.isNetworkConnected(contexT)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
                    networkConnDialog.show();
                    return;
                }
                new GetInvitedList().execute();
            }
        });
        setting_Add_Btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isButton=true;
                Intent intent = new Intent(contexT,AddMeFriendActivity.class);
                contexT.startActivity(intent);
                ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        setting_BugReport_Btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isButton=true;
                Intent intent=new Intent(contexT,BugReportActivity.class);
                intent.putExtra("id",id);
                contexT.startActivity(intent);
                ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        new GetPost().execute();
    }

    class GetInvitedList extends AsyncTask<String,String,String>{
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(contexT);
        String result=new String();

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
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
                                Intent invitedIntent = new Intent(contexT,InvitedActivity.class);
                                invitedIntent.putExtra("id",id);
                                System.out.println(invitedItems.get(0).getGroupSN()+" "+invitedItems.get(0).getHostName()+" "+ invitedItems.get(0).getHostNickname()+" "+invitedItems.get(0).getRoomName());
                                invitedIntent.putExtra("invitedItems",invitedItems);
                                contexT.startActivity(invitedIntent);
                                ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
                            }
                        },10);
                    }else{
                        Toast.makeText(contexT,"초대 목록이 없습니다",Toast.LENGTH_LONG).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            asyncProgressDialog.dismiss();
        }
    }

    private class GetPost extends AsyncTask<String, Integer, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(contexT);
        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            fileName = id+"_sumnail.jpg";
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/"+fileName);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    http.connect();
                    InputStream is = http.getInputStream();
                    image_bitmap = BitmapFactory.decodeStream(is);
                    http.disconnect();
                }else{
                    image_bitmap = raw_Image;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setting_Profile_View.setImageBitmap(image_bitmap);
            lock = false;
            asyncProgressDialog.dismiss();
            isProgressDialog=true;
        }
    }
}