package dreamingworm.grouptimetable;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by sungwoo on 2016-08-16.
 */
public class AsyncProgressDialog extends ProgressDialog {
    public AsyncProgressDialog(Context context) {
        super(context);
        setMessage("Loading...");
        setProgressStyle(STYLE_SPINNER);
    }
}
