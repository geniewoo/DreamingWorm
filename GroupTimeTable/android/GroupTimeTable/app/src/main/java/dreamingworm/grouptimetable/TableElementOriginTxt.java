package dreamingworm.grouptimetable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.GridLayout;
import android.widget.TextView;

/**
 * Created by sungwoo on 2016-07-05.
 */
public class TableElementOriginTxt extends TextView {
    static int originElementColor1=R.drawable.tableelement1;
    static int originElementColor2=R.drawable.tableelement2;
    boolean isOccupied;
    int part=0;
    private TableElementTxt tableElementTxt;
    public TableElementOriginTxt(Context context, int col, int row){
        super(context);
        init(col,row);
        if((row/4)%2==1){
            setBackgroundResource(originElementColor1);
        }else{
            setBackgroundResource(originElementColor2);
        }
    }

    public TableElementOriginTxt(Context context, int col, int row, boolean isGroup){
        super(context);
        init(col,row);
    }

    public void setTableElementTxt(TableElementTxt tableElementTxt){
        this.tableElementTxt=tableElementTxt;
    }

    public TableElementTxt getTableElementTxt(){
        return  tableElementTxt;
    }

    private void init(int col,int row){
        GridLayout.LayoutParams layoutParams=new GridLayout.LayoutParams();
        layoutParams.width= TimeTableInfo.dpToPx(getContext(), TimeTableInfo.TEXTWIDTH);
        layoutParams.height= TimeTableInfo.dpToPx(getContext(), TimeTableInfo.TEXTHEIGH);
        layoutParams.columnSpec= GridLayout.spec(col);
        layoutParams.rowSpec= GridLayout.spec(row);
        setLayoutParams(layoutParams);
        isOccupied=false;
    }
}
