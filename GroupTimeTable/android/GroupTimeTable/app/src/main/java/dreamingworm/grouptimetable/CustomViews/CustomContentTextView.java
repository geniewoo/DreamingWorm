package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by sungwoo on 2016-08-04.
 */
public class CustomContentTextView extends TextView {
    public CustomContentTextView(Context context) {
        super(context);
        init();
    }

    public CustomContentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomContentTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setBackgroundColor(Color.TRANSPARENT);
    }
}
