package dreamingworm.grouptimetable;

import java.util.HashMap;

/**
 * Created by sungwoo on 2016-07-21.
 */
public class GroupGroupInfo {
    int IMGNUM;
    HashMap<String,Integer> GROUP;
    GroupGroupInfo(){
        GROUP=new HashMap<String,Integer>();
        GROUP.put("GROUP1",R.drawable.table1);
        GROUP.put("GROUP2",R.drawable.table2);
        GROUP.put("GROUP3",R.drawable.table3);
        GROUP.put("GROUP4",R.drawable.table4);
        GROUP.put("GROUP5",R.drawable.table5);
        GROUP.put("GROUP6",R.drawable.table6);
        GROUP.put("GROUP7",R.drawable.table7);
        GROUP.put("GROUP8",R.drawable.table8);
        GROUP.put("GROUP9",R.drawable.table9);
        GROUP.put("GROUP10",R.drawable.table10);
        GROUP.put("GROUP11",R.drawable.table11);
        GROUP.put("GROUP12",R.drawable.table12);
        GROUP.put("GROUP13",R.drawable.table13);
        GROUP.put("GROUP13",R.drawable.table14);

        IMGNUM=GROUP.size();
    }
}
