package com.limvi_licef.ar_driving_assistant.config;

/**
 * Contains constants used for the Dynamic Time Warping algorithm
 */
public final class DynamicTimeWarping {

    /**
     * The default cutoff for determining whether a match is found between two TimeSeries of acceleration data
     */
    public static final double DEFAULT_ACCELERATION_DISTANCE_CUTOFF = 15;

    /**
     * The default cutoff for determining whether a match is found between two TimeSeries of rotation data
     */
    public static final double DEFAULT_ROTATION_DISTANCE_CUTOFF = 100;

    /**
     * The default cutoff for determining whether a match is found between two TimeSeries of speed data
     */
    public static final double DEFAULT_SPEED_DISTANCE_CUTOFF = 15;

    /**
     * The search radius for matching the two TimeSeries
     */
    public static final int SEARCH_RADIUS = 10;
}
