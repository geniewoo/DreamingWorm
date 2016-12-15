package dreamingworm.grouptimetable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
 * Created by sungwoo on 2016-07-15.
 */
public class GroupLayout extends LinearLayout {
    boolean isFirst=true;
    boolean isButton=false;
    boolean isProgressDialog=false;

    AsyncProgressDialog asyncProgressDialog;
    String LOG="GroupLayout";
    CountDownLatch latch;
    boolean isFabClickable=false;
    boolean lock;
    int currentTabImg;//for SendTabPost,Dialog
    int resultType;//for SendTabPost,Dialog
    int tabPosition;//for SendTabPost,OnLongClickItemListener
    int currentTabNum=0;
    ArrayList<GroupTab> groupTabs;
    ArrayList<GroupGroup> groupGroups;
    ArrayList<GroupTableBtn> selectedGroupTableBtns;
    ArrayList<ImageView> listTabImageViews=new ArrayList<>();
    ListView group_Tab_ListView;
    LinearLayout group_Table_Layout;
    Context contexT;
    String id;
    TabListAdapter tabListAdapter;
    FloatingActionButton group_AddGroup_Fab;
    IMGINFO imginfo=new IMGINFO();
    int tabNum;
    public GroupLayout(Context context) {
        super(context);
        contexT=context;
        init();
    }

    public GroupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        contexT=context;
        init();
    }

    public GroupLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contexT=context;
        init();
    }

    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
        currentTabNum=0;
        Log.e(LOG,"isFisrt : "+isFirst+" isButton : "+isButton+" isProgressDialog : "+isProgressDialog);
        if(getVisibility()==GONE){
            if(!isFirst){
                Log.e(LOG,"isFirst");
                isProgressDialog=false;
                return;
            }
            isFirst=false;
        }
        if(lock){
            Log.e(LOG,"isLock");
            return;
        }
        if(isButton){
            isButton=false;
            Log.e(LOG,"isButton");
            return;
        }
        if(isProgressDialog){
            isProgressDialog=false;
            Log.e(LOG,"isProgressDialog");
            return;
        }
        lock = true;
        if (!NetworkConn.isNetworkConnected(contexT)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
            networkConnDialog.show();
            return;
        }

        setTab();

        ///////////////////group_Table_ListView 만들기//////////////////////
        new GetAllGroupTableList().execute();
        ///////////////////group_Table_ListView 만들기//////////////////////
        group_Tab_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!NetworkConn.isNetworkConnected(contexT)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
                    networkConnDialog.show();
                    return;
                }
                isButton=true;
                if(position==0){//즐찾
                    isFabClickable=false;
                    currentTabNum=0;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            tabListAdapter.notifyDataSetChanged();
                        }
                    }).run();
                    showFavoriteGroupList();
                    //즐겨찾기 groupListView 조정하기
                }else if(position==groupTabs.size()-1&&tabNum<imginfo.MAX+1){//플러스
                    //+눌렸을 때
                    tabPosition=position;
                    startTabDialog();
                }else{
                    isFabClickable=true;//일반텝
                    currentTabNum=groupTabs.get(position).tabNum;
                    showClickedGroupList();
                    //new GetGroupTableList().execute();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            tabListAdapter.notifyDataSetChanged();
                        }
                    }).run();
                }
           }
        });

        group_Tab_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!NetworkConn.isNetworkConnected(contexT)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
                    networkConnDialog.show();
                    return true;
                }
                isButton=true;
                if(position!=0&&!(position==tabNum&&tabNum<=5)){
                    Toast.makeText(contexT,"LongClicked",Toast.LENGTH_LONG).show();
                    startTabDialog(position);
                }
                return true;
            }
        });
    }

    ///////////////////////////////////////////////////////////GROUP////////////////////////////////
    class GroupTableBtn extends Button{
        GroupGroup mGroupGroup=new GroupGroup();
        public GroupTableBtn(Context context, final GroupGroup groupGroup) {
            super(context);
            mGroupGroup=groupGroup;
            GroupGroupInfo groupGroupInfo=new GroupGroupInfo();
            setBackgroundResource(groupGroupInfo.GROUP.get("GROUP"+groupGroup.groupImg));
            setTypeface(Typeface.createFromAsset(getContext().getAssets(),"BMJUA_ttf.ttf"));
            setTextSize(TimeTableInfo.dpToPx(contexT,5));
            setTextColor(Color.YELLOW);
            if(mGroupGroup.isFav==true){
                setText("★\n"+groupGroup.groupName);
            }else{
                setText("☆\n"+groupGroup.groupName);
            }
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(TimeTableInfo.dpToPx(contexT,145),TimeTableInfo.dpToPx(contexT,145));
            layoutParams.setMargins(TimeTableInfo.dpToPx(contexT,5),TimeTableInfo.dpToPx(contexT,5),0,0);
            setLayoutParams(layoutParams);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    isButton=true;
                    Intent intent=new Intent(contexT,GroupTableActivity.class);
                    intent.putExtra("id",id);
                    intent.putExtra("groupSN",mGroupGroup.groupSN);
                    intent.putExtra("groupImg",mGroupGroup.groupImg-1);
                    intent.putExtra("groupName",mGroupGroup.groupName);
                    intent.putExtra("tableNum",mGroupGroup.myTableNum);
                    intent.putExtra("currentTabNum",mGroupGroup.tabNum-1);
                    ArrayList<Integer> tabImg = new ArrayList<Integer>();
                    for(int i = 1; i < groupTabs.size()-1;i++) {
                        tabImg.add(groupTabs.get(i).tabImgCode);
                    }
                    intent.putExtra("tabImg",tabImg);
                    contexT.startActivity(intent);
                    ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
                }
            });
            setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!NetworkConn.isNetworkConnected(contexT)) {
                        Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(contexT);
                        networkConnDialog.show();
                        return true;
                    }
                    new ChangeTableFav().execute((GroupTableBtn) v);
                    return true;
                }
            });
        }
    }

    class ChangeTableFav extends AsyncTask<GroupTableBtn,String,GroupTableBtn>{
        String gottenTab=new String();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected GroupTableBtn doInBackground(GroupTableBtn... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/changeFavState.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("groupSN", params[0].mGroupGroup.groupSN).appendQueryParameter("isFav",String.valueOf(params[0].mGroupGroup.isFav)).appendQueryParameter("id",id);

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
                    Log.e(LOG,"ChangeTableFav line : "+line);
                    gottenTab+=line;
                }
                http.disconnect();

            }catch (Exception e) {
                e.printStackTrace();
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(GroupTableBtn s) {
            super.onPostExecute(s);
            if(gottenTab.contains("fail")){
                Log.e(LOG,"ChangeTableFav Error");
                return;
            }else{
                Log.e(LOG,"ChangeTableFav Succeed");
            }
            if(s.mGroupGroup.isFav==true){
                s.mGroupGroup.isFav=false;
                s.setText("☆\n"+s.mGroupGroup.groupName);
            }else{
                s.mGroupGroup.isFav=true;
                s.setText("★\n"+s.mGroupGroup.groupName);
            }
        }
    }

    private void makeGroupView(){
        group_Table_Layout.removeAllViews();
        int i;
        LinearLayout linearLayout = new LinearLayout(contexT);
        for(i=0;i<selectedGroupTableBtns.size();i++) {
            if (i % 2 == 0) {
                linearLayout = new LinearLayout(contexT);
                LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.setOrientation(HORIZONTAL);
                linearLayout.setLayoutParams(layoutParams);
            }
            linearLayout.addView(selectedGroupTableBtns.get(i));
            if (i % 2 == 1) {
                group_Table_Layout.addView(linearLayout);
            }
        }
        if(i%2==1){
            group_Table_Layout.addView(linearLayout);
        }
        Log.e(LOG,"makegroupview fin");
    }
    class GetAllGroupTableList extends AsyncTask<String,String,Integer>{
        String gottenTable="";

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            try {
                latch.await(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        protected Integer doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/getAllGroupTableList.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", id).appendQueryParameter("tabNum",String.valueOf(currentTabNum));

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
                    gottenTable+=line;
                }
                http.disconnect();

            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            try{
                groupGroups=new ArrayList<>();
                if(gottenTable.equals("")){
                    Log.e(LOG,"gottenTable contains none");
                }else if(!gottenTable.contains("fail")){
                    Log.e(LOG,"gottenTable : "+gottenTable);
                    JSONArray jsonArray=new JSONArray(gottenTable);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        GroupGroup groupGroup=new GroupGroup();
                        groupGroup.groupSN=jsonObject.getString("groupSN");
                        groupGroup.groupImg=jsonObject.getInt("tableImg");
                        groupGroup.groupName=jsonObject.getString("groupName");
                        groupGroup.myTableNum=jsonObject.getInt("myTableNum");
                        groupGroup.tabNum=jsonObject.getInt("tabNum");
                        if(jsonObject.getInt("isFav")==0){
                            groupGroup.isFav=false;
                        }else {
                            groupGroup.isFav = true;
                        }
                        groupGroups.add(groupGroup);
                    }
                    Log.e(LOG,"showFavoriteGroupList");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            showFavoriteGroupList();
                        }
                    }).run();
                }else {
                    Log.e(LOG,"gottenTable : "+gottenTable);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            lock = false;
            isProgressDialog=true;
            asyncProgressDialog.dismiss();
        }
    }

    private void showClickedGroupList(){
        selectedGroupTableBtns=new ArrayList<>();
        for(int i=0;i<groupGroups.size();i++){
            if(groupGroups.get(i).tabNum==currentTabNum){
                GroupTableBtn groupTableBtn = new GroupTableBtn(contexT,groupGroups.get(i));
                selectedGroupTableBtns.add(groupTableBtn);
            }
        }
        makeGroupView();
    }

    private void showFavoriteGroupList(){
        selectedGroupTableBtns=new ArrayList<>();
        for(int i=0;i<groupGroups.size();i++){
            if(groupGroups.get(i).isFav==true){
                GroupTableBtn groupTableBtn = new GroupTableBtn(contexT,groupGroups.get(i));
                selectedGroupTableBtns.add(groupTableBtn);
            }
        }
        Log.e(LOG,"showFavoriteGroupList");
        makeGroupView();
    }

    class GroupGroup{
        int tabNum;
        boolean isFav;
        String groupName;
        int groupImg;
        String groupSN;
        int myTableNum;
    }

    ///////////////////////////////////////////////////////////GROUP////////////////////////////////

    ///////////////////////////////////////////////////////////TAB////////////////////////////////
    private void setTab(){

        new GetTabPost().execute();

    }

    public class TabListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return groupTabs.size();
        }

        @Override
        public Object getItem(int position) {
            return groupTabs.get(position).tabImg;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ImageView tempImageView=new ImageView(getContext());
            tempImageView.setImageResource(groupTabs.get(position).tabImg);
            ListView.LayoutParams layoutParams=new ListView.LayoutParams(TimeTableInfo.dpToPx(contexT,60),TimeTableInfo.dpToPx(contexT,60));
            tempImageView.setLayoutParams(layoutParams);
            listTabImageViews.add(tempImageView);
            if(currentTabNum==groupTabs.get(position).tabNum){
                tempImageView.setBackgroundColor(Color.CYAN);
            }
            return tempImageView;
        }
    }

    private void startTabDialog(int position){
        tabPosition=position;
        TabDialog tabDialog=new TabDialog(contexT,2);
        tabDialog.show();
    }

    private void startTabDialog(){
        Toast.makeText(contexT, "makeNewTab " , Toast.LENGTH_LONG).show();

        TabDialog tabDialog=new TabDialog(contexT,1);
        tabDialog.show();

    }

    class TabDialog extends Dialog {
        int clickedTabImg=1;
        String LOG="TabDialog";
        Button group_Tab_Btn1;
        Button group_Tab_Btn2;
        LinearLayout group_Tab_LinearLayout;
        IMGINFO imginfo=new IMGINFO();
        ArrayList<ImageView> TabImages=new ArrayList<>();

        @Override
        protected void onStop() {
            super.onStop();
        }

        @Override
        public void dismiss() {
            super.dismiss();
            switch (resultType){
                case 1:
                    new SendTabPost().execute();
                    break;
                case 2://삭제
                    new SendTabPost().execute();
                    break;
                case 3:
                    new SendTabPost().execute();
                    break;
                case 4:
                    break;
            }
        }

        public TabDialog(Context context, int type) {
            super(context);
            resultType=0;
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_group_tab);

            group_Tab_Btn1=(Button)findViewById(R.id.group_Tab_Btn1);
            group_Tab_Btn2=(Button)findViewById(R.id.group_Tab_Btn2);
            group_Tab_LinearLayout=(LinearLayout) findViewById(R.id.group_Tab_LinearLayout);

            if(type==1){
                group_Tab_Btn1.setText("생성");
                group_Tab_Btn2.setText("취소");
            }else if(type==2){
                group_Tab_Btn1.setText("수정");
                group_Tab_Btn2.setText("삭제");
            }

            if(type==1) {
                group_Tab_Btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resultType=1;
                        dismiss();
                    }
                });

                group_Tab_Btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resultType=4;
                        dismiss();
                    }
                });
            }else{
                group_Tab_Btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resultType=3;
                        dismiss();
                    }
                });

                group_Tab_Btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(groupTabs.size()==3){
                            Toast.makeText(contexT,"텝은 최소 하나는 있어야 합니다",Toast.LENGTH_LONG).show();
                            return;
                        }
                        for(int i=0;i<groupGroups.size();i++){
                            if(groupGroups.get(i).tabNum==groupTabs.get(tabPosition).tabNum){

                                Toast.makeText(contexT,"텝에 다른 그룹이 포함되어 있습니다",Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        resultType=2;
                        dismiss();
                    }
                });
            }


            LinearLayout linearLayout;
            for(int i=0;i<=(imginfo.IMGNUM-1)/5;i++){
                linearLayout=new LinearLayout(contexT);
                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.setOrientation(HORIZONTAL);
                linearLayout.setLayoutParams(layoutParams);

                int maxj;
                if(i<imginfo.IMGNUM/5){
                    maxj=4;
                }else{
                    maxj=(imginfo.IMGNUM-1)%5;
                }
                for(int j=0;j<=maxj;j++){
                    currentTabImg=i*5+j+1;
                    ImageView tempImageView=new ImageView(contexT);
                    LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(TimeTableInfo.dpToPx(contexT,60),TimeTableInfo.dpToPx(contexT,60));
                    layoutParams.setMargins(TimeTableInfo.dpToPx(contexT,5),TimeTableInfo.dpToPx(contexT,5),TimeTableInfo.dpToPx(contexT,5),TimeTableInfo.dpToPx(contexT,5));
                    tempImageView.setLayoutParams(layoutParams1);
                    int imgCode=imginfo.TAB.get("IMG"+(currentTabImg));
                    tempImageView.setImageResource(imgCode);
                    tempImageView.setId(currentTabImg);

                    tempImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ////////////////////////////////////바꿀곳///////////////////////////////
                            int clickedImgCode=imginfo.TAB.get("IMG"+clickedTabImg);
                            TabImages.get(clickedTabImg-1).setImageResource(clickedImgCode);

                            currentTabImg=v.getId();
                            clickedTabImg=currentTabImg;


                            int tempCurrentImgCode=imginfo.TAB.get("IMG"+currentTabImg);
                            Bitmap getCurrentBitmap=BitmapFactory.decodeResource(getResources(),tempCurrentImgCode);
                            Bitmap tempCurrentBitmap=Bitmap.createBitmap(getCurrentBitmap.getWidth(),getCurrentBitmap.getHeight(), Bitmap.Config.RGB_565);
                            Canvas currentCanvas=new Canvas(tempCurrentBitmap);
                            currentCanvas.drawBitmap(getCurrentBitmap,0,0,null);

                            TabImages.get(currentTabImg-1).setImageBitmap(tempCurrentBitmap);
                            ////////////////////////////////////바꿀곳///////////////////////////////
                        }
                    });
                    TabImages.add(tempImageView);

                    linearLayout.addView(tempImageView);


                }
                group_Tab_LinearLayout.addView(linearLayout);
            }
            currentTabImg=1;
            int tempCurrentImgCode=imginfo.TAB.get("IMG"+currentTabImg);
            Bitmap getCurrentBitmap=BitmapFactory.decodeResource(getResources(),tempCurrentImgCode);
            Bitmap tempCurrentBitmap=Bitmap.createBitmap(getCurrentBitmap.getWidth(),getCurrentBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas currentCanvas=new Canvas(tempCurrentBitmap);
            currentCanvas.drawBitmap(getCurrentBitmap,0,0,null);

            TabImages.get(0).setImageBitmap(tempCurrentBitmap);
        }
    }

    private class GetTabPost extends  AsyncTask<String,String,String>{

        String gottenTab="";

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            latch=new CountDownLatch(1);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                groupTabs=new ArrayList<>();
                addTabArrayCroppedBitmap(R.drawable.star);
                if(!gottenTab.equals("fail")) {
                    JSONArray jsonArray = new JSONArray(gottenTab);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int tabImgCode = jsonObject.getInt("tabImg");
                        int tabNum = jsonObject.getInt("tabNum");
                        addTabArrayCroppedBitmap(imginfo.TAB.get("IMG" + tabImgCode));
                        addTabArray(tabImgCode, tabNum);
                    }
                }
                tabNum=groupTabs.size();
                if(tabNum<imginfo.MAX+1){
                    addTabArrayCroppedBitmap(R.drawable.plus);
                }
                tabListAdapter=new TabListAdapter();
                listTabImageViews.clear();
                group_Tab_ListView.setAdapter(tabListAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            latch.countDown();
            new GetAllGroupTableList().execute();
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
                    gottenTab+=line;
                }
                http.disconnect();

            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class SendTabPost extends AsyncTask<String,String,String>{
        JSONObject tabJO;
        String jsValue;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                tabJO = new JSONObject();
                tabJO.put("type", resultType);

                switch (resultType){
                    case 1:
                        tabJO.put("tabImg", currentTabImg);
                        if(tabNum==1){
                            tabJO.put("tabPos",1);
                        }else{
                            tabJO.put("tabPos",groupTabs.get(tabNum-1).tabNum+1);
                        }
                        break;
                    case 2:
                        tabJO.put("tabPos",groupTabs.get(tabPosition).tabNum);
                        break;
                    case 3:
                        tabJO.put("tabImg", currentTabImg);
                        tabJO.put("tabPos", groupTabs.get(tabPosition).tabNum);
                        break;
                }
                jsValue=tabJO.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/postGroupTab.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("json", jsValue)
                        .appendQueryParameter("id", id);
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
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            isProgressDialog=true;
            asyncProgressDialog.dismiss();
        }
    }

    private void init(){
        setVisibility(GONE);
        LayoutInflater inflater=LayoutInflater.from(contexT);
        inflater.inflate(R.layout.layout_group,this,true);

        asyncProgressDialog=new AsyncProgressDialog(contexT);

        SharedPreferences preferences=contexT.getSharedPreferences("environ",0);
        id=preferences.getString("ID","");

        group_Tab_ListView=(ListView)findViewById(R.id.group_Tab_ListView);
        group_Table_Layout=(LinearLayout) findViewById(R.id.group_Table_Layout);
        group_AddGroup_Fab=(FloatingActionButton)findViewById(R.id.group_AddGroup_Fab);

        group_AddGroup_Fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFabClickable){
                    isButton=true;
                    Intent intent=new Intent(contexT,AddGroupActivity.class);
                    intent.putExtra("id",id);
                    intent.putExtra("currentTabNum",currentTabNum);
                    contexT.startActivity(intent);
                    ((Activity)contexT).overridePendingTransition(R.anim.hold,R.anim.hold);
                }else{
                    Toast.makeText(contexT,"즐겨찾기나 텝 추가 버튼에서는 그룹을 만들 수 없습니다.",Toast.LENGTH_LONG).show();
                }
            }
        });
        isFabClickable=false;

    }

    public void addTabArrayCroppedBitmap(int imgCode){
        GroupTab groupTab=new GroupTab();
        if(imgCode==R.drawable.plus){
            groupTab.tabNum=-1;
        }
        groupTab.tabImg=imgCode;
        groupTabs.add(groupTab);
    }

    private void addTabArray(int tabImgCode,int tabNum){
        groupTabs.get(groupTabs.size()-1).tabImgCode=tabImgCode;
        groupTabs.get(groupTabs.size()-1).tabNum=tabNum;
    }

    class GroupTab{
        int tabNum;
        int tabImgCode;
        int tabImg;
    }

    static public class IMGINFO{
        int IMGNUM;
        int MAX=5;
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
    ///////////////////////////////////////////////////////////TAB////////////////////////////////
}
