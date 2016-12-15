package dreamingworm.grouptimetable;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
 * Created by Young on 2016-07-20.
 */

public class AddTagDialog extends Dialog {
    private String id;
    private String tag;
    private String result;
    private String nickname;
    String LOG="AddTagDialog";
    private Button AddTagDialog_Summit_Btn;
    private Button AddTagDialog_Cancel_Btn;
    private EditText AddTagDialog_Tag_Txt;
    private Context context;
    public AddTagDialog(final Context context) {
        super(context);
        this.context=context;
        SharedPreferences preferences = context.getSharedPreferences("environ", 0);
        id = preferences.getString("ID", "");
        nickname = preferences.getString("nickname","");
        tag = preferences.getString("tag","");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_addtag);
        Log.e(LOG,"start");

        AddTagDialog_Summit_Btn=(Button)findViewById(R.id.addTagDiaglog_Summit_Btn);
        AddTagDialog_Cancel_Btn=(Button)findViewById(R.id.addTagDiaglog_Cancel_Btn);
        AddTagDialog_Tag_Txt=(EditText)findViewById(R.id.addTagDiaglog_Tag_Edt);
        AddTagDialog_Tag_Txt.setText(tag);
        AddTagDialog_Summit_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(context)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(context);
                    networkConnDialog.show();
                    return;
                }
                tag=AddTagDialog_Tag_Txt.getText().toString();
                new SendPost().execute();
            }
        });
        AddTagDialog_Cancel_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTagDialog.this.dismiss();
            }
        });

        Log.e(LOG,"finished");
    }

    private class SendPost extends AsyncTask<String,Integer,String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(context);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/putTag.php");
                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id",id)
                        .appendQueryParameter("nickname",nickname)
                        .appendQueryParameter("tag",tag);
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
            if(result.matches("success")){
                        AddTagDialog.this.dismiss();
            } else Toast.makeText(context,"태그 등록 실패",Toast.LENGTH_SHORT).show();
            asyncProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismiss();
    }
}
