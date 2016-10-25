package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.limvi_licef.ar_driving_assistant.R;

public abstract class Preferences {

    private static final String DEFAULT_ID = "0";

    /*
     * Returns the current user id defined in 'Setup Preferences' button
     */
    public static String getCurrentUserId(Context context) {
        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        return prefs.getString(idPref, DEFAULT_ID);
    }

    /*
     * http://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
     * Shared Preferences getter and setter for double
     */
    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}
