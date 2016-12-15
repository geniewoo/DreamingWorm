package dreamingworm.grouptimetable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Youngs on 2016-07-20.
 */
public class ProfileActivity extends AppCompatActivity{
    final int REQ_CODE_SELECT_IMAGE=100;
    Bitmap image_bitmap;
    Bitmap raw_Image;
    String id;
    String result;
    String fileName;
    String sumnailName;
    String image;
    String sumnail;
    Button profile_Summit_Btn;
    Button profile_Delete_Btn;
    ImageView profile_Image_View;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (!NetworkConn.isNetworkConnected(this)) {
            Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(this);
            networkConnDialog.show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("environ",0);
        id = preferences.getString("ID","");
        profile_Image_View = (ImageView)findViewById(R.id.profile_Image_View);
        profile_Summit_Btn = (Button)findViewById(R.id.profile_Summit_Btn);
        profile_Delete_Btn = (Button)findViewById(R.id.profile_Delete_Btn);
        profile_Image_View.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        raw_Image = BitmapFactory.decodeResource(getResources(),R.drawable.worm);

        new GetPost().execute();

        profile_Image_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
                overridePendingTransition(R.anim.hold,R.anim.hold);
            }
        });
        profile_Summit_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(ProfileActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(ProfileActivity.this);
                    networkConnDialog.show();
                    return;
                }
                new PutPost().execute();
            }
        });
        profile_Delete_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConn.isNetworkConnected(ProfileActivity.this)) {
                    Dialog networkConnDialog = NetworkConnDialog.createNetworkConnDialog(ProfileActivity.this);
                    networkConnDialog.show();
                    return;
                }
                new DeletePost().execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQ_CODE_SELECT_IMAGE)
        {
            if(resultCode== RESULT_OK)
            {
                try {

                    //이미지 데이터를 비트맵으로 받아온다.
                    image_bitmap 	= MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    int height = image_bitmap.getHeight();
                    int width = image_bitmap.getWidth();
                    if(height > 500) {
                        Bitmap sizingBmp = Bitmap.createScaledBitmap(image_bitmap, width*500/height, 500, true);
                        //배치해놓은 ImageView에 set
                        profile_Image_View.setImageBitmap(sizingBmp);
                    }else{
                        profile_Image_View.setImageBitmap(image_bitmap);
                    }

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PutPost extends AsyncTask<String, Integer, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(ProfileActivity.this);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            fileName = id+".jpg";
            sumnailName = id+"_sumnail.jpg";
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            int height = image_bitmap.getHeight();
            int width = image_bitmap.getWidth();
            if(height > 500) {
                Bitmap sizingBmp = Bitmap.createScaledBitmap(image_bitmap, width * TimeTableInfo.dpToPx(getApplicationContext(),500) / height, TimeTableInfo.dpToPx(getApplicationContext(),500), true);
                sizingBmp.compress(Bitmap.CompressFormat.JPEG,100,bao);
            }else{
                image_bitmap.compress(Bitmap.CompressFormat.JPEG,100,bao);
            }
            byte [] ba = bao.toByteArray();
            image = Base64.encodeToString(ba,Base64.DEFAULT);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap sizingBmp2 = Bitmap.createScaledBitmap(image_bitmap, width*TimeTableInfo.dpToPx(getApplicationContext(),50)/height, TimeTableInfo.dpToPx(getApplicationContext(),50), true);
            sizingBmp2.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte [] bas = baos.toByteArray();
            sumnail = Base64.encodeToString(bas,Base64.DEFAULT);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/putJPG.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id",id)
                        .appendQueryParameter("fileName", fileName)
                        .appendQueryParameter("image",image)
                        .appendQueryParameter("sumnail",sumnail)
                        .appendQueryParameter("sumnailName",sumnailName);
                String query = builder.build().getEncodedQuery();
                OutputStream outStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                    Log.d("twat Profile", line);
                    result += line;
                }
                http.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (result.matches("success")) {
                Toast.makeText(getApplicationContext()," 사진 등록 성공", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(R.anim.hold,R.anim.hold);
            } else
                Toast.makeText(getApplicationContext(), "사진 등록 실패", Toast.LENGTH_SHORT).show();
            asyncProgressDialog.dismiss();
        }
    }

    private class GetPost extends AsyncTask<String, Integer, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(ProfileActivity.this);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            fileName = id+".jpg";
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL("http://pama.dothome.co.kr/"+fileName);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    http.connect();
                    InputStream is = http.getInputStream();
                    image_bitmap = BitmapFactory.decodeStream(is);
                    http.disconnect();
                }else{
                    image_bitmap = raw_Image;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
                profile_Image_View.setImageBitmap(image_bitmap);
            asyncProgressDialog.dismiss();
        }

    }

    private class DeletePost extends AsyncTask<String, Integer, String> {
        AsyncProgressDialog asyncProgressDialog=new AsyncProgressDialog(ProfileActivity.this);

        @Override
        protected void onPreExecute() {
            asyncProgressDialog.show();
            super.onPreExecute();
            fileName = id+".jpg";
            sumnailName = id+"_sumnail.jpg";
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                result = new String();
                URL url = new URL("http://pama.dothome.co.kr/deleteJPG.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDefaultUseCaches(false);
                http.setDoInput(true);
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id",id)
                        .appendQueryParameter("fileName", fileName)
                        .appendQueryParameter("sumnailName",sumnailName);
                String query = builder.build().getEncodedQuery();
                OutputStream outStream = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                http.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                    Log.d("twat Profile", line);
                    result += line;
                }
                http.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (result.matches("success")) {
                Toast.makeText(getApplicationContext()," 사진 삭제 성공", Toast.LENGTH_SHORT).show();
                image_bitmap = raw_Image;
                profile_Image_View.setImageBitmap(image_bitmap);
            } else
                Toast.makeText(getApplicationContext(), "사진 삭제 실패", Toast.LENGTH_SHORT).show();
            asyncProgressDialog.dismiss();
        }
    }
}
