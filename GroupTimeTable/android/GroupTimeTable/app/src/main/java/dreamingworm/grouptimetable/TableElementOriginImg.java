package dreamingworm.grouptimetable;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by sungwoo on 2016-07-05.
 */
public class TableElementOriginImg extends ImageView {
    static int originElementColor1=Color.rgb(186,220,216);
    static int originElementColor2=Color.rgb(250,250,250);
    int pointer;
    boolean isOccupied;
    int part=0;
    private TableElementTxt tableElementTxt;
    public TableElementOriginImg(Context context, int col, int row){
        super(context);
        init(col,row);
        if((row/4)%2==1){
            setBackgroundColor(originElementColor1);
        }else{
            setBackgroundColor(originElementColor2);
        }
    }

    public TableElementOriginImg(Context context, int col, int row, boolean isGroup){
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
        setScaleType(ImageView.ScaleType.FIT_XY);
        setImageResource(R.drawable.listviewstrokemagenta);
    }
}
