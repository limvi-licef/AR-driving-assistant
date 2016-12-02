package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;

import java.util.ArrayList;
import java.util.List;

public final class Database {

    private Database(){}

    /**
     * Get all the database table names
     * @param database
     * @return an arraylist of table name strings
     */
    public static ArrayList<String> getAllTableNames(SQLiteDatabase database){
        ArrayList<String> namesArray = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while(c.moveToNext()){
            String s = c.getString(0);
            if(!s.equals("android_metadata")) namesArray.add(s);
        }
        c.close();
        return namesArray;
    }

    /**
     * Checks if the external storage is available to be written to
     * @return Whether or not the external storage is writable
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Fetches sensor data associated to given parameters
     * @param fromTimestamp The start timestamp
     * @param toTimestamp The end timestamp
     * @param tableName The table containing the wanted column
     * @param valueColumnName The column containing the wanted data
     * @param context
     * @return a List of TimestampedDouble
     */
    public static List<TimestampedDouble> getSensorData(long fromTimestamp, long toTimestamp, String tableName, String valueColumnName, Context context) {
        List<TimestampedDouble> data = new ArrayList<>();
        Cursor cursor = DatabaseHelper.getHelper(context).getReadableDatabase().query(tableName,
                new String[]{DatabaseContract.CommonSensorFields.USER_ID, DatabaseContract.CommonSensorFields.TIMESTAMP, valueColumnName},
                DatabaseContract.CommonSensorFields.USER_ID + " = ? AND " + DatabaseContract.CommonSensorFields.TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{Preferences.getCurrentUserId(context), String.valueOf(fromTimestamp), String.valueOf(toTimestamp)}, null, null, "Timestamp ASC");
        int timestampColumnIndex = cursor.getColumnIndexOrThrow(DatabaseContract.LinearAccelerometerData.TIMESTAMP);
        int valueColumnIndex = cursor.getColumnIndexOrThrow(valueColumnName);
        while (cursor.moveToNext()) {
            data.add(new TimestampedDouble(cursor.getLong(timestampColumnIndex), cursor.getDouble(valueColumnIndex)));
        }
        cursor.close();
        return data;
    }
}
