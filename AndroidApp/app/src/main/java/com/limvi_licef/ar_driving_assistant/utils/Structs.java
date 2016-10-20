package com.limvi_licef.ar_driving_assistant.utils;

import java.util.List;

public abstract class Structs {

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
}