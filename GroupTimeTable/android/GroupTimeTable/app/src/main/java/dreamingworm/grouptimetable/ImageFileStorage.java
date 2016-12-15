package dreamingworm.grouptimetable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Youngs on 2016-07-26.
 */
public class ImageFileStorage {
    public static void saveBitmaptoJpeg(Bitmap bitmap, String name) {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/Worm/";
        String file_name = name + ".jpg";
        String string_path = ex_storage + foler_name;

        File file_path;
        try {
            file_path = new File(string_path);
            if (!file_path.isDirectory()) {
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path + file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        } catch (FileNotFoundException exception) {
            Log.e("FileNotFoundException", exception.getMessage());
        } catch (IOException exception) {
            Log.e("IOException", exception.getMessage());
        }
    }

    public static Bitmap loadJpegtoBitmap(String name) {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/Worm/";
        String file_name = name + ".jpg";
        String string_path = ex_storage + foler_name+file_name;

        Bitmap bitmap = BitmapFactory.decodeFile(string_path);
        return bitmap;
    }

    public static boolean checkFile(String name) {
        //path 부분엔 파일 경로를 지정해주세요.
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/worm/";
        String file_name = name + ".jpg";
        String string_path = ex_storage + foler_name + file_name;
        File files = new File(string_path);
        //파일 유무를 확인합니다.
        if (files.exists() == true) {
            //파일이 있을시
            return true;
        } else {
            //파일이 없을시
            return false;
        }
    }
}
