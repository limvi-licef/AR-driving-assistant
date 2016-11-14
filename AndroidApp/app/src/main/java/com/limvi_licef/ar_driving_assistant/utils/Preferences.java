package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {

    public static final String USER_SHARED_PREFERENCES = "user_shared";
    public static final String ID_PREFERENCE = "user_id";
    public static final String IP_ADDRESS_PREFERENCE = "user_ip";
    public static final String OFFSET_X_PREF = "offset_x";
    public static final String OFFSET_Y_PREF = "offset_y";
    public static final String OFFSET_Z_PREF = "offset_z";
    private static final String DEFAULT_ID = "0";

    private Preferences(){}

    /*
     * Returns the current user id defined in 'Setup' Preferences button
     */
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(ID_PREFERENCE, DEFAULT_ID);
    }

    /*
     * Returns the ip address defined in 'Setup' Preferences button
     */
    public static String getIPAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(IP_ADDRESS_PREFERENCE, null);
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
