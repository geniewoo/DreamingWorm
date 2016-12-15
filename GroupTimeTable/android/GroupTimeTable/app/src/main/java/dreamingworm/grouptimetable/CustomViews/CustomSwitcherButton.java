package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import dreamingworm.grouptimetable.R;

/**
 * Created by sungwoo on 2016-08-05.
 */
public class CustomSwitcherButton extends Button {
    public CustomSwitcherButton(Context context) {
        super(context);
        init();
    }

    public CustomSwitcherButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSwitcherButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setBackgroundColor(Color.GRAY);
        setTextColor(Color.WHITE);
        setTextSize(20);
    }
}
