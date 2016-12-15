package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import dreamingworm.grouptimetable.R;

/**
 * Created by SungWoo on 2016-08-04.
 */
public class CustomButtonGreen extends Button{
    public CustomButtonGreen(Context context) {
        super(context);
        init();
    }

    public CustomButtonGreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomButtonGreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setBackgroundResource(R.drawable.greenreck);
    }
}
