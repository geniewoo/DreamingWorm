package dreamingworm.grouptimetable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sungwoo on 2016-07-01.
 */

public class FindIDDialog extends Dialog {
    private String phone;
    private String result;
    String LOG="FindIDDialog";
    private Button findIDDiaglog_Find_Btn;
    private EditText findIDDialog_Phone_Edt;
    private TextView findIDDiaglog_EditText_Txt;
    private Context context;

    public FindIDDialog(final Context context) {
        super(context);
        this.context=context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_findid);
        Log.e(LOG,"start");

        findIDDiaglog_Find_Btn=(Button)findViewById(R.id.findIDDiaglog_Find_Btn);
        findIDDialog_Phone_Edt=(EditText)findViewById(R.id.findIDDiaglog_Phone_Edt);
        findIDDiaglog_EditText_Txt=(TextView)findViewById(R.id.findIDDialog_EditText_Txt);
        findIDDiaglog_Find_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(context)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(context);
                    networkConnDialog.show();
                    return;
                }
                new SendPost().execute();
                phone=findIDDialog_Phone_Edt.getText().toString();
            }
        });

        Log.e(LOG,"finished");
    }

    private class SendPost extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/searchID.php");
                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("phone",phone);
                String query = builder.build().getEncodedQuery();
                OutputStream outStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(),"UTF-8"));
                Log.e(LOG,"bufferedReader open");
                while(true){
                    String line = br.readLine();
                    Log.e(LOG,"bufferedReader getLine");
                    if(line == null) break;
                    Log.d("get",line);
                    result = line;
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
            Log.e(LOG,"onPost");
            Log.e(LOG,"result : "+result);
            if(!result.equals("fail")&&!result.equals("<br>")){
                //받은값 id로 넘겨주고 text랑 button바꾸기
                findIDDialog_Phone_Edt.setText("찾으신 ID는 "+result+"입니다.");
                findIDDialog_Phone_Edt.setEnabled(false);
                findIDDiaglog_Find_Btn.setText("확인");
                findIDDiaglog_EditText_Txt.setVisibility(View.GONE);
                findIDDiaglog_Find_Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FindIDDialog.this.dismiss();
                    }
                });
            } else Toast.makeText(context,"등록된 번호가 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismiss();
    }
}
