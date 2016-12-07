package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.models.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.utils.Database;

import java.util.List;

public class RewriteAccelerationRunnable extends RewriteAlgorithmRunnable {

    private static final String WHERE_CLAUSE = DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerData.TIMESTAMP + " BETWEEN ? AND ?";
    private static final String WHERE_CLAUSE_STATS = DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerStats.AXIS_NAME + " = ? AND " + DatabaseContract.LinearAccelerometerStats.START_TIMESTAMP + " >= ? AND " + DatabaseContract.LinearAccelerometerStats.END_TIMESTAMP + " <= ?";

    public RewriteAccelerationRunnable(Handler handler, Context context) {
        super(handler, context);
    }

    @Override
    protected void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats, String userId, String column) {
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.LinearAccelerometerData.TIMESTAMP, td.timestamp);
            values.put(column, td.value);

            //Create new row if none exist for this timestamp
            int id = (int) db.insertWithOnConflict(DatabaseContract.LinearAccelerometerData.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                //if row exists, add axis data to correct column
                db.update(DatabaseContract.LinearAccelerometerData.TABLE_NAME, values, "Timestamp=?", new String[] {String.valueOf(td.timestamp)});
            }
        }

        ContentValues stats = new ContentValues();
        stats.put(DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID, userId);
        stats.put(DatabaseContract.LinearAccelerometerStats.AXIS_NAME, column);
        stats.put(DatabaseContract.LinearAccelerometerStats.START_TIMESTAMP, firstTimestamp);
        stats.put(DatabaseContract.LinearAccelerometerStats.END_TIMESTAMP, lastTimestamp);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_AVERAGE, extremaStats.positiveAverage);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_STD_DEVIATION, extremaStats.positiveStandardDeviation);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_COUNT, extremaStats.positiveCount);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_COUNT, extremaStats.negativeCount);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_AVERAGE, extremaStats.negativeAverage);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_STD_DEVIATION, extremaStats.negativeStandardDeviation);
        db.insert(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, null, stats);
    }

    @Override
    protected List<TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String column) {
        return Database.getSensorData(fromTimestamp, toTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, column, context);
    }

    @Override
    protected void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.LinearAccelerometerData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, WHERE_CLAUSE_STATS, new String[]{userId, DatabaseContract.LinearAccelerometerData.AXIS_X, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, WHERE_CLAUSE_STATS, new String[]{userId, DatabaseContract.LinearAccelerometerData.AXIS_Y, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, WHERE_CLAUSE_STATS, new String[]{userId, DatabaseContract.LinearAccelerometerData.AXIS_Z, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.LinearAccelerometerData.TABLE_NAME;
    }

    @Override
    protected String[] getColumns() {
        return new String[]{DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z};
    }

}
