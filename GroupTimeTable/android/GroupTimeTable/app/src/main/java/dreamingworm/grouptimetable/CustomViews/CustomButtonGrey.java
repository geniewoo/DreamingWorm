package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import dreamingworm.grouptimetable.R;

/**
 * Created by SungWoo on 2016-08-04.
 */
public class CustomButtonGrey extends Button{
    public CustomButtonGrey(Context context) {
        super(context);
        init();
    }

    public CustomButtonGrey(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomButtonGrey(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setBackgroundResource(R.drawable.greyreck);
        setTextColor(Color.WHITE);
    }
}
