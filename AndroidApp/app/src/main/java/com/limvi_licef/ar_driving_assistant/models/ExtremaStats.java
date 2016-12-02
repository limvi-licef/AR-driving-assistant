package com.limvi_licef.ar_driving_assistant.models;

/*
 * Data structure to hold the statistics of a list of extremum
 */
public class ExtremaStats {
    public double positiveAverage;
    public double negativeAverage;
    public double positiveStandardDeviation;
    public double negativeStandardDeviation;
    public int positiveCount;
    public int negativeCount;

    public ExtremaStats(double positiveAverage, double negativeAverage, double positiveStandardDeviation, double negativeStandardDeviation, int positiveCount, int negativeCount) {
        this.positiveAverage = positiveAverage;
        this.negativeAverage = negativeAverage;
        this.positiveStandardDeviation = positiveStandardDeviation;
        this.negativeStandardDeviation = negativeStandardDeviation;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
    }
}
