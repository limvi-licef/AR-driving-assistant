package com.limvi_licef.ar_driving_assistant.utils;

public final class Config {

    /**
     * Contains constants used for communication with the HoloLens
     */
    public static final class HoloLens {

        /**
         * Port that the HoloLens is listening on for receiving new events
         */
        public static final int HOLOLENS_PORT = 12345;

        public static final String JSON_REQUEST_TYPE_PARAM_EVENT = "event";
        public static final String JSON_EVENT_TYPE = "eventType";
        public static final String JSON_EVENT_MESSAGE = "message";

        public static final String JSON_REQUEST_TYPE = "requestType";

        public static final String JSON_REQUEST_TYPE_USERS = "GetUsersList";
        public static final String JSON_REQUEST_TYPE_PARAM_USER = "userList";
        public static final String JSON_USERS = "users";

        public static final String JSON_REQUEST_TYPE_INSERT_USER = "InsertNewUser";
		public static final String JSON_REQUEST_TYPE_PARAM_NEW_USER = "newUser";
        public static final String JSON_REQUEST_RETURN_VALUES_NAME = "userName";
        public static final String JSON_REQUEST_RETURN_VALUES_AGE = "userAge";
        public static final String JSON_REQUEST_RETURN_VALUES_GENDER = "userGender";
        public static final String JSON_REQUEST_RETURN_VALUES_AVATAR = "userAvatar";

        public static final String JSON_REQUEST_TYPE_LAST_KNOWN = "GetLastKnownRides";
        public static final String JSON_REQUEST_TYPE_PARAM_RIDES = "userRidesList";
        public static final String JSON_RIDES = "rides";
        public static final String JSON_REQUEST_ID = "userId";
        public static final String JSON_LAST_KNOWN_ERROR_MESSAGE = "Impossible de récupérer les derniers trajets";
        public static final String JSON_LAST_KNOWN_EMPTY_RESULT = "Aucunes données récupérées";
        public static final String JSON_LAST_KNOWN_SUCCESS = "Success";

        public static final String JSON_REQUEST_TYPE_PARAM_SPEED = "speedCounter";
        public final static String JSON_SPEED_UNITS = " km/h";

        public static final String JSON_RETURN_STATUS = "status";
    }

    /**
     * Contains constants used for sensors monitoring
     */
    public static final class SensorDataCollection {

        /**
         * Determines how long the ComputeAlgorithm runnables accumulate data before running
         */
        public static final int SHORT_DELAY = 1000 * 60;

        /**
         * Determines how long the RewriteAlgorithm runnables accumulate data before running
         */
        public static final int LONG_DELAY = 1000 * 60 * 10;

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
        public static final long ROTATION_PRECISION = 100;

        /**
         * Precision, in milliseconds, of the linear accelerometer sensor timestamp rounding
         */
        public static final long ACCELERATION_PRECISION = 100;
    }

    /**
     * Contains constants used for the Dynamic Time Warping algorithm
     */
    public static final class DynamicTimeWarping {

        /**
         * The cutoff for determining whether a match is found between two TimeSeries of acceleration data
         */
        public static final double ACCELERATION_DISTANCE_CUTOFF = 15;

        /**
         * The cutoff for determining whether a match is found between two TimeSeries of rotation data
         */
        public static final double ROTATION_DISTANCE_CUTOFF = 200;

        /**
         * The cutoff for determining whether a match is found between two TimeSeries of speed data
         */
        public static final double SPEED_DISTANCE_CUTOFF = 10;

        /**
         * Time, in milliseconds, between each segments of recent data
         * In other words, the actual value of the +1 below
         *
         *      |------|------|------>
         *      x      y
         *        |------|
         *       x+1    y+1
         */
        public static final double TIME_BETWEEN_SEGMENTS = 100;

        /**
         * The search radius for matching the two TimeSeries
         */
        public static final int SEARCH_RADIUS = 10;
    }

    /**
     * See http://www.awareframework.com/linear-accelerometer/
     * http://www.awareframework.com/locations/
     * http://www.awareframework.com/plugin/?package=com.aware.plugin.openweather
     */
    public static final class AwareSettings {

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
}
