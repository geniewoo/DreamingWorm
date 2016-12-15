package dreamingworm.grouptimetable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.concurrent.CountDownLatch;

/**
 * Created by sungwoo on 2016-07-29.
 */
public class GroupTableActivity extends ActionBarActivity {
    boolean isButton;
    boolean isProgressDialog=false;
    boolean lock=true;
    boolean isFirst=true;
    AsyncProgressDialog asyncProgressDialog;
    String LOG="GroupTableActivity";
    String id;
    String groupSN;
    String groupName;
    int tableNum;
    int groupImg;
    int currentTabNum = 0;
    int maxTabNum = 0;
    CountDownLatch latch;
    DrawerLayout drawerLayout;
    ListView listView;
    GroupTableListAdapter adapter;
    ActionBarDrawerToggle toggle;
    private JSONArray jsonArray;
    private String friend;
    private String result;
    private ArrayList<String> txt;
    private ArrayList<String> txt2;
    private ArrayList<String> fileName;
    private ArrayList<Bitmap> image_bitmap;
    private Bitmap raw_Image;
    private ArrayList<Boolean> default_Image;
    LinearLayout groupTable_Days_Layout;
    LinearLayout groupTable_Times_Layout;
    LinearLayout groupTable_Container_Layout;
    TimeTableGridLayout timeGridView;
    FrameLayout frameLayout;
    TextView groupTable_Name_Txt;
    ImageView imageViews[];
    TextView daysTxt[];
    TextView timesTxt[];
    boolean isZoomed = false;
    boolean isZoomOut = false;


    boolean isOverClicked = false;
    boolean isZoomIn = false;
    int currentI;
    int lastY = -1;
    int lastJ = -1;
    int touchX = -1;
    int touchI = -1;
    int touchJ = -1;
    private int screenTopY;
    private int screenBottomY;
    long lastTouch = 0;
    int currentPivotY;
    NicknamesInfo nicknamesInfo[] = new NicknamesInfo[TimeTableInfo.SIZEY * TimeTableInfo.SIZEX];
    int groupMemberNum = 0;
    ArrayList<Integer> tabImg = new ArrayList<Integer>();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(image_bitmap!=null){
            for(int i=0;i<image_bitmap.size();i++){
                image_bitmap.get(i).recycle();
            }
        }
        adapter.clear();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        asyncProgressDialog=new AsyncProgressDialog(GroupTableActivity.this);

        Log.e(LOG,"onCreate");
        setContentView(R.layout.activity_grouptable);

        getAndSetIntent();

        findViews();

        initNicknamesInfo();

        latch = new CountDownLatch(1);
        raw_Image = BitmapFactory.decodeResource(getResources(), R.drawable.worm_sumnail);
        txt = new ArrayList<>();
        txt2 = new ArrayList<>();
        fileName = new ArrayList<>();
        image_bitmap = new ArrayList<>();
        default_Image = new ArrayList<>();
        friend = new String();
        result = new String();

        drawerLayout = (DrawerLayout) findViewById(R.id.groupTable_Drawer_Layout);
        listView = (ListView) findViewById(R.id.groupTable_Menu_List);
        adapter = new GroupTableListAdapter();
        // Set the adapter for the list view
        listView.setAdapter(adapter);
        // Set the list's click listener
        listView.setOnItemClickListener(new DrawerItemClickListener());
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                syncState();
            }
        };
        drawerLayout.setDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toggle.syncState();
        adapter.addItem(null, "친구목록", "", '1');
        new SendPost().execute();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long viewid) {
                                                if (!NetworkConn.isNetworkConnected(GroupTableActivity.this)) {
                                                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(GroupTableActivity.this);
                                                    networkConnDialog.show();
                                                    return;
                                                }
                                                isButton=true;

                                                GroupTableListItem groupTableListItem = (GroupTableListItem) adapter.getItem(position);
                                                if (groupTableListItem.getName().equals("설정")) {
                                                    if (!NetworkConn.isNetworkConnected(GroupTableActivity.this)) {
                                                        Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(GroupTableActivity.this);
                                                        networkConnDialog.show();
                                                        return;
                                                    }
                                                    Intent setIntent = new Intent(getApplicationContext(), GroupSettingActivity.class);
                                                    setIntent.putExtra("id", id);
                                                    setIntent.putExtra("groupSN", groupSN);
                                                    setIntent.putExtra("currentTabNum", currentTabNum);
                                                    setIntent.putExtra("nickname", txt2);
                                                    setIntent.putExtra("groupName", groupName);
                                                    setIntent.putExtra("groupImg", groupImg);
                                                    setIntent.putExtra("tableNum", tableNum);
                                                    setIntent.putExtra("tabImg", tabImg);
                                                    startActivityForResult(setIntent,2);
                                                } else if (groupTableListItem.getName().equals("나가기")) {
                                                    new OutPost().execute();
                                                } else if (groupTableListItem.getName().equals("친구목록")) {
                                                    for (int i = 1; i < adapter.getCount() - 2; i++) {
                                                        adapter.changeflag(i);
                                                    }
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
        );

        timeGridView.setOnTouchListener(new View.OnTouchListener()

                                        {
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
                                                            break;
                                                        case MotionEvent.ACTION_UP://손을 땔때

                                                            if (isZoomOut) {//줌아웃으로 만들어야 하면
                                                                setZoomOut();//줌아웃 해줌
                                                            } else {
                                                                if (isOverClicked) {    //길게 눌렀을 경우
                                                                    if (j < 0) j = 0;
                                                                    if (j > TimeTableInfo.SIZEY - 1)
                                                                        j = TimeTableInfo.SIZEY - 1;
                                                                    int start;
                                                                    int last;
                                                                    if (touchJ < j) {
                                                                        start = touchI * TimeTableInfo.SIZEY + touchJ;
                                                                        last = touchI * TimeTableInfo.SIZEY + j;
                                                                    } else {
                                                                        last = touchI * TimeTableInfo.SIZEY + touchJ;
                                                                        start = touchI * TimeTableInfo.SIZEY + j;
                                                                    }
                                                                    showUnbusyDialog(start, last);
                                                                    updateTextViewsColor(start, last);
                                                                    isOverClicked = false;
                                                                } else if (Math.abs(touchX - x) >= TimeTableInfo.dpToPx(getApplicationContext(), 50)) {//좌우 움직일 때
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
                                                        case MotionEvent.ACTION_MOVE://움직일 때
                                                            if (!isOverClicked) {//오버클릭이 아니면
                                                                if (lastTouch != 0 && System.currentTimeMillis() - lastTouch > 500) {//오버클릭 확인후 오버클릭으로 변환.
                                                                    imageViews[touchI * TimeTableInfo.SIZEY + touchJ].setBackgroundColor(TimeTableInfo.BackGround3);
                                                                    isOverClicked = true;
                                                                }
                                                            }
                                                            if (isZoomOut == false && isOverClicked) {//줌인상태에서 오래눌렀을 때
                                                                if (lastJ != j && 0 <= j && j < TimeTableInfo.SIZEY) {//같은 칸이아니면 색을 바꾼다.
                                                                    setColorTextViews(j);
                                                                }
                                                                if (y - screenTopY <= 40) {     //드레그중 위 아래로 이동할 때
                                                                    zoomInJ(-5);
                                                                } else if (screenBottomY - y <= 40) {
                                                                    zoomInJ(5);
                                                                }
                                                            } else {
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
                                                } else {
                                                    switch (action) {
                                                        case MotionEvent.ACTION_DOWN:
                                                            if (lastTouch > System.currentTimeMillis() - 500) {
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
                                                                //System.out.println("     X : "+x +"      Y : "+ y);
                                                            }
                                                    }
                                                }
                                                return true;
                                            }
                                        }

        );
        Log.e(LOG,"onCreateFin");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        Log.e(LOG,"onResume isProgressDialog : "+isProgressDialog+ " isFirst : " +isFirst+ " isButton : "+isButton);

        if(!isFirst){
            new GetGroupInfo().execute();
        }else{
            isFirst=false;
        }

        if(isButton){
            isButton=false;
            return;
        }

        if(isProgressDialog){
            isProgressDialog=false;
            return;
        }
        Log.e(LOG,"onResumeFin");
    }

    private void getAndSetIntent() {
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        groupSN = intent.getStringExtra("groupSN");
        currentTabNum = intent.getIntExtra("currentTabNum", 0);
        maxTabNum = intent.getIntExtra("maxTabNum", 0);
        groupImg = intent.getIntExtra("groupImg", 0);
        tableNum = intent.getIntExtra("tableNum", 0);
        groupName = intent.getStringExtra("groupName");
        tabImg = intent.getIntegerArrayListExtra("tabImg");
    }

    private void showUnbusyDialog(int start, int last) {
        ArrayList<String> tempStringList;
        tempStringList = (ArrayList<String>) nicknamesInfo[start].nicknames.clone();
        for (int i = start + 1; i <= last; i++) {
            for (int j = tempStringList.size() - 1; 0 <= j; j--) {
                if (!nicknamesInfo[i].nicknames.contains(tempStringList.get(j))) {
                    tempStringList.remove(j);
                }
            }
        }
        UnbusyDialog unbusyDialog = new UnbusyDialog(GroupTableActivity.this, tempStringList);
        unbusyDialog.show();
    }

    class UnbusyDialog extends Dialog {
        public UnbusyDialog(Context context, ArrayList<String> unbusyList) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_unbusy);
            TextView unbusy_List_Txt = (TextView) findViewById(R.id.unbusy_List_Txt);
            Button unbusy_Confirm_Btn = (Button) findViewById(R.id.unbusy_Confirm_Btn);
            unbusy_Confirm_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            for (String temp : unbusyList) {
                unbusy_List_Txt.append(temp);
            }
        }
    }

    private void findViews() {
        groupTable_Days_Layout = (LinearLayout) findViewById(R.id.groupTable_Days_Layout);
        groupTable_Times_Layout = (LinearLayout) findViewById(R.id.groupTable_Times_Layout);
        getDaysTxt();
        getTimesTxt();

        groupTable_Container_Layout = (LinearLayout) findViewById(R.id.groupTable_Container_Layout);
        timeGridView = new TimeTableGridLayout(this);
        frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(TimeTableInfo.dpToPx(this, TimeTableInfo.SIZEX * TimeTableInfo.TEXTWIDTH), TimeTableInfo.dpToPx(this, TimeTableInfo.SIZEY * TimeTableInfo.TEXTHEIGH));
        frameLayout.setLayoutParams(layoutParams);
        frameLayout.addView(timeGridView);
        groupTable_Container_Layout.addView(frameLayout);

        groupTable_Name_Txt = (TextView) findViewById(R.id.groupTable_Name_Txt);

        imageViews = new TableElementOriginImg[TimeTableInfo.SIZEX * TimeTableInfo.SIZEY];
        TimeTableInfo.context = this;


        Log.e(LOG,"findviews");
        for (int i = 0; i < TimeTableInfo.SIZEX; i++) {
            for (int j = 0; j < TimeTableInfo.SIZEY; j++) {
                TableElementOriginImg imgView = new TableElementOriginImg(this, i, j, true);
                imageViews[i * TimeTableInfo.SIZEY + j] = imgView;
                timeGridView.addView(imgView);
            }
        }
        Log.e(LOG,"findviewsFin");
    }

    private void getDaysTxt() {
        daysTxt = new TextView[TimeTableInfo.SIZEX];
        daysTxt[0] = (TextView) findViewById(R.id.groupTable_Sun_Txt);
        daysTxt[1] = (TextView) findViewById(R.id.groupTable_Mon_Txt);
        daysTxt[2] = (TextView) findViewById(R.id.groupTable_Tue_Txt);
        daysTxt[3] = (TextView) findViewById(R.id.groupTable_Wed_Txt);
        daysTxt[4] = (TextView) findViewById(R.id.groupTable_Thu_Txt);
        daysTxt[5] = (TextView) findViewById(R.id.groupTable_Fri_Txt);
        daysTxt[6] = (TextView) findViewById(R.id.groupTable_Sat_Txt);
        for (int i = 0; i < TimeTableInfo.SIZEX; i++) {
            daysTxt[i].setTextSize(TimeTableInfo.dpToPx(getApplicationContext(), 5));
        }
    }

    private void getTimesTxt() {
        timesTxt = new TextView[TimeTableInfo.SIZEY / 4];
        timesTxt[0] = (TextView) findViewById(R.id.groupTable_8_Txt);
        timesTxt[1] = (TextView) findViewById(R.id.groupTable_9_Txt);
        timesTxt[2] = (TextView) findViewById(R.id.groupTable_10_Txt);
        timesTxt[3] = (TextView) findViewById(R.id.groupTable_11_Txt);
        timesTxt[4] = (TextView) findViewById(R.id.groupTable_12_Txt);
        timesTxt[5] = (TextView) findViewById(R.id.groupTable_13_Txt);
        timesTxt[6] = (TextView) findViewById(R.id.groupTable_14_Txt);
        timesTxt[7] = (TextView) findViewById(R.id.groupTable_15_Txt);
        timesTxt[8] = (TextView) findViewById(R.id.groupTable_16_Txt);
        timesTxt[9] = (TextView) findViewById(R.id.groupTable_17_Txt);
        timesTxt[10] = (TextView) findViewById(R.id.groupTable_18_Txt);
        timesTxt[11] = (TextView) findViewById(R.id.groupTable_19_Txt);
        timesTxt[12] = (TextView) findViewById(R.id.groupTable_20_Txt);
        timesTxt[13] = (TextView) findViewById(R.id.groupTable_21_Txt);
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

    private void setZoomOut() {
        timeGridView.setScaleX(1);
        timeGridView.setScaleY(1);
        groupTable_Times_Layout.setScaleX(1);
        groupTable_Times_Layout.setScaleY(1);
        groupTable_Days_Layout.setScaleX(1);
        groupTable_Days_Layout.setScaleY(1);
        setDaysTxtZoomOut();
        setTimesTxtZoomOut();

        isZoomed = false;
        isZoomOut = false;
    }

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

        groupTable_Days_Layout.setPivotX(pivotX);
        groupTable_Days_Layout.setPivotY(TimeTableInfo.dpToPx(getApplicationContext(), 0));
        groupTable_Days_Layout.setScaleX(7f / 3f);
        groupTable_Days_Layout.setScaleY(7f / 3f);
        setDaysTxtZoomIn();

        groupTable_Times_Layout.setPivotX(TimeTableInfo.dpToPx(getApplicationContext(), 12));
        groupTable_Times_Layout.setPivotY(pivotY);
        groupTable_Times_Layout.setScaleX(7f / 3f);
        groupTable_Times_Layout.setScaleY(7f / 3f);
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
        timeGridView.setScaleX(7f / 3f);
        timeGridView.setScaleY(7f / 3f);

        groupTable_Times_Layout.setPivotY(pivotY);
        groupTable_Times_Layout.setScaleY(7f / 3f);
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

    private void setColorTextViews(int j) {
        if (lastJ < j) {
            for (; lastJ < touchJ && lastJ < j; lastJ++) {
                imageViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(getTextViewColor(touchI * TimeTableInfo.SIZEY + lastJ));
            }
            for (; lastJ >= touchJ && lastJ <= j; lastJ++) {
                imageViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(TimeTableInfo.BackGround3);
            }
            lastJ = j;
        } else {
            for (; lastJ > touchJ && lastJ > j; lastJ--) {
                imageViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(getTextViewColor(touchI * TimeTableInfo.SIZEY + lastJ));
            }
            for (; lastJ <= touchJ && lastJ >= j; lastJ--) {
                imageViews[touchI * TimeTableInfo.SIZEY + lastJ].setBackgroundColor(TimeTableInfo.BackGround3);
            }
            lastJ = j;
        }

    }

    class GetGroupInfo extends AsyncTask<String, String, String> {
        String result = new String();

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            Log.e(LOG,"GetGroupInfoOnPre");
            if(lock){
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            initNicknamesInfo();
            groupMemberNum=0;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.e(LOG,"GetGroupInfo");
            super.onPostExecute(s);
            Log.e(LOG,"GetGroupInfo");
            if (result.startsWith("fail")) {
                Log.e(LOG,"GetGroupInfoFail");
                Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_LONG).show();
            } else {
                Log.e(LOG,"GetGroupInfoSuccess");
                try {
                    Log.e(LOG,"GetGroupInfoStart");
                    JSONArray jsonArray = new JSONArray(result);
                    Log.e(LOG,"GetGroupInfoPost");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        groupMemberNum++;
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String isbusy = jsonObject.getString("isbusy1") + jsonObject.getString("isbusy2");
                        String nickname = jsonObject.getString("nickname");
                        String name = jsonObject.getString("name");
                        updateTextViewsInfo(isbusy, nickname, name);
                    }
                    updateTextViewsColor(0, TimeTableInfo.SIZEX * TimeTableInfo.SIZEY - 1);
                    Log.e(LOG,"GetGroupInfoPostFin");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            lock=false;
            asyncProgressDialog.dismiss();
            isProgressDialog=true;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/getGroupTableInfo.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder().appendQueryParameter("id", id).appendQueryParameter("groupSN", groupSN);
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
                    Log.e(LOG,"get info line : " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(LOG,"Doin over");
            asyncProgressDialog.dismiss();
            return null;
        }
    }

    class NicknamesInfo {
        ArrayList<String> nicknames = new ArrayList<String>();
    }

    private void updateTextViewsInfo(String isbusy, String nickname, String name) {
        for (int i = 0; i < isbusy.length(); i++) {
            if (isbusy.charAt(i) == '0') {
                nicknamesInfo[i].nicknames.add(nickname + "(" + name + ")");
            }
        }
    }

    private void updateTextViewsColor(int start, int last) {
        for (int i = start; i <= last; i++) {
            imageViews[i].setBackgroundColor(getTextViewColor(i));
        }
    }

    private int getTextViewColor(int index) {
        int color = (nicknamesInfo[index].nicknames.size() + 1) * (255 / (groupMemberNum + 2));
        return Color.argb(120, 0, color/2, color);
    }

    private void initNicknamesInfo() {
        for (int i = 0; i < nicknamesInfo.length; i++) {
            nicknamesInfo[i] = new NicknamesInfo();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(listView);
        return super.onPrepareOptionsMenu(menu);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        listView.setItemChecked(position, true);
        drawerLayout.closeDrawer(listView);
    }

    private class SendPost extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            Log.e(LOG,"SendPostDoIn");
            try {
                int countLine = 0;
                URL url = new URL("http://pama.dothome.co.kr/getGroupFriend.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("groupSN", groupSN);
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
                    Log.d("twat Group", line);
                    if (countLine == 0) {
                        result = line;
                        countLine++;
                        continue;
                    }
                    friend = line;
                }
                http.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.e(LOG,"SendPostOnDoIn");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e(LOG,"SendPostOnPost");
            if (result.matches("[0-9]*")) {
                if (Integer.parseInt(result) != 0 || Integer.parseInt(result) != 1) {
                    try {
                        jsonArray = new JSONArray(friend);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    for (int i = 0; i < Integer.parseInt(result) - 1; i++) {
                        try {
                            JSONObject order = jsonArray.getJSONObject(i);
                            txt.add(order.getString("name"));
                            txt2.add(order.getString("nickname"));
                            fileName.add(order.getString("fid"));
                            if (order.getString("image").equals("0")) {
                                default_Image.add(true);
                            } else {
                                default_Image.add(false);
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                latch.countDown();
                new GetPost().execute();
            } else
                Toast.makeText(getApplicationContext(), "친구목록 불러오기 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetPost extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(LOG,"GetPostOnPre");
            try {
                latch.await();
                latch=new CountDownLatch(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                for (int i = 0; i < Integer.parseInt(result) - 1; i++) {
                    if (!default_Image.get(i)) {
                        if (!ImageFileStorage.checkFile(fileName.get(i))) {
                            URL url = new URL("http://pama.dothome.co.kr/" + fileName.get(i) + "_sumnail.jpg");
                            HttpURLConnection http = (HttpURLConnection) url.openConnection();
                            http.setDefaultUseCaches(false);
                            http.setDoInput(true);
                            http.connect();
                            InputStream is = http.getInputStream();
                            image_bitmap.add(BitmapFactory.decodeStream(is));
                            http.disconnect();
                            ImageFileStorage.saveBitmaptoJpeg(image_bitmap.get(i), fileName.get(i));
                        } else {
                            image_bitmap.add(ImageFileStorage.loadJpegtoBitmap(fileName.get(i)));
                        }
                    } else {
                        image_bitmap.add(raw_Image);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            for (int i = 0; i < Integer.parseInt(result) - 1; i++) {
                adapter.addItem(image_bitmap.get(i), txt.get(i), txt2.get(i), '0');
                adapter.notifyDataSetChanged();
            }
            adapter.addItem(null, "설정", "", '1');
            adapter.addItem(null, "나가기", "", '1');
            adapter.notifyDataSetChanged();
            latch.countDown();
            new GetGroupInfo().execute();
        }
    }

    private class OutPost extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            asyncProgressDialog.dismiss();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/outGroup.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("groupSN", groupSN);
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
                    Log.d("twat Group", line);
                    result = line;
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
            if (result.matches("success")) {
                Toast.makeText(getApplicationContext(), "방을 나갑니다", Toast.LENGTH_SHORT);
                finish();
            } else
                Toast.makeText(getApplicationContext(), "나가기 실패", Toast.LENGTH_SHORT);
            asyncProgressDialog.dismiss();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2&&resultCode==2){
            currentTabNum = data.getIntExtra("currentTabNum", 0);
            groupImg = data.getIntExtra("groupImg", 0);
            tableNum = data.getIntExtra("tableNum", 0);
            groupName = data.getStringExtra("groupName");
        }
    }

}