package com.limvi_licef.ar_driving_assistant;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    public static final String DEFAULT_ID = "0";

    /*
     * Returns the current user id defined in 'Setup User' button
     */
    public static String getCurrentUserId(Context context) {
        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        return prefs.getString(idPref, DEFAULT_ID);
    }

    /*
     * Struct that holds a Double and a timestamp
     * Used to pass data through an algorithm but still retain its associated timestamp
     */
    public static class TimestampedDouble {
        public long timestamp;
        public Double value;

        public TimestampedDouble(long timestamp, Double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }
}
