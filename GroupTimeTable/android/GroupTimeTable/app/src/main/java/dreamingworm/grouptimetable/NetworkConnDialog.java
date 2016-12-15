package dreamingworm.grouptimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Created by sungwoo on 2016-08-03.
 */
public class NetworkConnDialog {
    static public AlertDialog createNetworkConnDialog(final Context context){
        final AlertDialog.Builder builder=new AlertDialog.Builder(context);

        builder.setCustomTitle(null);
        builder.setMessage("인터넷 연결상태를 확인해 주세요");
        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent intent = new Intent(context, context.getClass());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                ((Activity)context).overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        return builder.create();
    }
}
