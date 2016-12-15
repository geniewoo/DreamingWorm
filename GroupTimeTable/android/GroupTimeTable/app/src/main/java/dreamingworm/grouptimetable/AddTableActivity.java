package dreamingworm.grouptimetable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Youngs on 2016-07-04.
 */
public class AddTableActivity extends AppCompatActivity {
    String LOG="AddTableActivity";
    private String id;
    private int num;
    private int max;
    private boolean edit = true;
    private String tableName;
    private Button addTable_Summit_Btn;
    private Button addTable_Delete_Btn;
    private EditText addTable_tableNAme_Edt;
    private JSONArray jsonArray;
    private JSONArray putJA;
    private JSONObject putJO;
    private JSONObject putTrans;
    private String jsValue;
    private String result;
    ///////////////////////////////////////For TimeTable/////////////////////
    private TimeTableGridLayout timeGridView;
    private FrameLayout frameLayout;
    private TableElementOriginImg imgViews[];
    private TextView daysTxt[];
    private TextView timesTxt[];
    private LinearLayout layout;
    private LinearLayout timeTable_Days_Layout;
    private LinearLayout timeTable_Times_Layout;
    private int lastJ = -1;
    private int touchI = -1;
    private int touchJ = -1;
    private int lastY = -1;
    private int touchX = -1;
    private int currentI;
    private int currentPivotY;
    private int screenTopY;
    private int screenBottomY;
    private int touchedPart;
    private long lastTouch = 0;
    private boolean isZoomed = false;
    private boolean isZoomIn = false;
    private boolean isZoomOut = false;
    private boolean isOverClicked = false;
    private boolean isElement;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtable);
        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }
        final Intent getIn = getIntent();
        id = getIn.getStringExtra("id");
        num = getIn.getIntExtra("num", 0);
        max = getIn.getIntExtra("max", 0);

        addTable_Summit_Btn = (Button) findViewById(R.id.addTable_Summit);
        addTable_Delete_Btn = (Button) findViewById(R.id.addTable_Delete);
        addTable_tableNAme_Edt = (EditText) findViewById(R.id.addTable_TableName);

        if (num == 0) {
            addTable_Summit_Btn.setText("시간표 추가");
            addTable_Delete_Btn.setText("취소");
            addTable_tableNAme_Edt.setText("시간표" + String.valueOf(max + 1));
            num = max + 1;
            edit = false;
        } else {
            addTable_Summit_Btn.setText("시간표 수정");
            addTable_Delete_Btn.setText("삭제");
            new getPost().execute();
            addTable_tableNAme_Edt.setText(tableName);
        }

        addTable_Delete_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("추적","여기1");
                if (!NetworkConn.isNetworkConnected(AddTableActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(AddTableActivity.this);
                    networkConnDialog.show();
                    return;
                }
                if(num==0){
                    finish();
                    overridePendingTransition(R.anim.hold,R.anim.hold);
                }else{
                    new deletePost().execute();
                }
            }
        });

        addTable_Summit_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(AddTableActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(AddTableActivity.this);
                    networkConnDialog.show();
                    return;
                }
                if(addTable_tableNAme_Edt.getText().toString().length()>0&&addTable_tableNAme_Edt.getText().toString().length()<9) {
                    new putPost().execute();
                }else{
                    final AlertDialog.Builder builder=new AlertDialog.Builder(AddTableActivity.this);
                    builder.setCustomTitle(null);
                    builder.setMessage("방제목을 1~10자로 작성해 주세요");
                    builder.setNegativeButton("확인", null);
                    builder.create().show();
                }
            }
        });

        ////////////////////////////////////For TimeTable

        timeTable_Days_Layout = (LinearLayout) findViewById(R.id.timeTable_Days_Layout);
        timeTable_Times_Layout = (LinearLayout) findViewById(R.id.timeTable_Times_Layout);
        layout = (LinearLayout) findViewById(R.id.timeTable_Container_Layout);
        timeGridView = new TimeTableGridLayout(this);
        frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(TimeTableInfo.dpToPx(this, TimeTableInfo.SIZEX * TimeTableInfo.TEXTWIDTH), TimeTableInfo.dpToPx(this, TimeTableInfo.SIZEY * TimeTableInfo.TEXTHEIGH));
        frameLayout.setLayoutParams(layoutParams);
        frameLayout.addView(timeGridView);
        layout.addView(frameLayout);
        imgViews = new TableElementOriginImg[TimeTableInfo.SIZEX * TimeTableInfo.SIZEY];
        TimeTableInfo.context = this;

        getDaysTxt();
        getTimesTxt();

        for (int i = 0; i < TimeTableInfo.SIZEX; i++) {
            for (int j = 0; j < TimeTableInfo.SIZEY; j++) {
                TableElementOriginImg imgView = new TableElementOriginImg(this, i, j);
                imgViews[i * TimeTableInfo.SIZEY + j] = imgView;
                timeGridView.addView(imgView);
            }
        }
        timeGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                int i = x / TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.TEXTWIDTH);
                int j = y / TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.TEXTHEIGH);

                if (isZoomed) {
                    if (lastY == -1) {
                        lastY = y;
                    }
                    switch (action) {
                        case MotionEvent.ACTION_DOWN://눌렀을때
                            if (lastTouch > System.currentTimeMillis() - 500) {//더블클릭 유무판단
                                lastTouch = 0;
                                isZoomOut = true;
                                //행동함
                            } else {
                                lastTouch = System.currentTimeMillis();
                            }
                            touchI = i;
                            touchJ = j;
                            lastJ = j;
                            touchX = x;
                            setTouchedPart(touchI, touchJ);//누른 부분이 누구의 파트인지 touched part에 기록.
                            break;
                        case MotionEvent.ACTION_UP: //손 뗄때

                            if (isZoomOut) { //줌아웃으로 만들어야하면
                                setZoomOut();   //줌아웃 해주고
                                //setElementTextNull();   //텍스트뷰의 텍스트들을 없앤다.
                            } else {
                                if (isOverClicked) {    //길게 눌렀을 경우
                                    setIsElement();         //해당좌표가 element인지 일반 텍스트뷰인지 확인한다
                                    if (isElement) {    //element일 경우 엘레멘트 가져오고 수정 대화창 시작
                                        int first = imgViews[touchI * TimeTableInfo.SIZEY + touchJ].pointer;
                                        TableElementTxt tableElementTxt = imgViews[first].getTableElementTxt();
                                        startElementDialog(tableElementTxt, first);
                                        isOverClicked = false;
                                    } else {    //element아닌경우 element 생성하는 루트
                                        if (j < 0) j = 0;   //j 넘어가면 조정하고
                                        if (j > TimeTableInfo.SIZEY - 1)
                                            j = TimeTableInfo.SIZEY - 1;
                                        int start;
                                        int last;
                                        if (touchJ < j) {// start 와 last 정해준다.
                                            start = touchI * TimeTableInfo.SIZEY + touchJ;
                                            int part = imgViews[start].part;
                                            for (last = start; last <= touchI * TimeTableInfo.SIZEY + j; last++) {
                                                if (imgViews[last].part != part) {
                                                    break;
                                                }
                                            }
                                            last--;
                                        } else {
                                            last = touchI * TimeTableInfo.SIZEY + touchJ;
                                            int part = imgViews[last].part;
                                            for (start = last; start >= touchI * TimeTableInfo.SIZEY + j; start--) {
                                                if (imgViews[start].part != part) {
                                                    break;
                                                }
                                            }
                                            start++;
                                        }

                                        TableElementTxt tableElementTxt = new TableElementTxt(getApplicationContext(), touchI, start % TimeTableInfo.SIZEY, last - start + 1);//element 새로만들고
                                        startElementDialog(tableElementTxt, start, last);//dialog element용으로 시작;
                                        ///////테스트용////////선택하고 생성안하고 취소했을 때 되돌리는 코드
                                        for (int k = start; k <= last; k++) {
                                            if ((k / 4) % 2 == 1) {
                                                imgViews[k].setBackgroundColor(TableElementOriginImg.originElementColor1);
                                            } else {
                                                imgViews[k].setBackgroundColor(TableElementOriginImg.originElementColor2);
                                            }
                                        }
                                        ////////테스트용 //////////
                                        isOverClicked = false;
                                    }
                                } else if (Math.abs(touchX - x) >= TimeTableInfo.dpToPx(getApplicationContext(), 50)) {//좌우 움직임
                                    if (touchI > i) {
                                        zoomIn(currentI + 1, touchJ);
                                    } else if (touchI < i) {
                                        zoomIn(currentI - 1, touchJ);
                                    }
                                    lastTouch = 0;
                                }
                            }
                            lastY = -1;
                            break;
                        case MotionEvent.ACTION_MOVE:   //움직일 때
                            if (!isOverClicked) {       //오버클릭이 아니면
                                if (lastTouch != 0 && System.currentTimeMillis() - lastTouch > 500) {   //오버클릭확인후
                                    imgViews[touchI * TimeTableInfo.SIZEY + touchJ].setBackgroundColor(TimeTableInfo.BackGround3);
                                    isOverClicked = true;   //오버클릭으로 전환
                                }
                            }
                            if (isZoomOut == false && isOverClicked) {//줌인상태에서 오래 눌렸을 때
                                if (lastJ != j && 0 <= j && j < TimeTableInfo.SIZEY) {// 같은 칸이 아니면 색을 바꾼다.
                                    setColorTextViews(j);
                                }
                                if (y - screenTopY <= 40) {     //드레그중 위 아래로 이동할 때
                                    zoomInJ(-5);
                                } else if (screenBottomY - y <= 40) {
                                    zoomInJ(5);
                                }
                            } else if(isZoomOut==false&&isOverClicked==false) {//줌인이고 오버클릭 아닐 때// 고쳐야 할듯
                                //System.out.println("lastY : "+lastY+" y : "+y);
                                if (lastY > y + 10) {
                                    zoomInJ(lastY - y);
                                    lastTouch = 0;
                                    lastY = -1;
                                } else if (lastY + 10 < y) {
                                    zoomInJ(lastY - y);
                                    lastTouch = 0;
                                    lastY = -1;
                                } else {
                                    lastY = y;
                                }
                            }
                            break;
                    }
                } else {//줌아웃 상태에서
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            if (lastTouch > System.currentTimeMillis() - 500) {//더블클릭이면 줌인
                                lastTouch = 0;
                                isZoomIn = true;
                                //행동함
                            } else {
                                lastTouch = System.currentTimeMillis();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (isZoomIn) {
                                isZoomed = true;
                                isZoomIn = false;
                                zoomIn(i, j);
                                //setElementTextName();
                                //System.out.println("     X : "+x +"      Y : "+ y);
                            }
                    }
                }
                return true;
            }
        });


        ////////////////////////////////////////
    }

    private class getPost extends AsyncTask<String, String, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(AddTableActivity.this);
        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                int countLine = 0;
                tableName = new String();
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/getTable.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("num", String.valueOf(num));
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
            if (tableName.startsWith("fail")) {
                Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT);
            } else {
                addTable_tableNAme_Edt.setText(tableName);
                try {
                    jsonArray = new JSONArray(result);
                    for(int i = 0 ; i<jsonArray.length();i++){
                        JSONObject js = jsonArray.getJSONObject(i);
                        int row = js.getInt("row");
                        int col = js.getInt("column");
                        int size = js.getInt("size");
                        int color = Integer.parseInt(js.getString("color"));
                        String name = js.getString("name");
                        TableElementTxt tableElementTxt=new TableElementTxt(getApplicationContext(),col,row,size);
                        tableElementTxt.color=color;
                        tableElementTxt.name=name;
                        tableElementTxt.setText(name);
                        tableElementTxt.setBackgroundColor(ElementDialog.colorCode[color]);

                        removeViewFromLayout(col*TimeTableInfo.SIZEY+row,col*TimeTableInfo.SIZEY+row+size-1);
                        timeGridView.addView(tableElementTxt);

                        setOccupied(col*TimeTableInfo.SIZEY+row,col*TimeTableInfo.SIZEY+row+size-1,tableElementTxt);
                        int typeIs =setTypeIs1(col*TimeTableInfo.SIZEY+row,col*TimeTableInfo.SIZEY+row+size-1);
                        partNumAdd(col*TimeTableInfo.SIZEY+row,col*TimeTableInfo.SIZEY+row+size-1,typeIs);
                        System.out.println("row col size : "+ row +" "+col+ " " + size);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            asyncProgressDialog.dismiss();
        }
    }

    private class putPost extends AsyncTask<String, String, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(AddTableActivity.this);
        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            tableName = addTable_tableNAme_Edt.getText().toString();
            putTrans = new JSONObject();
            putJA = new JSONArray();
            putJO = new JSONObject();
            try {
                putTrans.put("id", id);
                putTrans.put("num",num);
                putTrans.put("edit",edit);
                putTrans.put("tableName",tableName);
                for (int i = 0; i < imgViews.length; i++) {
                    if (imgViews[i].getTableElementTxt() != null) {
                        TableElementTxt tableElementTxt = imgViews[i].getTableElementTxt();
                        JSONObject table = new JSONObject();
                        table.put("column", tableElementTxt.i);
                        table.put("row", tableElementTxt.j);
                        table.put("size", tableElementTxt.size);
                        table.put("color", tableElementTxt.color);
                        table.put("name", tableElementTxt.name);
                        putJA.put(table);
                    }
                }
                putJO.put("TRANS", putTrans);
                putJO.put("DATA", putJA);
                jsValue = new String();
                jsValue = putJO.toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/putTable.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("json", jsValue);
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
            if (result.equals("success")) {
                finish();
                overridePendingTransition(R.anim.hold,R.anim.hold);
            } else {
                Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT);
            }
            asyncProgressDialog.dismiss();
        }
    }

    private class deletePost extends AsyncTask<String, String, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(AddTableActivity.this);
        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            jsValue = new String();
            JSONStringer jsonStringer = new JSONStringer();
            try {
                jsValue = jsonStringer.object()
                        .key("TRANS").object()
                        .key("id").value(id)
                        .key("num").value(num)
                        .key("max").value(max).endObject()
                        .endObject().toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/deleteTable.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("json", jsValue);
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
                    Log.e(LOG,"deleteTable : "+result);
                    if (line == null) break;
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
            if(result.contains("delete")) {
                Toast.makeText(getApplicationContext(), "시간표가 사용중 입니다.", Toast.LENGTH_SHORT).show();
            } else if(result.contains("onlyone")) {
                Toast.makeText(getApplicationContext(), "시간표는 1개 이상 있어야 합니다.", Toast.LENGTH_SHORT).show();
            }else if (!result.contains("fail")) {
                finish();
                overridePendingTransition(R.anim.hold,R.anim.hold);
            } else{
                Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
            }
            asyncProgressDialog.dismiss();
        }
    }
/////////////////////////////////////////////////For TimeTable

    private void zoomIn(int i, int j) {

        int pivotX;
        int pivotY;
        int control=0;
        switch (i) {
            case 0:
                i = 1;
                break;
            case 3:
                control=1;
                break;
            case 4:
                control=2;
                break;
            case 5:
                control=3;
            case 6:
                control=3;
                i = 5;
                break;
        }
        j /= 4;
        switch (j) {
            case 0:
            case 1:
                j = 2;
                break;
            case 11:
            case 12:
            case 13:
                j = 10;
                break;
        }
        currentI = i;
        pivotX = TimeTableInfo.dpToPx(getApplicationContext(), (TimeTableInfo.WIDTH / 4) * (i - 1)+control);
        pivotY = TimeTableInfo.dpToPx(getApplicationContext(), (TimeTableInfo.HEIGHT / 8) * (j - 2));
        timeGridView.setPivotX(pivotX);
        timeGridView.setPivotY(pivotY);
        timeGridView.setScaleX(7f / 3f);
        timeGridView.setScaleY(7f / 3f);

        timeTable_Days_Layout.setPivotX(pivotX);
        timeTable_Days_Layout.setPivotY(TimeTableInfo.dpToPx(getApplicationContext(), 0));
        timeTable_Days_Layout.setScaleX(7f / 3f);
        timeTable_Days_Layout.setScaleY(7f / 3f);
        setDaysTxtZoomIn();

        timeTable_Times_Layout.setPivotX(TimeTableInfo.dpToPx(getApplicationContext(), 12));
        timeTable_Times_Layout.setPivotY(pivotY);
        timeTable_Times_Layout.setScaleX(7f / 3f);
        timeTable_Times_Layout.setScaleY(7f / 3f);
        setTimesZoomIn();

        currentPivotY = pivotY;


        screenTopY = TimeTableInfo.dpToPx(getApplicationContext(), (j - 2) * TimeTableInfo.TEXTHEIGH * 4);
        screenBottomY = TimeTableInfo.dpToPx(getApplicationContext(), (j + 3) * TimeTableInfo.TEXTHEIGH * 4);
    }

    private void zoomInJ(int y_Moved) {
        int pivotY;
        int i = currentI;
        switch (i) {
            case 0:
                i = 1;
                break;
            case 4:
                break;
            case 5:
            case 6:
                i = 5;
                break;
        }
        pivotY = currentPivotY + (TimeTableInfo.dpToPx(getApplicationContext(), y_Moved));
        if (pivotY < 0) {
            pivotY = 0;
        }
        if (pivotY > TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT)) {
            pivotY = TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT);
        }
        currentPivotY = pivotY;
        timeGridView.setPivotY(pivotY);
        timeGridView.setScaleY(7f / 3f);

        timeTable_Times_Layout.setPivotY(pivotY);
        timeTable_Times_Layout.setScaleY(7f / 3f);
        setTimesZoomIn();

        screenTopY += TimeTableInfo.dpToPx(getApplicationContext(), y_Moved * 2 / 3);
        screenBottomY += TimeTableInfo.dpToPx(getApplicationContext(), y_Moved * 2 / 3);
        if (screenTopY < 0) screenTopY = 0;
        if (screenTopY > TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT) * 4 / 7)
            screenTopY = TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT * 4 / 7);
        if (screenBottomY < TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT * 3 / 7))
            screenBottomY = TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT * 3 / 7);
        if (screenBottomY > TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT))
            screenBottomY = TimeTableInfo.dpToPx(getApplicationContext(), TimeTableInfo.HEIGHT);

        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removeViewFromLayout(int start, int last) {
        for (int i = start; i <= last; i++) {
            timeGridView.removeView(imgViews[i]);
        }
    }

    private void setOccupied(int start, int last, TableElementTxt tableElementTxt) {
        for (int i = start; i <= last; i++) {
            imgViews[i].isOccupied = true;
            imgViews[i].pointer = start;
        }
        imgViews[start].setTableElementTxt(tableElementTxt);
    }

    private void partNumAdd(int start, int last, int typeIs) {
        if (typeIs == 1) {
            for (int i = start; i <= last; i++) {
                imgViews[i].part++;
            }
            for (int i = last + 1; i < (touchI + 1) * (TimeTableInfo.SIZEY); i++) {
                imgViews[i].part += 2;
            }
        } else if (typeIs == 2) {
            for (int i = last + 1; i < (touchI + 1) * (TimeTableInfo.SIZEY); i++) {
                imgViews[i].part++;
            }
        } else if (typeIs == 3) {
            for (int i = start; i < (touchI + 1) * (TimeTableInfo.SIZEY); i++) {
                imgViews[i].part++;
            }
        } else {

        }
    }

    private void setColorTextViews(int j) {
        if (lastJ < j) {
            for (; lastJ < touchJ && lastJ < j; lastJ++) {
                if (touchedPart == getPart(touchI, lastJ)) {
                    if ((lastJ / 4) % 2 == 1) {
                        imgViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(TableElementOriginImg.originElementColor1);
                    } else {
                        imgViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(TableElementOriginImg.originElementColor2);
                    }
                } else {
                    break;
                }
            }
            for (; lastJ >= touchJ && lastJ <= j; lastJ++) {
                if (touchedPart == getPart(touchI, lastJ)) {
                    imgViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(TimeTableInfo.BackGround3);
                } else {
                    break;
                }
            }
            lastJ = j;
        } else {
            for (; lastJ > touchJ && lastJ > j; lastJ--) {
                if (touchedPart == getPart(touchI, lastJ)) {
                    if ((lastJ / 4) % 2 == 1) {
                        imgViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundResource(TableElementOriginTxt.originElementColor1);
                    } else {
                        imgViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundResource(TableElementOriginTxt.originElementColor2);
                    }
                } else {
                    break;
                }

            }
            for (; lastJ <= touchJ && lastJ >= j; lastJ--) {
                if (touchedPart == getPart(touchI, lastJ)) {
                    imgViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(TimeTableInfo.BackGround3);
                } else {
                    break;
                }
            }
            lastJ = j;
        }

    }

    private void setTouchedPart(int i, int j) {
        touchedPart = imgViews[i * TimeTableInfo.SIZEY + j].part;
    }

    private int getPart(int i, int j) {
        return imgViews[i * TimeTableInfo.SIZEY + j].part;
    }

    private int setTypeIs1(int start, int last) {
        if (start % TimeTableInfo.SIZEY != 0 && last % TimeTableInfo.SIZEY != TimeTableInfo.SIZEY - 1 && imgViews[start].part != imgViews[start - 1].part && imgViews[last].part != imgViews[last + 1].part) {
            return 4;
        } else if (last % TimeTableInfo.SIZEY == TimeTableInfo.SIZEY - 1 || imgViews[last].part != imgViews[last + 1].part) {
            return 3;
        } else if (start % TimeTableInfo.SIZEY == 0 || imgViews[start].part != imgViews[start - 1].part) {
            return 2;
        } else {
            return 1;
        }
    }

    private void getDaysTxt() {
        daysTxt = new TextView[TimeTableInfo.SIZEX];
        daysTxt[0] = (TextView) findViewById(R.id.timeTable_Sun_Txt);
        daysTxt[1] = (TextView) findViewById(R.id.timeTable_Mon_Txt);
        daysTxt[2] = (TextView) findViewById(R.id.timeTable_Tue_Txt);
        daysTxt[3] = (TextView) findViewById(R.id.timeTable_Wed_Txt);
        daysTxt[4] = (TextView) findViewById(R.id.timeTable_Thu_Txt);
        daysTxt[5] = (TextView) findViewById(R.id.timeTable_Fri_Txt);
        daysTxt[6] = (TextView) findViewById(R.id.timeTable_Sat_Txt);
        for (int i = 0; i < TimeTableInfo.SIZEX; i++) {
            daysTxt[i].setTextSize(TimeTableInfo.dpToPx(getApplicationContext(), 5));
        }
    }

    private void setDaysTxtZoomIn() {
        for (int i = 0; i < TimeTableInfo.SIZEX; i++) {
            daysTxt[i].setTextSize(TimeTableInfo.dpToPx(getApplicationContext(), 2));
        }
    }

    private void setDaysTxtZoomOut() {
        for (int i = 0; i < TimeTableInfo.SIZEX; i++) {
            daysTxt[i].setTextSize(TimeTableInfo.dpToPx(getApplicationContext(), 5));
        }
    }

    private void getTimesTxt() {
        timesTxt = new TextView[TimeTableInfo.SIZEY / 4];
        timesTxt[0] = (TextView) findViewById(R.id.timeTable_8_Txt);
        timesTxt[1] = (TextView) findViewById(R.id.timeTable_9_Txt);
        timesTxt[2] = (TextView) findViewById(R.id.timeTable_10_Txt);
        timesTxt[3] = (TextView) findViewById(R.id.timeTable_11_Txt);
        timesTxt[4] = (TextView) findViewById(R.id.timeTable_12_Txt);
        timesTxt[5] = (TextView) findViewById(R.id.timeTable_13_Txt);
        timesTxt[6] = (TextView) findViewById(R.id.timeTable_14_Txt);
        timesTxt[7] = (TextView) findViewById(R.id.timeTable_15_Txt);
        timesTxt[8] = (TextView) findViewById(R.id.timeTable_16_Txt);
        timesTxt[9] = (TextView) findViewById(R.id.timeTable_17_Txt);
        timesTxt[10] = (TextView) findViewById(R.id.timeTable_18_Txt);
        timesTxt[11] = (TextView) findViewById(R.id.timeTable_19_Txt);
        timesTxt[12] = (TextView) findViewById(R.id.timeTable_20_Txt);
        timesTxt[13] = (TextView) findViewById(R.id.timeTable_21_Txt);
    }

    private void setTimesZoomIn() {
        for (int i = 0; i < TimeTableInfo.SIZEY / 4; i++) {
            timesTxt[i].setTextSize(TimeTableInfo.dpToPx(getApplicationContext(), 2));
        }
    }

    private void setTimesTxtZoomOut() {
        for (int i = 0; i < TimeTableInfo.SIZEY / 4; i++) {
            timesTxt[i].setTextSize(TimeTableInfo.dpToPx(getApplicationContext(), 5));
        }
    }

    private void setZoomOut() {
        timeGridView.setScaleX(1);
        timeGridView.setScaleY(1);
        isZoomed = false;
        isZoomOut = false;
        setDaysTxtZoomOut();
        timeTable_Days_Layout.setScaleX(1);
        timeTable_Days_Layout.setScaleY(1);

        setTimesTxtZoomOut();
        timeTable_Times_Layout.setScaleX(1);
        timeTable_Times_Layout.setScaleY(1);
    }

    private void setIsElement() {
        if (imgViews[TimeTableInfo.SIZEY * touchI + touchJ].isOccupied) {
            isElement = true;
        } else {
            isElement = false;
        }
    }

    private void startElementDialog(final TableElementTxt elementTxt, final int first) {
        ElementDialog elementDialog = new ElementDialog(AddTableActivity.this, elementTxt,1);
        elementDialog.show();
        elementDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (elementTxt.isDelete) {
                    int typeIs = setTypeIs2(first, first + elementTxt.size - 1);
                    partNumSub(first, first + elementTxt.size - 1, typeIs);
                    removeElementFromLayout(elementTxt, first);
                }
            }
        });
    }

    private void startElementDialog(final TableElementTxt tableElementTxt, final int start,final int last) {
        ElementDialog elementDialog = new ElementDialog(AddTableActivity.this, tableElementTxt,2);
        elementDialog.show();
        elementDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (tableElementTxt.isDelete) {
                }else{
                    int typeIs = setTypeIs1(start, last);
                    System.out.println("start : "+ start+" last : "+ last +" typeIs : "+ typeIs);
                    partNumAdd(start, last, typeIs);
                    removeViewFromLayout(start, last);
                    timeGridView.addView(tableElementTxt);
                    setOccupied(start, last, tableElementTxt);
                }
            }
        });
    }

    private void removeElementFromLayout(TableElementTxt elementTxt, int first) {
        timeGridView.removeView(elementTxt);
        for (int k = 0; k < elementTxt.size; k++) {
            imgViews[k + first].isOccupied = false;
            timeGridView.addView(imgViews[k + first]);
            if (((first + k) / 4) % 2 == 1) {
                imgViews[first + k].setBackgroundColor(TimeTableInfo.BackGround1);
            } else {
                imgViews[first + k].setBackgroundColor(TimeTableInfo.BackGround2);
            }
        }
        imgViews[first].setTableElementTxt(null);
    }

    private int setTypeIs2(int first, int last) {
        if (first % TimeTableInfo.SIZEY == 0 || imgViews[first - 1].isOccupied) {
            return 2;
        } else {
            return 1;
        }
    }

    private void partNumSub(int first, int last, int typeIs) {
        if(typeIs==2){
            for(int i=last+1;i<(touchI+1)*TimeTableInfo.SIZEY;i++){
                imgViews[i].part--;
            }
        }else{
            for(int i=first;i<=last;i++){
                imgViews[i].part--;
            }
            for(int i=last+1;i<(touchI+1)*TimeTableInfo.SIZEY;i++){
                imgViews[i].part-=2;
            }
        }
    }

    private void setElementTextName(){
        for(int i=0;i<TimeTableInfo.SIZEY*TimeTableInfo.SIZEX;i++){
            if(imgViews[i].getTableElementTxt()!=null){
                imgViews[i].getTableElementTxt().setText(imgViews[i].getTableElementTxt().name);
                imgViews[i].getTableElementTxt().setTextSize(TimeTableInfo.dpToPx(getApplicationContext(),3));
            }
        }
    }

    private void setElementTextNull(){
        for(int i=0;i<TimeTableInfo.SIZEY*TimeTableInfo.SIZEX;i++){
            if(imgViews[i].getTableElementTxt()!=null){
                //textViews[i].getTableElementTxt().setText("");
                imgViews[i].getTableElementTxt().setTextSize(TimeTableInfo.dpToPx(getApplicationContext(),3));
            }
        }
    }
}
