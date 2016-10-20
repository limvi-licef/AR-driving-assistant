package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.limvi_licef.ar_driving_assistant.R;

public abstract class User {

    private static final String DEFAULT_ID = "0";

    /*
     * Returns the current user id defined in 'Setup User' button
     */
    public static String getCurrentUserId(Context context) {
        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        return prefs.getString(idPref, DEFAULT_ID);
    }
}
