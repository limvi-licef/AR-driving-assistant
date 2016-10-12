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
            AccelerometerData.CREATE_TABLE,
            LinearAccelerometerData.CREATE_TABLE,
            GyroscopeData.CREATE_TABLE,
            LocationData.CREATE_TABLE,
            TemperatureData.CREATE_TABLE,
            RotationData.CREATE_TABLE
    };

    public static final String[] SQL_DELETE_TABLE_ARRAY = {
            AccelerometerData.DELETE_TABLE,
            LinearAccelerometerData.DELETE_TABLE,
            GyroscopeData.DELETE_TABLE,
            LocationData.DELETE_TABLE,
            TemperatureData.DELETE_TABLE,
            RotationData.DELETE_TABLE
    };

    private DatabaseContract() {}

    public static abstract class AccelerometerData implements BaseColumns {
        public static final String TABLE_NAME       = "AccelerometerData";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String TIMESTAMP = "Timestamp";
        public static final String AXIS_X = "AxisX";
        public static final String AXIS_Y = "AxisY";
        public static final String AXIS_Z = "AxisZ";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                AXIS_X + REAL_TYPE + COMMA_SEP +
                AXIS_Y + REAL_TYPE + COMMA_SEP +
                AXIS_Z + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
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
                TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                AXIS_X + REAL_TYPE + COMMA_SEP +
                AXIS_Y + REAL_TYPE + COMMA_SEP +
                AXIS_Z + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class GyroscopeData implements BaseColumns {
        public static final String TABLE_NAME       = "GyroscopeData";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String TIMESTAMP = "Timestamp";
        public static final String AXIS_X = "AxisX";
        public static final String AXIS_Y = "AxisY";
        public static final String AXIS_Z = "AxisZ";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                CURRENT_USER_ID + TEXT_TYPE + COMMA_SEP +
                TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                AXIS_X + REAL_TYPE + COMMA_SEP +
                AXIS_Y + REAL_TYPE + COMMA_SEP +
                AXIS_Z + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class LocationData implements BaseColumns {
        public static final String TABLE_NAME       = "LocationData";
        public static final String CURRENT_USER_ID = "CurrentUserID";
        public static final String TIMESTAMP = "Timestamp";
        public static final String LATITUDE = "Latitude";
        public static final String LONGITUDE = "Longitude";
        public static final String ALTITUDE = "Altitude";
        public static final String SPEED = "Speed";
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
                SPEED + REAL_TYPE + COMMA_SEP +
                BEARING + REAL_TYPE + COMMA_SEP +
                ACCURACY + INTEGER_TYPE + " )";
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
                TIMESTAMP + DATETIME_TYPE + COMMA_SEP +
                AXIS_X + REAL_TYPE + COMMA_SEP +
                AXIS_Y + REAL_TYPE + COMMA_SEP +
                AXIS_Z + REAL_TYPE + COMMA_SEP +
                AZIMUTH + REAL_TYPE + " )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}