package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import dreamingworm.grouptimetable.R;

/**
 * Created by sungwoo on 2016-08-04.
 */
public class CustomEditText2 extends EditText{
    public CustomEditText2(Context context) {
        super(context);
        init();
    }

    public CustomEditText2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setBackgroundResource(R.drawable.blackstroke);
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"GodoB.ttf"));
    }
}
