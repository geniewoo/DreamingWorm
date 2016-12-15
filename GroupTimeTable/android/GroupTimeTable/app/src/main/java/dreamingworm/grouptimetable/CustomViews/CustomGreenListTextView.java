package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import dreamingworm.grouptimetable.R;
import dreamingworm.grouptimetable.TimeTableInfo;

/**
 * Created by sungwoo on 2016-08-04.
 */
public class CustomGreenListTextView extends TextView {
    public CustomGreenListTextView(Context context) {
        super(context);
        init();
    }

    public CustomGreenListTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomGreenListTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TimeTableInfo.dpToPx(getContext(),60));
        layoutParams.setMargins(TimeTableInfo.dpToPx(getContext(),4),TimeTableInfo.dpToPx(getContext(),4),TimeTableInfo.dpToPx(getContext(),4),0);
        setLayoutParams(layoutParams);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"BMJUA_ttf.ttf"));
        setTextSize(TimeTableInfo.dpToPx(getContext(),15));
        setBackgroundResource(R.drawable.greenreck);
    }
}
