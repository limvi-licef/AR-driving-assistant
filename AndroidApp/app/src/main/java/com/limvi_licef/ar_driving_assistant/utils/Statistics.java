package com.limvi_licef.ar_driving_assistant.utils;

import com.limvi_licef.ar_driving_assistant.utils.Structs.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

import java.util.ArrayList;
import java.util.List;

public final class Statistics {

    private Statistics(){}

    /**
     * Rounds off the given timestamp
     * @param timestamp the timestamp to round
     * @param precision timestamp precision in milliseconds
     * @return
     */
    public static long roundOffTimestamp(long timestamp, long precision) {
        return precision * (( timestamp + precision / 2 ) / precision);
    }

    /**
     * Calculate the average of the given list
     * @param list to calculate the average of
     * @return the average or 0 if list is empty
     */
    public static double calculateAverage(List<Double> list) {
        if(list.isEmpty()) return 0;
        double sum = 0;
        for (Double mark : list) {
            sum += mark;
        }
        return sum / list.size();
    }

    /**
     * Calculates the standard deviation of the given list
     * @param list
     * @param average
     * @return the standard deviation or 0 if list is empty
     */
    public static double calculateStandardDeviation(List<Double> list, double average) {
        if(list.isEmpty()) return 0;
        double squaredSum = 0;
        for(Double d : list) {
            squaredSum += (d-average)*(d-average);
        }
        return Math.sqrt(squaredSum / list.size());
    }

    /**
     * Calculates stats about the peaks in the given values list
     * @param values the list of timestamped values to calculate stats from
     * @param indexes the significant indexes (peaks indexes)
     * @return an ExtremaStats object containing the stats
     */
    public static ExtremaStats computeExtremaStats(List<TimestampedDouble> values, List<Integer> indexes){
        List<Double> negativeExtrema = new ArrayList<>();
        List<Double> positiveExtrema = new ArrayList<>();
        for(int i = 0; i < indexes.size() - 1; ++ i) {
            Double a = values.get(indexes.get(i)).value;
            Double b = values.get(indexes.get(i+1)).value;
            if(a > b) {
                positiveExtrema.add(a);
            } else if (a < b) {
                negativeExtrema.add(a);
            }
        }
        Double positiveAverage = calculateAverage(positiveExtrema);
        Double negativeAverage = calculateAverage(negativeExtrema);
        return new ExtremaStats(positiveAverage, negativeAverage,
                calculateStandardDeviation(positiveExtrema, positiveAverage),
                calculateStandardDeviation(negativeExtrema, negativeAverage),
                positiveExtrema.size(), negativeExtrema.size());
    }
}
