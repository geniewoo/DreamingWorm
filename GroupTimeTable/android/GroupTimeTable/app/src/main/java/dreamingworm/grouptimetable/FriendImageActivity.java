package dreamingworm.grouptimetable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Youngs on 2016-08-12.
 */
public class FriendImageActivity extends AppCompatActivity {
    String fid;
    String fileName;
    Bitmap image_bitmap;
    Bitmap raw_Image;
    ImageView imageView;
    Button button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendimage);
        imageView = (ImageView)findViewById(R.id.friendImage_Image_View);
        button = (Button)findViewById(R.id.friendImage_Ok_Btn);
        fid  = getIntent().getStringExtra("fid");
        raw_Image = BitmapFactory.decodeResource(getResources(),R.drawable.worm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        new GetPost().execute();
    }

    private class GetPost extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fileName = fid+".jpg";
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
            imageView.setImageBitmap(image_bitmap);
        }

    }
}
