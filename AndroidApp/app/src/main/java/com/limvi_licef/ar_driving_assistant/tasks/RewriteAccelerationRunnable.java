package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Statistics;
import com.limvi_licef.ar_driving_assistant.utils.Structs;
import com.limvi_licef.ar_driving_assistant.utils.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RewriteAccelerationRunnable implements Runnable {

    public final int DELAY = 1000 * 60 * 10;
    private static final int REWRITE_MINUTES = 10;
    private static final int TOLERANCE = 1;
    private static final String WHERE_CLAUSE = DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerData.TIMESTAMP + " BETWEEN ? AND ?";

    private String insertionStatus;
    private Handler handler;
    private SQLiteDatabase db;
    private Context context;

    public RewriteAccelerationRunnable(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        String userId = User.getCurrentUserId(context);
        long now = System.currentTimeMillis();
        long nowMinusMinutes = now - TimeUnit.MINUTES.toMillis(REWRITE_MINUTES);

        Structs.SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(getData(nowMinusMinutes, now, userId), TOLERANCE);
        List<Structs.TimestampedDouble> processedData = returnData.monotoneValues;
        Structs.ExtremaStats extremaStats = Statistics.computeExtremaStats(processedData, returnData.significantExtremaIndex);

        try{
            db.beginTransaction();
            deleteData(nowMinusMinutes, now, userId);
            saveData(processedData, extremaStats, userId);
            db.setTransactionSuccessful();
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_rewrite_success);
        }
        catch (IndexOutOfBoundsException e) {
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_rewrite_empty_data);
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_rewrite_failure) + " " + e;
        }
        finally{
            db.endTransaction();

            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            handler.postDelayed(this, DELAY);
        }
    }

    private void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats, String userId) {
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

    private List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String userId) {
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

    private void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.LinearAccelerometerData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME,
                DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerStats.START_TIMESTAMP + " >= ? AND " + DatabaseContract.LinearAccelerometerStats.END_TIMESTAMP + " <= ?",
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }
}
