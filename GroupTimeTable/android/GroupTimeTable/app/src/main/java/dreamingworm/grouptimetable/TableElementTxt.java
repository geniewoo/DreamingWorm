package dreamingworm.grouptimetable;

import android.content.Context;
import android.graphics.Color;
import android.widget.GridLayout;
import android.widget.TextView;

/**
 * Created by sungwoo on 2016-07-05.
 */
public class TableElementTxt extends TextView {
    int i,j,size;
    boolean isDelete;
    int color;
    String name;
    public TableElementTxt(Context context, int col, int row, int size) {
        super(context);
        isDelete=true;
        this.size=size;
        i = col;
        j = row;
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = TimeTableInfo.dpToPx(context, TimeTableInfo.TEXTWIDTH);
        layoutParams.height = TimeTableInfo.dpToPx(context, TimeTableInfo.TEXTHEIGH*size);
        layoutParams.columnSpec = GridLayout. spec(i);
        if(size==1){
            layoutParams.rowSpec = GridLayout.spec(j,0);
        }else {
            layoutParams.rowSpec = GridLayout.spec(j, size);
        }
        setLayoutParams(layoutParams);
        color=0;
        setTextSize(TimeTableInfo.dpToPx(context,3));
        setTextColor(Color.WHITE);
    }
}