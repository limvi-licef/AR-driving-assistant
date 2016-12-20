package com.limvi_licef.ar_driving_assistant.config;

/**
 * Contains constants used for sensors monitoring
 */
public final class SensorDataCollection {

    /**
     * Determines how long, in milliseconds, the ComputeAlgorithm runnables accumulate data before running
     */
    public static final int SHORT_DELAY = 1000 * 60; //1 minute

    /**
     * Determines how long, in milliseconds, the RewriteAlgorithm runnables accumulate data before running
     */
    public static final int LONG_DELAY = 1000 * 60 * 10; //10 minutes

    /**
     * Tolerance for the monotone segmentation algorithm
     */
    public static final int MONOTONE_SEGMENTATION_TOLERANCE = 1;

    /**
     * Minimum delay between each sensor datapoint to avoid clutter, in milliseconds
     */
    public static final long MINIMUM_DELAY = 10;

    /**
     * Precision, in milliseconds, of the rotation sensor timestamp rounding
     */
    public static final long ROTATION_PRECISION = 200;

    /**
     * Precision, in milliseconds, of the linear accelerometer sensor timestamp rounding
     */
    public static final long ACCELERATION_PRECISION = 200;

    /**
     * Defines whether or not the receivers notify the UI when they receive data
     */
    public static final boolean LOGGING_ENABLED = false;
}
