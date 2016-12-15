package dreamingworm.grouptimetable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import java.net.URL;
import java.util.ArrayList;

import dreamingworm.grouptimetable.CustomViews.CustomGreenListTextView;
import dreamingworm.grouptimetable.CustomViews.CustomGreenStrokeListTextView;

/**
 * Created by sungwoo on 2016-07-15.
 */
public class MyTableLayout extends LinearLayout {
    boolean isFirst=true;
    String LOG="MyTableLayout";
    boolean isClick=false;
    boolean isProgressDialog=false;
    boolean lock = false;
    private Context contexT;
    private String result;
    private ListView myTable_Table_List;
    private MyTableAdapter adapter;
    private ArrayList<String> arrayList;
    private JSONArray jsonArray;
    private String tablename;
    private String id;

    public MyTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.contexT=context;
        init();
    }

    public MyTableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.contexT=context;
        init();
    }

    public MyTableLayout(Context context) {
        super(context);
        this.contexT=context;
        init();
    }

    private void init(){
        LayoutInflater inflater=(LayoutInflater.from(contexT));
        inflater.inflate(R.layout.layout_mytable,this,true);
    }

    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
        if(getVisibility()==GONE){
            if(!isFirst){
                isProgressDialog=false;
                return;
            }else {
                isFirst = false;
            }
        }
        if(isClick){
            isClick=false;
            return;
        }
        if (!NetworkConn.isNetworkConnected(contexT)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
            networkConnDialog.show();
            return;
        }
        Log.e(LOG,"isProgressDialog : "+isProgressDialog);
        if(isProgressDialog){
            isProgressDialog=false;
            Log.e(LOG,"progressDialog");
            return;
        }

        SharedPreferences preferences= contexT.getSharedPreferences("environ",0);
        id=preferences.getString("ID","");

        myTable_Table_List =(ListView)findViewById(R.id.myTable_Table_ListView);

        new SendPost().execute();
    }

    private class MyTableAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout frameLayout = new FrameLayout(contexT);
            TextView textView;
            if(Integer.parseInt(result)<5&&position==arrayList.size()-1){
                textView = new CustomGreenStrokeListTextView(contexT);
            }else {
                textView = new CustomGreenListTextView(contexT);
            }
            textView.setText(arrayList.get(position));
            frameLayout.addView(textView);
            return frameLayout;
        }
    }

    private class SendPost extends AsyncTask<String,Integer,String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(contexT);
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
                    if(countLine == 0){
                        result = line;
                        countLine++;
                        continue;
                    }
                    tablename = line;
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
            arrayList = new ArrayList<>();
            adapter = new MyTableAdapter();
            myTable_Table_List.setAdapter(adapter) ;


            AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long onclickId) {
                    int count = adapter.getCount();
                    Intent in = new Intent(contexT,AddTableActivity.class);
                    in.putExtra("id",id);
                    in.putExtra("max",Integer.parseInt(result));
                    if(count == position+1 && Integer.parseInt(result) != 5){
                        in.putExtra("num",0);
                        contexT.startActivity(in);
                        ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
                    }
                    else {
                        in.putExtra("num", position + 1);
                        contexT.startActivity(in);
                        ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
                    }
                    isClick=true;
                }
            };
            myTable_Table_List.setOnItemClickListener(itemClickListener);
            if (result.matches("[0-5]")) {
                if(Integer.parseInt(result)!=0){
                    if(result == null){
                        Toast.makeText(contexT,"실패", Toast.LENGTH_SHORT);
                    }else{
                        try {
                            jsonArray = new JSONArray(tablename);
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
                for (int i = 0; i < Integer.parseInt(result); i++) {
                    String txt;
                    try{
                        JSONObject order = jsonArray.getJSONObject(i);
                        txt = order.getString("tableName");
                        arrayList.add(txt);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                }
                if(Integer.parseInt(result) < 5 ){
                    arrayList.add("+");
                    adapter.notifyDataSetChanged();
                }
            } else
                Toast.makeText(contexT, "시간표 불러오기 실패", Toast.LENGTH_SHORT).show();
            lock = false;
            isProgressDialog=true;
            asyncProgressDialog.dismiss();
        }
    }
}
