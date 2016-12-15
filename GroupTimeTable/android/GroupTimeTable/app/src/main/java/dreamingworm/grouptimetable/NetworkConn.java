package dreamingworm.grouptimetable;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;

/**
 * Created by sungwoo on 2016-08-03.
 */
public class NetworkConn {
    static public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        return cm.getActiveNetworkInfo() != null;
    }

    static public boolean isInternetAvailable()
    {
        try
        {
            InetAddress ipAddr = InetAddress.getByName( "google.com" ); //You can replace it with your name

            if( ipAddr.equals( "" ) )
            {
                return false;
            }
            else
            {
                return true;
            }

        }
        catch( Exception e )
        {
            return false;
        }
    }
}
