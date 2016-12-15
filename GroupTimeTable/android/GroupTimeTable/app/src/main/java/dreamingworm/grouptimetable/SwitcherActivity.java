package dreamingworm.grouptimetable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sungwoo on 2016-07-15.
 */
public class SwitcherActivity extends AppCompatActivity {
    long doubleTouch=0;
    private Button switcher_MyTable_Btn;
    private Button switcher_Friend_Btn;
    private Button switcher_Group_Btn;
    private Button switcher_Setting_Btn;
    private String id;

    MyTableLayout switcher_myTable_Layout;
    FriendLayout switcher_Friend_Layout;
    SettingLayout switcher_Setting_Layout;
    GroupLayout switcher_Group_Layout;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switcher);

        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        switcher_myTable_Layout=(MyTableLayout)findViewById(R.id.switcher_MyTable_Layout);
        switcher_Friend_Layout=(FriendLayout)findViewById(R.id.switcher_Friend_Layout);
        switcher_Setting_Layout=(SettingLayout)findViewById(R.id.switcher_Setting_Layout);
        switcher_Group_Layout=(GroupLayout)findViewById(R.id.switcher_Group_Layout);
        switcher_Friend_Layout.setVisibility(View.GONE);
        switcher_Setting_Layout.setVisibility(View.GONE);
        switcher_Group_Layout.setVisibility(View.GONE);
        switcher_myTable_Layout.setVisibility(View.VISIBLE);

        final Intent getIn =getIntent();
        id = getIn.getStringExtra("id").toString();
        switcher_MyTable_Btn = (Button)findViewById(R.id.switcher_MyTable_Btn);
        switcher_Friend_Btn = (Button)findViewById(R.id.switcher_Friend_Btn);
        switcher_Group_Btn = (Button)findViewById(R.id.switcher_Group_Btn);
        switcher_Setting_Btn = (Button)findViewById(R.id.switcher_Setting_Btn);
        switcher_MyTable_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher_Friend_Layout.setVisibility(View.GONE);
                switcher_Setting_Layout.setVisibility(View.GONE);
                switcher_Group_Layout.setVisibility(View.GONE);
                switcher_myTable_Layout.setVisibility(View.VISIBLE);
            }
        });
        switcher_Friend_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher_myTable_Layout.setVisibility(View.GONE);
                switcher_Setting_Layout.setVisibility(View.GONE);
                switcher_Group_Layout.setVisibility(View.GONE);
                switcher_Friend_Layout.setVisibility(View.VISIBLE);
            }
        });

        switcher_Group_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher_myTable_Layout.setVisibility(View.GONE);
                switcher_Friend_Layout.setVisibility(View.GONE);
                switcher_Setting_Layout.setVisibility(View.GONE);
                switcher_Group_Layout.setVisibility(View.VISIBLE);
            }
        });
        switcher_Setting_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher_myTable_Layout.setVisibility(View.GONE);
                switcher_Group_Layout.setVisibility(View.GONE);
                switcher_Friend_Layout.setVisibility(View.GONE);
                switcher_Setting_Layout.setVisibility(View.VISIBLE);
            }
        });

        ////만약 목록이 1개 이상이면 대화창 시작/////
    }
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-doubleTouch<700){
            super.onBackPressed();
        }else{
            doubleTouch=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"한번 더 뒤로가기를 누르면 앱이 종료됩니다.",Toast.LENGTH_LONG).show();
        }
    }

}