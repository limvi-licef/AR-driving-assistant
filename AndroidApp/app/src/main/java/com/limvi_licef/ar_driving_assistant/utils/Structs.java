package com.limvi_licef.ar_driving_assistant.utils;

import java.util.List;
import java.util.Map;

public abstract class Structs {

    /*
    * Struct that holds a Double, a Timestamp and optional extra data
    * Used to pass data through an algorithm but still retain its associated timestamp and data
    */
    public static class TimestampedDouble {
        public long timestamp;
        public Double value;
        public Map<String, Double> extraData;

        public TimestampedDouble(long timestamp, Double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public TimestampedDouble(long timestamp, Double value, Map<String, Double> extraData) {
            this.timestamp = timestamp;
            this.value = value;
            this.extraData = extraData;
        }
    }

    /*
     * Struct to return both the significant extrema and the processed data from the segmentation algorithm
     */
    public static class SegmentationAlgorithmReturnData {
        public ExtremaStats extremaStats;
        public List<TimestampedDouble> monotoneValues;

        public SegmentationAlgorithmReturnData(ExtremaStats extremaStats, List<TimestampedDouble> monotoneValues) {
            this.extremaStats = extremaStats;
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
}
