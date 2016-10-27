package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.ArrayList;
import java.util.List;

public class RewriteSpeedRunnable extends RewriteAlgorithmRunnable {

    private static final String WHERE_CLAUSE = DatabaseContract.SpeedData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.SpeedData.TIMESTAMP + " BETWEEN ? AND ?";

    public RewriteSpeedRunnable(Handler handler, Context context) {
        super(handler, context);
    }

    @Override
    protected void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats, String userId) {
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(Structs.TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.SpeedData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.SpeedData.TIMESTAMP, td.timestamp);
            values.put(DatabaseContract.SpeedData.SPEED, td.value);
            db.insert(DatabaseContract.SpeedData.TABLE_NAME, null, values);
        }

        ContentValues stats = new ContentValues();
        stats.put(DatabaseContract.SpeedStats.CURRENT_USER_ID, userId);
        stats.put(DatabaseContract.SpeedStats.START_TIMESTAMP, firstTimestamp);
        stats.put(DatabaseContract.SpeedStats.END_TIMESTAMP, lastTimestamp);
        stats.put(DatabaseContract.SpeedStats.INCREASING_SPEED_AVERAGE, extremaStats.positiveAverage);
        stats.put(DatabaseContract.SpeedStats.INCREASING_SPEED_STD_DEVIATION, extremaStats.positiveStandardDeviation);
        stats.put(DatabaseContract.SpeedStats.INCREASING_SPEED_COUNT, extremaStats.positiveCount);
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_COUNT, extremaStats.negativeCount);
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_AVERAGE, extremaStats.negativeAverage);
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_STD_DEVIATION, extremaStats.negativeStandardDeviation);
        db.insert(DatabaseContract.SpeedStats.TABLE_NAME, null, stats);
    }

    @Override
    protected List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String userId) {
        List<Structs.TimestampedDouble> data = new ArrayList<>();
        Cursor speedCursor = db.query(DatabaseContract.SpeedData.TABLE_NAME,
                new String[]{DatabaseContract.SpeedData.CURRENT_USER_ID, DatabaseContract.SpeedData.TIMESTAMP,DatabaseContract.SpeedData.SPEED},
                WHERE_CLAUSE,
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)}, null, null, "Timestamp ASC");
        int timestampColumnIndex = speedCursor.getColumnIndexOrThrow(DatabaseContract.SpeedData.TIMESTAMP);
        int speedColumnIndex = speedCursor.getColumnIndexOrThrow(DatabaseContract.SpeedData.SPEED);
        while (speedCursor.moveToNext()) {
            data.add(new Structs.TimestampedDouble(speedCursor.getLong(timestampColumnIndex), speedCursor.getDouble(speedColumnIndex)));
        }
        speedCursor.close();
        return data;
    }

    @Override
    protected void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.SpeedData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.SpeedStats.TABLE_NAME,
                DatabaseContract.SpeedStats.CURRENT_USER_ID + " = ? AND " + DatabaseContract.SpeedStats.START_TIMESTAMP + " >= ? AND " + DatabaseContract.SpeedStats.END_TIMESTAMP + " <= ?",
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.SpeedData.TABLE_NAME;
    }
}
