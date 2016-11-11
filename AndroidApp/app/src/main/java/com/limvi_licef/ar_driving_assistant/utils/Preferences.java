package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {

    private Preferences(){}

    private static final String DEFAULT_ID = "0";

    /*
     * Returns the current user id defined in 'Setup' Preferences button
     */
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        return prefs.getString(Constants.ID_PREFERENCE, DEFAULT_ID);
    }

    /*
     * Returns the ip address defined in 'Setup' Preferences button
     */
    public static String getIPAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        return prefs.getString(Constants.IP_ADDRESS_PREFERENCE, null);
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
