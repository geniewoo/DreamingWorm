package dreamingworm.grouptimetable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
 * Created by sungwoo on 2016-08-01.
 */
public class InvitedActivity extends AppCompatActivity {
    AsyncProgressDialog asyncProgressDialog;
    String LOG = "InvitedActivity";
    InvitedAdapter invitedAdapter;
    CountDownLatch latch = new CountDownLatch(1);
    ListView invited_List_ListView;
    Button invited_Close_Btn;
    String id;
    ArrayList<GroupTab> groupTabs;
    ArrayList<String> myTableNames;
    GroupLayout.IMGINFO imginfo = new GroupLayout.IMGINFO();
    int currentTab = -1;
    int currentMyTable = -1;
    char isBusy[] = new char[392];
    InvitedItem currentItem;

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_invited);

        asyncProgressDialog=new AsyncProgressDialog(InvitedActivity.this);

        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }


        invited_List_ListView = (ListView) findViewById(R.id.invited_List_ListView);
        invited_Close_Btn = (Button) findViewById(R.id.invited_Close_Btn);
        invitedAdapter = new InvitedAdapter();

        id = getIntent().getStringExtra("id");
        ArrayList<InvitedItem> invitedItems = (ArrayList<InvitedItem>) getIntent().getSerializableExtra("invitedItems");
        Log.e(LOG, "invitedItems Size : " + invitedItems.size());
        invitedAdapter.setItems(invitedItems);

        invited_List_ListView.setAdapter(invitedAdapter);

        invited_Close_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LoginIntent = new Intent(getApplicationContext(), SwitcherActivity.class);
                LoginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LoginIntent.putExtra("id", id);
                startActivity(LoginIntent);
                overridePendingTransition(R.anim.hold, R.anim.hold);
            }
        });
    }

    class InvitedAdapter extends BaseAdapter {
        private ArrayList<InvitedItem> items = new ArrayList<>();

        @Override
        public int getCount() {
            Log.e(LOG, "items size : " + items.size());
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_invited, parent, false);
            }
            TextView invited_roomName_Txt = (TextView) convertView.findViewById(R.id.invited_RoomName_Txt);
            TextView invited_HostName_Txt = (TextView) convertView.findViewById(R.id.invited_HostName_Txt);
            final Button invited_Confirm_Btn = (Button) convertView.findViewById(R.id.invited_Confirm_Btn);
            Button invited_Refuse_Btn = (Button) convertView.findViewById(R.id.invited_Refuse_Btn);

            invited_Confirm_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkConn.isNetworkConnected(InvitedActivity.this)) {
                        Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(InvitedActivity.this);
                        networkConnDialog.show();
                        return;
                    }
                    currentItem = (InvitedItem) getItem(position);
                    Log.e(LOG, "ConfirmBtnClicked");
                    new SendPost().execute();
                }
            });

            invited_Refuse_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentItem = (InvitedItem) getItem(position);
                    Log.e(LOG, "RefuseBtnClicked");
                    new DeleteInviteInvitedList().execute();
                }
            });

            invited_roomName_Txt.setText(items.get(position).getRoomName());
            invited_HostName_Txt.setText(items.get(position).getHostNickname() + "(" + items.get(position).getHostName() + ")");
            return convertView;
        }

        public ArrayList<InvitedItem> getItems() {
            return items;
        }

        public void setItems(ArrayList<InvitedItem> items) {
            this.items = items;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private class SendPost extends AsyncTask<String, Integer, String> {
        String tablename = new String();
        String result = new String();

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
                    if (countLine == 0) {
                        result += line;
                        countLine++;
                        continue;
                    }
                    tablename += line;
                    Log.e(LOG, "SendPost : " + line);
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

            myTableNames = new ArrayList<>();
            if (result.matches("[0-5]")) {
                if (Integer.parseInt(result) != 0) {
                    if (result == null) {
                        Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT);
                    } else {
                        try {
                            if (tablename.equals("")) {
                                Toast.makeText(getApplicationContext(), "나의 테이블이 없습니다.", Toast.LENGTH_LONG).show();
                            }
                            jsonArray = new JSONArray(tablename);
                            for (int i = 0; i < Integer.parseInt(result); i++) {
                                String txt;
                                JSONObject order = jsonArray.getJSONObject(i);
                                myTableNames.add(order.getString("tableName"));
                            }
                            latch.countDown();
                            new GetTabAsynk().execute();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            } else {
                Log.e(LOG, "SendPost error");
            }
        }

    }

    private class GetTabAsynk extends AsyncTask<String, String, String> {

        String gottenTab = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                latch.await(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch = new CountDownLatch(1);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                groupTabs = new ArrayList<GroupTab>();
                if (!gottenTab.contains("fail")) {
                    JSONArray jsonArray = new JSONArray(gottenTab);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int tabImgCode = jsonObject.getInt("tabImg");
                        int tabNum = jsonObject.getInt("tabNum");
                        addTabArrayCroppedBitmap(imginfo.TAB.get("IMG" + tabImgCode));
                        addTabArray(tabImgCode, tabNum);
                    }
                    new InvitedDialog(InvitedActivity.this).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            asyncProgressDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/getGroupTab.php");
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
                    gottenTab += line;
                    Log.e(LOG, "GetTabAsynk");
                }
                http.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class InvitedDialog extends Dialog {

        boolean isSuccess = false;
        ImageView invitedTabImgVIews[] = new ImageView[5];
        TextView invitedMyTableTxts[] = new TextView[5];
        Button invited_DialogConfirmBtn;
        Button invited_DialogCancelBtn;
        TextView invitedRoomNameTxt;
        TextView invitedHostNmaeTxt;

        public InvitedDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_invited);

            invitedRoomNameTxt = (TextView) findViewById(R.id.invited_RoomName_Txt);
            invitedHostNmaeTxt = (TextView) findViewById(R.id.invited_HostName_Txt);

            invitedRoomNameTxt.setText(currentItem.getRoomName());
            invitedHostNmaeTxt.setText(currentItem.getHostNickname() + "(" + currentItem.getHostName() + ")");

            ImageView imageView = (ImageView) findViewById(R.id.invited_Tab1_ImgView);
            imageView.setId(0);
            invitedTabImgVIews[0] = imageView;
            imageView = (ImageView) findViewById(R.id.invited_Tab2_ImgView);
            imageView.setId(1 + 0);
            invitedTabImgVIews[1] = imageView;
            imageView = (ImageView) findViewById(R.id.invited_Tab3_ImgView);
            imageView.setId(2 + 0);
            invitedTabImgVIews[2] = imageView;
            imageView = (ImageView) findViewById(R.id.invited_Tab4_ImgView);
            imageView.setId(3 + 0);
            invitedTabImgVIews[3] = imageView;
            imageView = (ImageView) findViewById(R.id.invited_Tab5_ImgView);
            imageView.setId(4 + 0);
            invitedTabImgVIews[4] = imageView;

            TextView textView = (TextView) findViewById(R.id.invited_MyTable1_Txt);
            textView.setId(0 + 0);
            invitedMyTableTxts[0] = textView;
            textView = (TextView) findViewById(R.id.invited_MyTable2_Txt);
            textView.setId(1 + 0);
            invitedMyTableTxts[1] = textView;
            textView = (TextView) findViewById(R.id.invited_MyTable3_Txt);
            textView.setId(2 + 0);
            invitedMyTableTxts[2] = textView;
            textView = (TextView) findViewById(R.id.invited_MyTable4_Txt);
            textView.setId(3 + 0);
            invitedMyTableTxts[3] = textView;
            textView = (TextView) findViewById(R.id.invited_MyTable5_Txt);
            textView.setId(4 + 0);
            invitedMyTableTxts[4] = textView;

            currentTab=0;
            invitedTabImgVIews[currentTab].setBackgroundColor(Color.CYAN);

            for (int i = 0; i < groupTabs.size(); i++) {

                invitedTabImgVIews[i].setImageBitmap(groupTabs.get(i).tabImg);
                invitedTabImgVIews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = v.getId();
                        invitedTabImgVIews[currentTab].setBackgroundColor(Color.TRANSPARENT);
                        currentTab = index;
                        invitedTabImgVIews[currentTab].setBackgroundColor(Color.CYAN);
                    }
                });
            }

            currentMyTable=0;

            for (int i = 0; i < myTableNames.size(); i++) {
                invitedMyTableTxts[i].setText(myTableNames.get(i));
                if(i==currentMyTable){
                    invitedMyTableTxts[i].setTextColor(Color.argb(255, 255, 0, 0));
                }else{
                    invitedMyTableTxts[i].setTextColor(Color.argb(255, 200, 200, 200));
                }
                invitedMyTableTxts[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = v.getId();

                        invitedMyTableTxts[currentMyTable].setTextColor(Color.argb(255, 200, 200, 200));
                        currentMyTable = index;
                        invitedMyTableTxts[currentMyTable].setTextColor(Color.argb(255, 255, 0, 0));
                    }
                });
            }

            invited_DialogConfirmBtn = (Button) findViewById(R.id.invited_DialogConfirm_Btn);
            invited_DialogConfirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkConn.isNetworkConnected(InvitedActivity.this)) {
                        Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(InvitedActivity.this);
                        networkConnDialog.show();
                        return;
                    }
                    acceptInvite();
                }
            });
            invited_DialogCancelBtn = (Button) findViewById(R.id.invited_DialogCancel_Btn);
            invited_DialogCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (isSuccess) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                invitedAdapter.getItems().remove(currentItem);
                                invitedAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                }
            });
        }

        private void acceptInvite() {
            new GetBusyAsynk().execute();
        }

        private class GetBusyAsynk extends AsyncTask<String, String, String> {
            String tableName = new String();
            String result;

            @Override
            protected void onPreExecute() {
                asyncProgressDialog.show();
                super.onPreExecute();
                try {
                    latch.await(10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch = new CountDownLatch(1);
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
                            .appendQueryParameter("num", String.valueOf(currentMyTable + 1));
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
                        Log.e(LOG, "GetBusyAsynk : " + line);
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
                    isBusy = new char[392];
                    for (int i = 0; i < isBusy.length; i++) {
                        isBusy[i] = '0';
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject js = jsonArray.getJSONObject(i);
                            int row = js.getInt("row");
                            int col = js.getInt("column");
                            int size = js.getInt("size");

                            for (int j = 0; j < size; j++) {
                                isBusy[col * TimeTableInfo.SIZEY + row + j] = '1';
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (result.contains("fail")) {
                    Log.e(LOG, "GetBusyAsynk error");
                } else {
                    latch.countDown();
                    new PutGroupTableInfo().execute();
                }
            }
        }

        private class PutGroupTableInfo extends AsyncTask<String, Integer, String> {
            JSONObject tableInfoObject = new JSONObject();
            String jsValue;
            String jsValue2;
            String result = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    latch.await(10, TimeUnit.MILLISECONDS);
                    latch = new CountDownLatch(1);
                    String isBusyStr = new String(isBusy, 0, 196);
                    String isBusyStr2 = new String(isBusy, 196, 196);
                    tableInfoObject.put("tableInfo1", isBusyStr);
                    tableInfoObject.put("tableInfo2", isBusyStr2);
                    tableInfoObject.put("groupSN", String.valueOf(currentItem.getGroupSN()));
                    jsValue = tableInfoObject.toString();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("groupName", currentItem.getRoomName());
                    jsonObject.put("myTableNum", currentMyTable);
                    jsonObject.put("tabNum", groupTabs.get(currentTab).tabNum);
                    jsonObject.put("tableImg", currentItem.getGroupImg());
                    jsValue2 = jsonObject.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://pama.dothome.co.kr/putGroupTableInfo.php");
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setDefaultUseCaches(false);
                    http.setDoInput(true);
                    http.setDoOutput(true);
                    http.setRequestMethod("POST");
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("groupSN", currentItem.getGroupSN())
                            .appendQueryParameter("json", jsValue)
                            .appendQueryParameter("json2", jsValue2)
                            .appendQueryParameter("id", id)
                            .appendQueryParameter("type", "2");
                    System.out.println("id : " + id);
                    String query = builder.build().getEncodedQuery();
                    OutputStream outputStream = http.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    http.connect();

                    BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
                    while (true) {
                        String line = br.readLine();
                        if (line == null) break;
                        result += line;
                        Log.e(LOG, "PutGroupTableInfo : " + line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (result.contains("fail")) {
                    Log.e(LOG, "PutGroupTableInfo error");
                } else {
                    isSuccess = true;
                    dismiss();
                }
                asyncProgressDialog.dismiss();
            }
        }

    }

    class DeleteInviteInvitedList extends AsyncTask<String, String, String> {
        String result = "";

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/deleteInviteList.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("groupSN", currentItem.getGroupSN())
                        .appendQueryParameter("id", id);
                System.out.println("id : " + id);
                String query = builder.build().getEncodedQuery();
                OutputStream outputStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                    result += line;
                    Log.e(LOG, "DeleteInviteInvitedList : " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (result.startsWith("fail")) {
                Log.e(LOG, "DeleteInviteInvitedList error");
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        invitedAdapter.getItems().remove(currentItem);
                        invitedAdapter.notifyDataSetChanged();
                    }
                });
            }
            asyncProgressDialog.dismiss();
        }
    }

    class GroupTab {
        int tabNum;
        int tabImgCode;
        Bitmap tabImg;
    }

    public void addTabArrayCroppedBitmap(int imgCode) {
        Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(), imgCode);
        tempBitmap = getCroppedBitmap(tempBitmap);
        GroupTab groupTab = new GroupTab();
        groupTab.tabImg = tempBitmap;
        groupTabs.add(groupTab);
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private void addTabArray(int tabImgCode, int tabNum) {
        groupTabs.get(groupTabs.size() - 1).tabImgCode = tabImgCode;
        groupTabs.get(groupTabs.size() - 1).tabNum = tabNum;
    }
}