package dreamingworm.grouptimetable.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by SungWoo on 2016-08-04.
 */
public class CustomGreenTitleView extends TextView {
    public CustomGreenTitleView(Context context) {
        super(context);
        init();
    }

    public CustomGreenTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomGreenTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(),"BMJUA_ttf.ttf"));
    }
}
