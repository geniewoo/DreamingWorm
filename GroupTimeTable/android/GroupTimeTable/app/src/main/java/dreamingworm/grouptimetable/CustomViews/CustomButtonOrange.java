package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import dreamingworm.grouptimetable.R;

/**
 * Created by SungWoo on 2016-08-04.
 */
public class CustomButtonOrange extends Button{
    public CustomButtonOrange(Context context) {
        super(context);
        init();
    }

    public CustomButtonOrange(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomButtonOrange(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setBackgroundResource(R.drawable.orangereck);
    }
}
