package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.utils.Structs.*;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.User;

import java.util.List;

public class ComputeSpeedRunnable extends ComputeAlgorithmRunnable {

    public ComputeSpeedRunnable(Handler handler, Context context) {
        super(handler, context);
    }

    @Override
    protected void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats) {
        String userId = User.getCurrentUserId(context);
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(TimestampedDouble td : processedData) {
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
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_AVERAGE, extremaStats.negativeAverage);
        stats.put(DatabaseContract.SpeedStats.DECREASING_SPEED_STD_DEVIATION, extremaStats.negativeStandardDeviation);
        db.insert(DatabaseContract.SpeedStats.TABLE_NAME, null, stats);
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.SpeedData.TABLE_NAME;
    }

}
