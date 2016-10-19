package com.limvi_licef.ar_driving_assistant;

import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Time;
import java.util.List;

public class Utils {

    private static final String DEFAULT_ID = "0";

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

    /*
     * Struct to return both the significant extrema and the processed data from the segmentation algorithm
     */
    public static class SegmentationAlgorithmReturnData {
        public List<Integer> significantExtremaIndex;
        public List<TimestampedDouble> monotoneValues;

        public SegmentationAlgorithmReturnData(List<Integer> significantExtremaIndex, List<TimestampedDouble> monotoneValues) {
            this.significantExtremaIndex = significantExtremaIndex;
            this.monotoneValues = monotoneValues;
        }
    }
}
