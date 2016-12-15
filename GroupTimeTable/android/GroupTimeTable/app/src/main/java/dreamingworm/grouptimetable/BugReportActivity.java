package dreamingworm.grouptimetable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sungwoo on 2016-08-12.
 */
public class BugReportActivity extends AppCompatActivity{
    String LOG="BugReportActivity";

    Button bugReport_Send_Btn;
    EditText bugReport_Content_Edt;
    String content;
    String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugreport);

        bugReport_Content_Edt=(EditText)findViewById(R.id.bugReport_content_Edt);
        bugReport_Send_Btn=(Button)findViewById(R.id.bugReport_Send_Btn);
        id=getIntent().getStringExtra("id");

        bugReport_Send_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(BugReportActivity.this);

                builder.setCustomTitle(null);
                builder.setMessage("버그리포트를 전송하시겠습니까?");
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        content=bugReport_Content_Edt.getText().toString();
                        new SendReport().execute();
                    }
                });
                builder.create().show();
            }
        });
    }
    class SendReport extends AsyncTask<String,String,String>{
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(BugReportActivity.this);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(),"정상적으로 전송되었습니다",Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/sendReport.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("id", id).appendQueryParameter("content",content);
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
                    Log.e(LOG,"sendReport line : "+line);
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            asyncProgressDialog.dismiss();
            return null;
        }
    }
}
