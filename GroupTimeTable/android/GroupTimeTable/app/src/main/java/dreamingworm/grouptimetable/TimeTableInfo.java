package dreamingworm.grouptimetable;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by sungwoo on 2016-07-05.
 */
public class TimeTableInfo {
    static int COLORNUM=8;
    static int SIZEX=7;
    static int SIZEY=56;
    static int TEXTHEIGH=8;
    static  int TEXTWIDTH=41;
    static int WIDTH=TEXTWIDTH*SIZEX;
    static int HEIGHT=TEXTHEIGH*SIZEY;
    static int BackGround1= Color.rgb(100, 100, 30);
    static int BackGround2= Color.rgb(150, 150, 30);
    static int BackGround3= Color.argb(255, 0, 0, 255);

    static Context context;
    static int part[]={1,1,1,1,1,1,1};
    static public float pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dp = (px / (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
    static public int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return Math.round(px);
    }
}
