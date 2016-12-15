package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by sungwoo on 2016-08-04.
 */
public class CustomCheckBox1 extends CheckBox {
    public CustomCheckBox1(Context context) {
        super(context);
        init();
    }

    public CustomCheckBox1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCheckBox1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
        setBackgroundColor(Color.TRANSPARENT);
    }
}
