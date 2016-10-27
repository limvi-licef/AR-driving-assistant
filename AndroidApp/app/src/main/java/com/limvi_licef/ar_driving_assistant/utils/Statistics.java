package com.limvi_licef.ar_driving_assistant.utils;

import com.limvi_licef.ar_driving_assistant.utils.Structs.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

import java.util.ArrayList;
import java.util.List;

public abstract class Statistics {

    /*
     * Returns the average of the list values or 0 if the list is empty
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
     * Returns the standard deviation of the list using the average or 0 if the list is empty
     */
    public static double calculateStandardDeviation(List<Double> list, double average) {
        if(list.isEmpty()) return 0;
        double squaredSum = 0;
        for(Double d : list) {
            squaredSum += (d-average)*(d-average);
        }
        return Math.sqrt(squaredSum / list.size());
    }

    /*
     * Calculates and returns the average and the standard deviation for both increases and decreases of a list of timestamped extrema
     */
    public static ExtremaStats computeExtremaStats(List<TimestampedDouble> values, List<Integer> indexes){
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
        Double positiveAverage = calculateAverage(positiveExtrema);
        Double negativeAverage = calculateAverage(negativeExtrema);
        return new ExtremaStats(positiveAverage, negativeAverage,
                calculateStandardDeviation(positiveExtrema, positiveAverage),
                calculateStandardDeviation(negativeExtrema, negativeAverage),
                positiveExtrema.size(), negativeExtrema.size());
    }
}
