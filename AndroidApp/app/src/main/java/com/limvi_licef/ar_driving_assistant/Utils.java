package com.limvi_licef.ar_driving_assistant;

import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Time;
import java.util.ArrayList;
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

    /*
     * Struct to hold the statistics of a list of extremum
     */
    public static class ExtremaStats {
        public double positiveAverage;
        public double negativeAverage;
        public double positiveStandardDeviation;
        public double negativeStandardDeviation;

        public ExtremaStats(double positiveAverage, double negativeAverage, double positiveStandardDeviation, double negativeStandardDeviation) {
            this.positiveAverage = positiveAverage;
            this.negativeAverage = negativeAverage;
            this.positiveStandardDeviation = positiveStandardDeviation;
            this.negativeStandardDeviation = negativeStandardDeviation;
        }
    }

    /*
     * Return the average of the list values or 0 if the list is empty
     */
    public static double calculateAverage(List<Double> list) {
        if(list.isEmpty()) return 0;
        double sum = 0;
        for (Double mark : list) {
            sum += mark;
        }
        return sum / list.size();
    }

    /*
     * Return the standard deviation of the list using the average or 0 if the list is empty
     */
    public static double calculateStandardDeviation(List<Double> list, double average) {
        if(list.isEmpty()) return 0;
        double squaredSum = 0;
        for(Double d : list) {
            squaredSum += (d-average)*(d-average);
        }
        return Math.sqrt(squaredSum / list.size());
    }

    //TODO Move to appropriate location
    public static Utils.ExtremaStats computeExtremaStats(List<Utils.TimestampedDouble> values, List<Integer> indexes){
        List<Double> negativeExtrema = new ArrayList<>();
        List<Double> positiveExtrema = new ArrayList<>();
        for(int i = 0; i < indexes.size() - 1; ++ i) {
            Double a = values.get(indexes.get(i)).value;
            Double b = values.get(indexes.get(i+1)).value;
            if(a > b) {
                negativeExtrema.add(a);
            } else {
                positiveExtrema.add(a);
            }
        }
        Double positiveAverage = Utils.calculateAverage(positiveExtrema);
        Double negativeAverage = Utils.calculateAverage(negativeExtrema);
        return new Utils.ExtremaStats(positiveAverage, negativeAverage,
                Utils.calculateStandardDeviation(positiveExtrema, positiveAverage),
                Utils.calculateStandardDeviation(negativeExtrema, negativeAverage));
    }
}
