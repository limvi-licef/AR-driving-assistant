package com.limvi_licef.ar_driving_assistant.config;

/**
 * See http://www.awareframework.com/linear-accelerometer/
 * http://www.awareframework.com/locations/
 * http://www.awareframework.com/plugin/?package=com.aware.plugin.openweather
 */
public final class AwareSettings {

    /**
     * true or false to activate or deactivate accelerometer sensor
     */
    public static final boolean ACCELEROMETER_ENABLED = true;

    /**
     * non-deterministic frequency in microseconds (dependent of the hardware sensor capabilities and resources)
     */
    public static final int ACCELEROMETER_FREQUENCY = 200000;

    /**
     * true or false to activate or deactivate GPS locations
     */
    public static final boolean LOCATION_GPS_ENABLED = true;

    /**
     * true or false to activate or deactivate Network locations
     */
    public static final boolean LOCATION_NETWORK_ENABLED = false;

    /**
     * how frequent to check the GPS location, in seconds. By default, every 180 seconds
     * Setting to 0 (zero) will keep the GPS location tracking always on
     */
    public static final int LOCATION_FREQUENCY = 0;

    /**
     * the minimum acceptable accuracy of GPS location, in meters
     * By default, 150 meters. Setting to 0 (zero) will keep the GPS location tracking always on
     */
    public static final int LOCATION_MIN_GPS_ACCURACY = 0;

    /**
     * the amount of elapsed time, in seconds, until the location is considered outdated.
     * By default, 300 seconds
     */
    public static final int LOCATION_EXPIRATION_TIME = 0;

    /**
     * true or false to activate or deactivate plugin
     */
    public static final boolean OPENWEATHER_ENABLED = true;

    /**
     * How frequently to fetch weather information (in minutes), default 60 minutes
     */
    public static final double OPENWEATHER_FREQUENCY = 30;
}
