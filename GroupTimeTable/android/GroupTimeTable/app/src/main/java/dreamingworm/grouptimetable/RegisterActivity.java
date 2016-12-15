package dreamingworm.grouptimetable;


        import android.app.Dialog;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.PersistableBundle;
        import android.support.v7.app.AppCompatActivity;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.net.HttpURLConnection;
        import java.net.URL;

/**
 * Created by Youngs on 2016-06-16.
 */
public class RegisterActivity extends AppCompatActivity {

    private Button register_Register_Btn;
    private Button register_Cancel_Btn;
    private EditText register_ID_Edt;
    private EditText register_Password_Edt;
    private EditText register_PasswordChk_Edt;
    private EditText register_Name_Edt;
    private EditText register_Phone_Edt;
    private EditText register_Nickname_Edt;
    private TextView register_ID_Txt;
    private TextView register_Password_Txt;
    private TextView register_PasswordChk_Txt;
    private TextView register_Name_Txt;
    private TextView register_Phone_Txt;
    private TextView register_Nickname_Txt;
    private String re_Id_value;
    private String re_Password_value;
    private String re_PasswordChk_value;
    private String re_Name_value;
    private String re_Phone_value;
    private String re_Nickname_value;
    private String result;

    private boolean isIDPassed=false;
    private boolean isPasswordPassed=false;
    private boolean isPasswordChkPassed=false;
    private boolean isNamePassed=false;
    private boolean isPhonePassed=false;
    private boolean isNicknamePassed=false;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_register);

        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        register_Register_Btn = (Button)findViewById(R.id.register_Register_Btn);
        register_Cancel_Btn = (Button)findViewById(R.id.register_Cancel_Btn);
        register_ID_Edt = (EditText)findViewById(R.id.register_ID_Edt);
        register_ID_Txt=(TextView)findViewById(R.id.register_ID_Txt);
        register_Password_Edt = (EditText)findViewById(R.id.register_Password_Edt);
        register_Password_Txt=(TextView)findViewById(R.id.register_Password_Txt);
        register_PasswordChk_Edt = (EditText)findViewById(R.id.register_PasswordChk_Edt);
        register_PasswordChk_Txt=(TextView)findViewById(R.id.register_PasswordChk_Txt);
        register_Name_Edt = (EditText)findViewById(R.id.register_Name_Edt);
        register_Name_Txt=(TextView)findViewById(R.id.register_Name_Txt);
        register_Nickname_Edt = (EditText)findViewById(R.id.register_Nickname_Edt);
        register_Nickname_Txt= (TextView) findViewById(R.id.register_Nickname_Txt);
        register_Phone_Edt = (EditText)findViewById(R.id.register_Phone_Edt);
        register_Phone_Txt=(TextView) findViewById(R.id.register_Phone_Txt);


        register_Register_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(RegisterActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(RegisterActivity.this);
                    networkConnDialog.show();
                    return;
                }
                if(!isIDPassed){
                    register_ID_Edt.requestFocus();
                    return;
                }else if(!isPasswordPassed){
                    register_Password_Edt.requestFocus();
                    return;
                }else if(!isPasswordChkPassed){
                    register_PasswordChk_Edt.requestFocus();
                    return;
                }else if(!isPhonePassed){
                    register_Phone_Edt.requestFocus();
                    return;
                }else if(!isNamePassed){
                    register_Name_Edt.requestFocus();
                    return;
                }else if(!isNicknamePassed){
                    register_Nickname_Edt.requestFocus();
                    return;
                }
                re_Id_value = register_ID_Edt.getText().toString();
                re_Password_value = register_Password_Edt.getText().toString();
                re_PasswordChk_value = register_PasswordChk_Edt.getText().toString();
                re_Name_value =  register_Name_Edt.getText().toString();
                re_Phone_value = register_Phone_Edt.getText().toString();
                re_Nickname_value = register_Nickname_Edt.getText().toString();
                if( re_Password_value.equals(re_PasswordChk_value)) {
                    new SendPost().execute();
                }
                else{
                    Toast.makeText(getApplicationContext(),"암호가 일치하지 않음",Toast.LENGTH_SHORT).show();
                }
            }
        });
        register_Cancel_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        register_ID_Edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    register_ID_Txt.setVisibility(View.VISIBLE);
                    if(register_ID_Edt.length()<8){
                        register_ID_Txt.setText("ID는 8자 이상 이어야 합니다");
                        isIDPassed=false;
                    }else if(register_ID_Edt.length()>16){
                        register_ID_Txt.setText("ID는 16자 이하 이어야 합니다");
                        isIDPassed=false;
                    }else if(register_ID_Edt.getText().toString().matches("^[0-9]*")){
                        register_ID_Txt.setText("ID에 영문자를 추가해 주세요");
                        isIDPassed=false;
                    }else if (!register_ID_Edt.getText().toString().matches("^[0-9a-zA-Z]*")){
                        register_ID_Txt.setText("ID는 영문 대/소문자, 숫자로 작성해 주세요");
                        isIDPassed=false;
                    }else{
                        register_ID_Txt.setText("");
                        isIDPassed=true;
                    }
                }else{
                    register_ID_Txt.setVisibility(View.GONE);
                    register_ID_Txt.setText("");
                }
            }
        });


        register_ID_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                register_ID_Txt.setVisibility(View.VISIBLE);
                if(register_ID_Edt.length()<8){
                    register_ID_Txt.setText("ID는 8자 이상 이어야 합니다");
                    isIDPassed=false;
                }else if(register_ID_Edt.length()>16){
                    register_ID_Txt.setText("ID는 16자 이하 이어야 합니다");
                    isIDPassed=false;
                }else if(register_ID_Edt.getText().toString().matches("^[0-9]*")){
                    register_ID_Txt.setText("ID에 영문자를 추가해 주세요");
                    isIDPassed=false;
                }else if (!register_ID_Edt.getText().toString().matches("^[0-9a-zA-Z]*")){
                    register_ID_Txt.setText("ID는 영문 대/소문자, 숫자로 작성해 주세요");
                    isIDPassed=false;
                }else{
                    register_ID_Txt.setText("");
                    isIDPassed=true;
                }
            }
        });

        register_Nickname_Edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    register_Nickname_Txt.setVisibility(View.VISIBLE);
                    if(register_Nickname_Edt.length()<2){
                        register_Nickname_Txt.setText("닉네임은 2자 이상 이어야 합니다");
                        isNicknamePassed=false;
                    }else if(register_Nickname_Edt.length()>5){
                        register_Nickname_Txt.setText("닉네임은 5자 이하 이어야 합니다");
                        isNicknamePassed=false;
                    }else if (!register_Nickname_Edt.getText().toString().matches("^[0-9a-zA-Z가-힣]*")){
                        register_Nickname_Txt.setText("닉네임은 한글, 영문 대/소문자, 숫자로 작성해 주세요");
                        isNicknamePassed=false;
                    }else{
                        register_Nickname_Txt.setText("");
                        isNicknamePassed=true;
                    }
                }else{
                    register_Nickname_Txt.setVisibility(View.GONE);
                    register_Nickname_Txt.setText("");
                }
            }
        });

        register_Nickname_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(register_Nickname_Edt.length()<2){
                    register_Nickname_Txt.setText("닉네임은 2자 이상 이어야 합니다");
                    isNicknamePassed=false;
                }else if(register_Nickname_Edt.length()>5){
                    register_Nickname_Txt.setText("닉네임은 5자 이하 이어야 합니다");
                    isNicknamePassed=false;
                }else if (!register_Nickname_Edt.getText().toString().matches("^[0-9a-zA-Z가-힣]*")){
                    register_Nickname_Txt.setText("닉네임은 한글, 영문 대/소문자, 숫자로 작성해 주세요");
                    isNicknamePassed=false;
                }else{
                    register_Nickname_Txt.setText("");
                    isNicknamePassed=true;
                }
            }
        });

        register_Password_Edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    register_Password_Txt.setVisibility(View.VISIBLE);
                    if(register_Password_Edt.length()<6){
                        register_Password_Txt.setText("비밀번호는 6자 이상 이어야 합니다");
                        isPasswordPassed=false;
                    }else if(register_Password_Edt.length()>16){
                        register_Password_Txt.setText("비밀번호는 16자 이하 이어야 합니다");
                        isPasswordPassed=false;
                    }else if (!register_Password_Edt.getText().toString().matches("^[0-9a-zA-Z!@#$%]*")){
                        register_Password_Txt.setText("비밀번호는 영문 대/소문자, 숫자, 특수문자 !@#$%로 작성해 주세요");
                        isPasswordPassed=false;
                    }else if(register_Password_Edt.getText().toString().matches("^[0-9]*")||register_Password_Edt.getText().toString().matches("^[a-zA-Z]*")){
                        register_Password_Txt.setText("비밀번호는 숫자나 문자로만 이루어질 수 없습니다");
                        isPasswordPassed=false;
                    }else{
                        register_Password_Txt.setText("");
                        isPasswordPassed=true;
                    }
                }else{
                    register_Password_Txt.setVisibility(View.GONE);
                    register_Password_Txt.setText("");
                }
            }
        });

        register_Password_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(register_Password_Edt.length()<6){
                    register_Password_Txt.setText("비밀번호는 6자 이상 이어야 합니다");
                    isPasswordPassed=false;
                }else if(register_Password_Edt.length()>16){
                    register_Password_Txt.setText("비밀번호는 16자 이하 이어야 합니다");
                    isPasswordPassed=false;
                }else if (!register_Password_Edt.getText().toString().matches("^[0-9a-zA-Z!@#$%]*")){
                    register_Password_Txt.setText("비밀번호는 영문 대/소문자, 숫자, 특수문자 !@#$%로 작성해 주세요");
                    isPasswordPassed=false;
                }else if(register_Password_Edt.getText().toString().matches("^[0-9]*")||register_Password_Edt.getText().toString().matches("^[a-zA-Z]*")){
                    register_Password_Txt.setText("비밀번호는 숫자나 문자로만 이루어질 수 없습니다");
                    isPasswordPassed=false;
                }else{
                    register_Password_Txt.setText("");
                    isPasswordPassed=true;
                }
            }
        });
        register_PasswordChk_Edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    register_PasswordChk_Txt.setVisibility(View.VISIBLE);
                    if(!register_PasswordChk_Edt.getText().toString().equals(register_Password_Edt.getText().toString())){
                        register_PasswordChk_Txt.setText("위의 비밀번호와 다릅니다.");
                        isPasswordChkPassed=false;
                    }else{
                        register_PasswordChk_Txt.setText("");
                        isPasswordChkPassed=true;
                    }
                }else{
                    register_PasswordChk_Txt.setVisibility(View.GONE);
                }
            }
        });
        register_PasswordChk_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!register_PasswordChk_Edt.getText().toString().equals(register_Password_Edt.getText().toString())) {
                    register_PasswordChk_Txt.setText("위의 비밀번호와 다릅니다.");
                    isPasswordChkPassed=false;
                }else {
                    register_PasswordChk_Txt.setText("");
                    isPasswordChkPassed=true;
                }
            }
        });

        register_Phone_Edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                register_Phone_Txt.setVisibility(View.VISIBLE);
                if(hasFocus) {
                    if (!register_Phone_Edt.getText().toString().matches("[0-9][0-9]*")) {
                        register_Phone_Txt.setText("번호는 - 나 특수기호 없이 숫자로만~♥");
                        isPhonePassed=false;
                    }else if(register_Phone_Edt.length()<11){
                        register_Phone_Txt.setText("휴대번호가 짧습니다.");
                        isPhonePassed=false;
                    }else if(register_Phone_Edt.length()>11){
                        register_Phone_Txt.setText("휴대번호가 깁니다.");
                        isPhonePassed=false;
                    }else{
                        register_Phone_Txt.setText("");
                        isPhonePassed=true;
                    }
                }else{
                    register_Phone_Txt.setVisibility(View.GONE);
                }
            }
        });

        register_Phone_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!register_Phone_Edt.getText().toString().matches("[0-9][0-9]*")) {
                    register_Phone_Txt.setText("번호는 - 나 특수기호 없이 숫자로만~♥");
                    isPhonePassed=false;
                }else if(register_Phone_Edt.length()<11){
                    register_Phone_Txt.setText("휴대번호가 짧습니다.");
                    isPhonePassed=false;
                }else if(register_Phone_Edt.length()>11){
                    register_Phone_Txt.setText("휴대번호가 깁니다.");
                    isPhonePassed=false;
                }else{
                    register_Phone_Txt.setText("");
                    isPhonePassed=true;
                }
            }
        });

        register_Name_Edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    register_Name_Txt.setVisibility(View.VISIBLE);
                    if (!register_Name_Edt.getText().toString().matches("[가-힣][가-힣]*")){
                        register_Name_Txt.setText("이름은 한글로만 입력해 주세요");
                        isNamePassed=false;
                    }else if(register_Name_Edt.length()<2){
                        register_Name_Txt.setText("이름은 2~4자로 입력해 주세요");
                        isNamePassed=false;
                    }else if(register_Name_Edt.length()>4){
                        register_Name_Txt.setText("이름은 2~4자로 입력해 주세요");
                        isNamePassed=false;
                    }else{
                        register_Name_Txt.setText("");
                        isNamePassed=true;
                    }
                }else {
                    register_Name_Txt.setVisibility(View.GONE);
                }
            }
        });

        register_Name_Edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!register_Name_Edt.getText().toString().matches("[가-힣][가-힣]*")){
                    register_Name_Txt.setText("이름은 한글로만 입력해 주세요");
                    isNamePassed=false;
                }else if(register_Name_Edt.length()<2){
                    register_Name_Txt.setText("이름은 2~4자로 입력해 주세요");
                    isNamePassed=false;
                }else if(register_Name_Edt.length()>4){
                    register_Name_Txt.setText("이름은 2~4자로 입력해 주세요");
                    isNamePassed=false;
                }else{
                    register_Name_Txt.setText("");
                    isNamePassed=true;
                }
            }
        });
    }

    private class SendPost extends AsyncTask<String,Integer,String>{
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(RegisterActivity.this);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/register.php");
                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id",re_Id_value)
                        .appendQueryParameter("password",re_Password_value)
                        .appendQueryParameter("name",re_Name_value)
                        .appendQueryParameter("phone",re_Phone_value)
                        .appendQueryParameter("nickname",re_Nickname_value);
                String query = builder.build().getEncodedQuery();
                OutputStream outStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(),"UTF-8"));
                while(true){
                    String line = br.readLine();
                    if(line == null) break;
                    Log.d("get",line);
                    result += line;
                }
                http.disconnect();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!result.contains("fail")){
                Toast.makeText(getApplicationContext(),"회원가입 성공",Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()  {
                    public void run() {
                        finish();
                    }
                }, 1000);
            }
            else Toast.makeText(getApplicationContext(),"회원가입 실패",Toast.LENGTH_SHORT).show();
            asyncProgressDialog.dismiss();
        }
    }

}

