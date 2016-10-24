package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.ArrayList;
import java.util.List;

public class RewriteAccelerationRunnable extends RewriteAlgorithmRunnable {

    private static final String WHERE_CLAUSE = DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerData.TIMESTAMP + " BETWEEN ? AND ?";

    public RewriteAccelerationRunnable(Handler handler, Context context) {
        super(handler, context);
    }

    @Override
    protected void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats, String userId) {
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(Structs.TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.LinearAccelerometerData.TIMESTAMP, td.timestamp);
            values.put(DatabaseContract.LinearAccelerometerData.ACCEL, td.value);
            db.insert(DatabaseContract.LinearAccelerometerData.TABLE_NAME, null, values);
        }

        ContentValues stats = new ContentValues();
        stats.put(DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID, userId);
        stats.put(DatabaseContract.LinearAccelerometerStats.START_TIMESTAMP, firstTimestamp);
        stats.put(DatabaseContract.LinearAccelerometerStats.END_TIMESTAMP, lastTimestamp);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_AVERAGE, extremaStats.positiveAverage);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_STD_DEVIATION, extremaStats.positiveStandardDeviation);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_AVERAGE, extremaStats.negativeAverage);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_STD_DEVIATION, extremaStats.negativeStandardDeviation);
        db.insert(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, null, stats);
    }

    @Override
    protected List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String userId) {
        List<Structs.TimestampedDouble> data = new ArrayList<>();
        Cursor accelerationCursor = db.query(DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                new String[]{DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID, DatabaseContract.LinearAccelerometerData.TIMESTAMP,DatabaseContract.LinearAccelerometerData.ACCEL},
                WHERE_CLAUSE,
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)}, null, null, "Timestamp ASC");
        int timestampColumnIndex = accelerationCursor.getColumnIndexOrThrow(DatabaseContract.LinearAccelerometerData.TIMESTAMP);
        int accelColumnIndex = accelerationCursor.getColumnIndexOrThrow(DatabaseContract.LinearAccelerometerData.ACCEL);
        while (accelerationCursor.moveToNext()) {
            data.add(new Structs.TimestampedDouble(accelerationCursor.getLong(timestampColumnIndex), accelerationCursor.getDouble(accelColumnIndex)));
        }
        accelerationCursor.close();
        return data;
    }

    @Override
    protected void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.LinearAccelerometerData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME,
                DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerStats.START_TIMESTAMP + " >= ? AND " + DatabaseContract.LinearAccelerometerStats.END_TIMESTAMP + " <= ?",
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.LinearAccelerometerData.TABLE_NAME;
    }
}
