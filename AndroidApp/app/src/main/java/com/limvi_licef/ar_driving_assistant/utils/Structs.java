package com.limvi_licef.ar_driving_assistant.utils;

import java.util.List;
import java.util.Map;

public final class Structs {

    private Structs(){}

    /*
    * Struct that holds a Double and a Timestamp
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

    /**
     * User class used to User information to the UnityApp
     */
    public static class User {
        public String userName;
        public int userAge;
        public String userGender;
        public int userAvatar;

        public User(String userName, int userAge, String userGender, int userAvatar){
            this.userName = userName;
            this.userAge = userAge;
            this.userGender = userGender;
            this.userAvatar = userAvatar;
        }
    }

    /**
     * TimestampTuple class to hold two timestamps
     */
    public static class TimestampTuple {
        public long first;
        public long second;

        public TimestampTuple(long first, long second) {
            this.first = first;
            this.second = second;
        }
    }
}
