package com.aware.plugin.openweather;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

public class Provider extends ContentProvider {

    public static final int DATABASE_VERSION = 6;

    /**
     * Provider authority: com.aware.plugin.openweather.provider.openweather
     */
    public static String AUTHORITY = "com.aware.plugin.openweather.provider.openweather";

    private static final int OPENWEATHER = 1;
    private static final int OPENWEATHER_ID = 2;

    public static final String DATABASE_NAME = "plugin_openweather.db";

    public static final String[] DATABASE_TABLES = {
            "plugin_openweather"
    };

    public static final String[] TABLES_FIELDS = {
            OpenWeather_Data._ID + " integer primary key autoincrement," +
                    OpenWeather_Data.TIMESTAMP + " real default 0," +
                    OpenWeather_Data.DEVICE_ID + " text default ''," +
                    OpenWeather_Data.CITY + " text default ''," +
                    OpenWeather_Data.TEMPERATURE + " real default 0," +
                    OpenWeather_Data.TEMPERATURE_MAX + " real default 0," +
                    OpenWeather_Data.TEMPERATURE_MIN + " real default 0," +
                    OpenWeather_Data.UNITS + " text default ''," +
                    OpenWeather_Data.HUMIDITY + " real default 0," +
                    OpenWeather_Data.PRESSURE + " real default 0," +
                    OpenWeather_Data.WIND_SPEED + " real default 0," +
                    OpenWeather_Data.WIND_DEGREES + " real default 0," +
                    OpenWeather_Data.CLOUDINESS + " real default 0," +
                    OpenWeather_Data.RAIN + " real default 0," +
                    OpenWeather_Data.SNOW + " real default 0," +
                    OpenWeather_Data.SUNRISE + " real default 0," +
                    OpenWeather_Data.SUNSET + " real default 0," +
                    OpenWeather_Data.WEATHER_ICON_ID + " integer default 0," +
                    OpenWeather_Data.WEATHER_DESCRIPTION + " text default ''"
    };

    public static final class OpenWeather_Data implements BaseColumns {
        private OpenWeather_Data() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/plugin_openweather");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.openweather";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.openweather";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String CITY = "city";
        public static final String TEMPERATURE = "temperature";
        public static final String TEMPERATURE_MAX = "temperature_max";
        public static final String TEMPERATURE_MIN = "temperature_min";

        /**
         * temperature units: metric or imperial
         */
        public static final String UNITS = "unit";

        /**
         * % humidity
         */
        public static final String HUMIDITY = "humidity";

        /**
         * Atmospheric Pressure in hPa
         */
        public static final String PRESSURE = "pressure";

        /**
         * Wind speed in m/s
         */
        public static final String WIND_SPEED = "wind_speed";

        /**
         * Wind direction, in degrees
         */
        public static final String WIND_DEGREES = "wind_degrees";

        /**
         * % of clouds
         */
        public static final String CLOUDINESS = "cloudiness";

        /**
         * Amount of rain
         */
        public static final String RAIN = "rain";

        /**
         * Amount of snow
         */
        public static final String SNOW = "snow";

        /**
         * Sunrise time
         */
        public static final String SUNRISE = "sunrise";

        /**
         * Sunset time
         */
        public static final String SUNSET = "sunset";
        public static final String WEATHER_ICON_ID = "weather_icon_id";
        public static final String WEATHER_DESCRIPTION = "weather_description";
    }

    private static UriMatcher sUriMatcher;
    private static HashMap<String, String> openWeatherMap;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        }
        if (databaseHelper != null && (database == null || !database.isOpen())) {
            database = databaseHelper.getWritableDatabase();
        }
        return (database != null && databaseHelper != null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case OPENWEATHER:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case OPENWEATHER:
                return OpenWeather_Data.CONTENT_TYPE;
            case OPENWEATHER_ID:
                return OpenWeather_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case OPENWEATHER:
                long weather_id = database.insert(DATABASE_TABLES[0],
                        OpenWeather_Data.WEATHER_DESCRIPTION, values);

                if (weather_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            OpenWeather_Data.CONTENT_URI,
                            weather_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {

        AUTHORITY = getContext().getPackageName() + ".provider.openweather";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], OPENWEATHER);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", OPENWEATHER_ID);

        openWeatherMap = new HashMap<String, String>();
        openWeatherMap.put(OpenWeather_Data._ID, OpenWeather_Data._ID);
        openWeatherMap.put(OpenWeather_Data.TIMESTAMP, OpenWeather_Data.TIMESTAMP);
        openWeatherMap.put(OpenWeather_Data.DEVICE_ID, OpenWeather_Data.DEVICE_ID);
        openWeatherMap.put(OpenWeather_Data.CITY, OpenWeather_Data.CITY);
        openWeatherMap.put(OpenWeather_Data.TEMPERATURE, OpenWeather_Data.TEMPERATURE);
        openWeatherMap.put(OpenWeather_Data.TEMPERATURE_MAX, OpenWeather_Data.TEMPERATURE_MAX);
        openWeatherMap.put(OpenWeather_Data.TEMPERATURE_MIN, OpenWeather_Data.TEMPERATURE_MIN);
        openWeatherMap.put(OpenWeather_Data.UNITS, OpenWeather_Data.UNITS);
        openWeatherMap.put(OpenWeather_Data.HUMIDITY, OpenWeather_Data.HUMIDITY);
        openWeatherMap.put(OpenWeather_Data.PRESSURE, OpenWeather_Data.PRESSURE);
        openWeatherMap.put(OpenWeather_Data.WIND_SPEED, OpenWeather_Data.WIND_SPEED);
        openWeatherMap.put(OpenWeather_Data.WIND_DEGREES, OpenWeather_Data.WIND_DEGREES);
        openWeatherMap.put(OpenWeather_Data.CLOUDINESS, OpenWeather_Data.CLOUDINESS);
        openWeatherMap.put(OpenWeather_Data.RAIN, OpenWeather_Data.RAIN);
        openWeatherMap.put(OpenWeather_Data.SNOW, OpenWeather_Data.SNOW);
        openWeatherMap.put(OpenWeather_Data.SUNRISE, OpenWeather_Data.SUNRISE);
        openWeatherMap.put(OpenWeather_Data.SUNSET, OpenWeather_Data.SUNSET);
        openWeatherMap.put(OpenWeather_Data.WEATHER_ICON_ID, OpenWeather_Data.WEATHER_ICON_ID);
        openWeatherMap.put(OpenWeather_Data.WEATHER_DESCRIPTION, OpenWeather_Data.WEATHER_DESCRIPTION);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case OPENWEATHER:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(openWeatherMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case OPENWEATHER:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
