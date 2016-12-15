package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import dreamingworm.grouptimetable.R;
import dreamingworm.grouptimetable.TimeTableInfo;

/**
 * Created by sungwoo on 2016-08-04.
 */
public class CustomGreenStrokeListTextView extends TextView {
    public CustomGreenStrokeListTextView(Context context) {
        super(context);
        init();
    }

    public CustomGreenStrokeListTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomGreenStrokeListTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TimeTableInfo.dpToPx(getContext(),60));
        layoutParams.setMargins(TimeTableInfo.dpToPx(getContext(),4),TimeTableInfo.dpToPx(getContext(),4),TimeTableInfo.dpToPx(getContext(),4),0);
        setLayoutParams(layoutParams);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setTextSize(TimeTableInfo.dpToPx(getContext(),15));
        setBackgroundResource(R.drawable.greenstrokereck);
    }
}
