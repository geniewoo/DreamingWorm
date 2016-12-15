package dreamingworm.grouptimetable;

import android.content.Context;
import android.widget.GridLayout;

/**
 * Created by sungwoo on 2016-07-05.
 */
public class TimeTableGridLayout extends GridLayout {
    public TimeTableGridLayout(Context context) {
        super(context);
        LayoutParams layoutParams=new LayoutParams();
        layoutParams.width= TimeTableInfo.dpToPx(context, TimeTableInfo.WIDTH);
        layoutParams.height= TimeTableInfo.dpToPx(context, TimeTableInfo.HEIGHT);
        setLayoutParams(layoutParams);
        this.setColumnCount(TimeTableInfo.SIZEX);
        this.setRowCount(TimeTableInfo.SIZEY);
        this.setOrientation(VERTICAL);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println(event.getX());
        return true;
    }*/
}
