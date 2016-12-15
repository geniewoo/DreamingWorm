package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by SungWoo on 2016-08-04.
 */
public class CustomButtonTransparent extends Button{
    public CustomButtonTransparent(Context context) {
        super(context);
        init();
    }

    public CustomButtonTransparent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomButtonTransparent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setBackgroundColor(Color.TRANSPARENT);
    }
}
