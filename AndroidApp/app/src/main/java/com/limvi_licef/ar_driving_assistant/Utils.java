package com.limvi_licef.ar_driving_assistant;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    public static final String DEFAULT_ID = "0";

    public static String getCurrentUserId(Context context) {
        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        return prefs.getString(idPref, DEFAULT_ID);
    }
}
