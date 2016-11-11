package com.limvi_licef.ar_driving_assistant.database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private static final String TEXT_TYPE          = " TEXT";
    private static final String REAL_TYPE          = " REAL";
    private static final String INTEGER_TYPE          = " INTEGER";
    private static final String DATETIME_TYPE          = " DATETIME";
    private static final String COMMA_SEP          = ",";

    public static final int    DATABASE_VERSION   = 1;
    public static final String DATABASE_NAME      = "drivingAssistant.db";

    public static final String[] SQL_CREATE_TABLE_ARRAY = {
            LinearAccelerometerData.CREATE_TABLE,
            LinearAccelerometerStats.CREATE_TABLE,
            LocationData.CREATE_TABLE,
            SpeedData.CREATE_TABLE,
            SpeedStats.CREATE_TABLE,
            TemperatureData.CREATE_TABLE,
            RotationData.CREATE_TABLE,
            TrainingEvents.CREATE_TABLE
    };

    public static final String[] SQL_DELETE_TABLE_ARRAY = {
            LinearAccelerometerData.DELETE_TABLE,
            LinearAccelerometerStats.DELETE_TABLE,
            LocationData.DELETE_TABLE,
            SpeedData.DELETE_TABLE,
            SpeedStats.DELETE_TABLE,
            TemperatureData.DELETE_TABLE,
            RotationData.DELETE_TABLE,
            TrainingEvents.DELETE_TABLE
    };

    private DatabaseContract() {}

    public static abstract class CommonSensorFields {
        public static final String TIMESTAMP = "Timestamp";
        public static final String USER_ID = "CurrentUserID";
    }

    public static abstract class LinearAccelerometerData implements BaseColumns {
        public static final String TABLE_NAME       = "LinearAccelerometerData";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String TIMESTAMP = "Timestamp";
        public static final String AXIS_X = "AxisX";
        public static final String AXIS_Y = "AxisY";
        public static final String AXIS_Z = "AxisZ";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                TIMESTAMP + DATETIME_TYPE + " UNIQUE" + COMMA_SEP +
                AXIS_X + REAL_TYPE + COMMA_SEP +
                AXIS_Y + REAL_TYPE + COMMA_SEP +
                AXIS_Z + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class LinearAccelerometerStats implements BaseColumns {
        public static final String TABLE_NAME       = "LinearAccelerometerStats";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String AXIS_NAME = "AxisName";
        public static final String START_TIMESTAMP = "StartTimestamp";
        public static final String END_TIMESTAMP = "EndTimestamp";
        public static final String ACCEL_AVERAGE = "AccelerationAverage";
        public static final String ACCEL_STD_DEVIATION = "AccelerationStandardDeviation";
        public static final String DECEL_AVERAGE = "DecelerationAverage";
        public static final String DECEL_STD_DEVIATION = "DecelerationStandardDeviation";
        public static final String ACCEL_COUNT = "AccelerationCount";
        public static final String DECEL_COUNT = "DecelerationCount";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                AXIS_NAME + TEXT_TYPE + COMMA_SEP +
                START_TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                END_TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                ACCEL_AVERAGE + REAL_TYPE + COMMA_SEP +
                ACCEL_STD_DEVIATION + REAL_TYPE + COMMA_SEP +
                ACCEL_COUNT + INTEGER_TYPE + COMMA_SEP +
                DECEL_COUNT + INTEGER_TYPE + COMMA_SEP +
                DECEL_AVERAGE + REAL_TYPE + COMMA_SEP +
                DECEL_STD_DEVIATION + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class LocationData implements BaseColumns {
        public static final String TABLE_NAME       = "LocationData";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String TIMESTAMP = "Timestamp";
        public static final String LATITUDE = "Latitude";
        public static final String LONGITUDE = "Longitude";
        public static final String ALTITUDE = "Altitude";
        public static final String BEARING = "Bearing";
        public static final String ACCURACY = "Accuracy";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                LATITUDE + REAL_TYPE + COMMA_SEP +
                LONGITUDE + REAL_TYPE + COMMA_SEP +
                ALTITUDE + REAL_TYPE + COMMA_SEP +
                BEARING + REAL_TYPE + COMMA_SEP +
                ACCURACY + INTEGER_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class SpeedData implements BaseColumns {
        public static final String TABLE_NAME       = "SpeedData";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String TIMESTAMP = "Timestamp";
        public static final String SPEED = "Speed";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                SPEED + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class SpeedStats implements BaseColumns {
        public static final String TABLE_NAME       = "SpeedStats";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String START_TIMESTAMP = "StartTimestamp";
        public static final String END_TIMESTAMP = "EndTimestamp";
        public static final String INCREASING_SPEED_AVERAGE = "IncreasingSpeedAverage";
        public static final String INCREASING_SPEED_STD_DEVIATION = "IncreasingSpeedStandardDeviation";
        public static final String DECREASING_SPEED_AVERAGE = "DecreasingSpeedAverage";
        public static final String DECREASING_SPEED_STD_DEVIATION = "DecreasingSpeedStandardDeviation";
        public static final String INCREASING_SPEED_COUNT = "IncreasingSpeedCount";
        public static final String DECREASING_SPEED_COUNT = "DecreasingSpeedCount";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                START_TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                END_TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                INCREASING_SPEED_AVERAGE + REAL_TYPE + COMMA_SEP +
                INCREASING_SPEED_STD_DEVIATION + REAL_TYPE + COMMA_SEP +
                INCREASING_SPEED_COUNT + INTEGER_TYPE + COMMA_SEP +
                DECREASING_SPEED_COUNT + INTEGER_TYPE + COMMA_SEP +
                DECREASING_SPEED_AVERAGE + REAL_TYPE + COMMA_SEP +
                DECREASING_SPEED_STD_DEVIATION + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TemperatureData implements BaseColumns {
        public static final String TABLE_NAME       = "TemperatureData";
        public static final String TIMESTAMP = "Timestamp";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String CITY = "City";
        public static final String TEMPERATURE = "Temperature";
        public static final String WIND_SPEED = "WindSpeed";
        public static final String WIND_DIRECTION = "WindDirection";
        public static final String RAIN = "Rain";
        public static final String SNOW = "Snow";
        public static final String CLOUDINESS = "Cloudiness";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                CITY + TEXT_TYPE + COMMA_SEP +
                TEMPERATURE + REAL_TYPE + COMMA_SEP +
                WIND_SPEED + REAL_TYPE + COMMA_SEP +
                WIND_DIRECTION + REAL_TYPE + COMMA_SEP +
                RAIN + REAL_TYPE + COMMA_SEP +
                SNOW + REAL_TYPE + COMMA_SEP +
                CLOUDINESS + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class RotationData implements BaseColumns {
        public static final String TABLE_NAME       = "RotationData";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String TIMESTAMP = "Timestamp";
        public static final String AXIS_X = "AxisX";
        public static final String AXIS_Y = "AxisY";
        public static final String AXIS_Z = "AxisZ";
        public static final String AZIMUTH = "Azimuth";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                TIMESTAMP + DATETIME_TYPE + " UNIQUE" + COMMA_SEP +
                AXIS_X + REAL_TYPE + COMMA_SEP +
                AXIS_Y + REAL_TYPE + COMMA_SEP +
                AXIS_Z + REAL_TYPE + COMMA_SEP +
                AZIMUTH + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TrainingEvents implements BaseColumns {
        public static final String TABLE_NAME       = "TrainingEvents";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String START_TIMESTAMP = "StartTimestamp";
        public static final String END_TIMESTAMP = "EndTimestamp";
        public static final String LABEL = "Label";
        public static final String TYPE = "Type";
        public static final String MESSAGE = "Message";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                START_TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                END_TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                TYPE + TEXT_TYPE + COMMA_SEP +
                MESSAGE + TEXT_TYPE + COMMA_SEP +
                LABEL + TEXT_TYPE  + " UNIQUE" + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}