package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.models.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.utils.Database;

import java.util.List;

public class RewriteAzimuthRunnable extends RewriteAlgorithmRunnable {

    private static final String WHERE_CLAUSE = DatabaseContract.RotationData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.RotationData.TIMESTAMP + " BETWEEN ? AND ?";

    public RewriteAzimuthRunnable(Handler handler, Context context) {
       super(handler, context);
    }

    @Override
    protected void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats, String userId, String column) {
        for(TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.RotationData.TIMESTAMP, td.timestamp);
            values.put(column, td.value);
            db.insert(DatabaseContract.RotationData.TABLE_NAME, null, values);
        }
    }

    @Override
    protected List<TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String column) {
        return Database.getSensorData(fromTimestamp, toTimestamp, DatabaseContract.RotationData.TABLE_NAME, column, context);
    }

    @Override
    protected void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.RotationData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.RotationData.TABLE_NAME;
    }

    @Override
    protected String[] getColumns() {
        return new String[]{DatabaseContract.RotationData.AZIMUTH};
    }
}
