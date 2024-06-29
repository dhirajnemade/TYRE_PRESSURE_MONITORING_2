package com.example.tpms;

/**
 * Created by shrad on 07-Apr-18.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.audiofx.BassBoost;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;



import java.lang.reflect.Type;
import java.util.ArrayList;


public class UniversalHelper {

    private static Context myContext;
    private Activity myActivity;


    private static final String APPNAME = "tpms";

    private static final String TAG = "UniversalHelper";

    public UniversalHelper(Context context) {
        // TODO Auto-generated constructor stub
        myContext = context;


    }
    public static void savePreferences(String key, String value) {
        @SuppressWarnings("static-access")
        SharedPreferences preferences = myContext.getSharedPreferences(APPNAME, myContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }




    public static String loadPreferences(String key) {
        String strValue = "";
        @SuppressWarnings("static-access")
        SharedPreferences preferences = myContext.getSharedPreferences(APPNAME, myContext.MODE_PRIVATE);
        strValue = preferences.getString(key, "");

        return strValue;
    }




}

