package dreamingworm.grouptimetable;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by sungwoo on 2016-07-11.
 */
public class ElementDialog extends Dialog {

    static int tableColor[]={R.drawable.tablecolor000153204, R.drawable.tablecolor102204153, R.drawable.tablecolor153153153, R.drawable.tablecolor204204255, R.drawable.tablecolor204255051, R.drawable.tablecolor255102153, R.drawable.tablecolor255204051, R.drawable.tablecolor255255153};
    static int tableCheck[]={R.drawable.tablecheck000153204, R.drawable.tablecheck102204153, R.drawable.tablecheck153153153, R.drawable.tablecheck204204255, R.drawable.tablecheck204255051, R.drawable.tablecheck255102153, R.drawable.tablecheck255204051, R.drawable.tablecheck255255153};
    static int colorCode[]={Color.rgb(000,153,204), Color.rgb(102,204,153), Color.rgb(153,153,153), Color.rgb(204,204,255), Color.rgb(204,255,051), Color.rgb(255,102,153), Color.rgb(255,204,051), Color.rgb(255,255,153)};
    int clickedColor;

    String LOG = "ElementDialog";
    TableElementTxt element;
    TextView colorText[];
    EditText memoEdt;
    Button deleteBtn;
    Button button;

    static public int[] makeTableColors() {
        int tableColor[] = new int[8];
        tableColor[0] = R.drawable.tablecolor000153204;
        tableColor[1] = R.drawable.tablecolor102204153;
        tableColor[2] = R.drawable.tablecolor153153153;
        tableColor[3] = R.drawable.tablecolor204204255;
        tableColor[4] = R.drawable.tablecolor204255051;
        tableColor[5] = R.drawable.tablecolor255102153;
        tableColor[6] = R.drawable.tablecolor255204051;
        tableColor[7] = R.drawable.tablecolor255255153;
        return tableColor;
    }

    static public int[] makeCheckColors() {
        int checkColor[] = new int[8];
        checkColor[0] = R.drawable.tablecheck000153204;
        checkColor[1] = R.drawable.tablecheck102204153;
        checkColor[2] = R.drawable.tablecheck153153153;
        checkColor[3] = R.drawable.tablecheck204204255;
        checkColor[4] = R.drawable.tablecheck204255051;
        checkColor[5] = R.drawable.tablecheck255102153;
        checkColor[6] = R.drawable.tablecheck255204051;
        checkColor[7] = R.drawable.tablecheck255255153;
        return checkColor;
    }

    static public int[] makeColorCode(){
        int colorCode[]=new int[8];
        return colorCode;
    }
    @Override
    protected void onStop() {
        super.onStop();
        dismiss();
    }

    public ElementDialog(Context context, final TableElementTxt element, final int type) {
        super(context);
        Log.e(LOG, "first");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_element);
        Log.e(LOG, "getWindow");
        this.element = element;
        colorText = new TextView[TimeTableInfo.COLORNUM];
        colorText[0] = (TextView) findViewById(R.id.element_0_Txt);
        colorText[1] = (TextView) findViewById(R.id.element_1_Txt);
        colorText[2] = (TextView) findViewById(R.id.element_2_Txt);
        colorText[3] = (TextView) findViewById(R.id.element_3_Txt);
        colorText[4] = (TextView) findViewById(R.id.element_4_Txt);
        colorText[5] = (TextView) findViewById(R.id.element_5_Txt);
        colorText[6] = (TextView) findViewById(R.id.element_6_Txt);
        colorText[7] = (TextView) findViewById(R.id.element_7_Txt);
        memoEdt = (EditText) findViewById(R.id.element_Memo_Edt);
        deleteBtn = (Button) findViewById(R.id.element_Delete_Btn);
        button = (Button) findViewById(R.id.element_Button_Btn);

        if (type == 1) {
            button.setText("수정");
            deleteBtn.setText("삭제");
            memoEdt.setText(element.name);
        } else if (type == 2) {
            button.setText("생성");
            deleteBtn.setText("취소");
        }

        clickedColor = element.color;
        Log.e(LOG, "setColor");


        for (int i = 0; i < TimeTableInfo.COLORNUM; i++) {
            colorText[i].setBackgroundResource(tableColor[i]);
        }

        colorText[clickedColor].setBackgroundResource(tableCheck[clickedColor]);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                element.setText(memoEdt.getText().toString());
                element.name = memoEdt.getText().toString();
                element.setBackgroundColor(colorCode[clickedColor]);
                element.color = clickedColor;
                element.isDelete = false;
                ElementDialog.this.dismiss();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1) {
                    element.isDelete = true;
                }
                ElementDialog.this.dismiss();
            }
        });

        colorText[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=0;
                v.setBackgroundResource(tableCheck[0]);
            }
        });

        colorText[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=1;
                v.setBackgroundResource(tableCheck[1]);
            }
        });

        colorText[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=2;
                v.setBackgroundResource(tableCheck[2]);
            }
        });

        colorText[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=3;
                v.setBackgroundResource(tableCheck[3]);
            }
        });

        colorText[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=4;
                v.setBackgroundResource(tableCheck[4]);
            }
        });

        colorText[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=5;
                v.setBackgroundResource(tableCheck[5]);
            }
        });

        colorText[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=6;
                v.setBackgroundResource(tableCheck[6]);
            }
        });

        colorText[7].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorText[clickedColor].setBackgroundResource(tableColor[clickedColor]);
                clickedColor=7;
                v.setBackgroundResource(tableCheck[7]);
            }
        });
    }
}
