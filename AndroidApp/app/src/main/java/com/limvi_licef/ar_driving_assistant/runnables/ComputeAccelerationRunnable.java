package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

import java.util.List;

public class ComputeAccelerationRunnable extends ComputeAlgorithmRunnable {

    private final String axisColumnName;

    public ComputeAccelerationRunnable(Handler handler, Context context, String axisColumnName) {
        super(handler, context);
        this.axisColumnName = axisColumnName;
    }

    @Override
    protected void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats) {
        String userId = Preferences.getCurrentUserId(context);
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.LinearAccelerometerData.TIMESTAMP, td.timestamp);
            values.put(axisColumnName, td.value);

            int id = (int) db.insertWithOnConflict(DatabaseContract.LinearAccelerometerData.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                db.update(DatabaseContract.LinearAccelerometerData.TABLE_NAME, values, "Timestamp=?", new String[] {String.valueOf(td.timestamp)});
            }
        }

        ContentValues stats = new ContentValues();
        stats.put(DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID, userId);
        stats.put(DatabaseContract.LinearAccelerometerStats.AXIS_NAME, axisColumnName);
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
    protected String getTableName(){
        return DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + axisColumnName;
    }
}
