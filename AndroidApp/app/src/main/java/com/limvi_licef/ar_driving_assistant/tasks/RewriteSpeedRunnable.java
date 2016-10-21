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

public class RewriteSpeedRunnable implements Runnable {

    public final int DELAY = 1000 * 60 * 10;
    private static final int REWRITE_MINUTES = 10;
    private static final int TOLERANCE = 1;
    private static final String WHERE_CLAUSE = DatabaseContract.SpeedData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.SpeedData.TIMESTAMP + " BETWEEN ? AND ?";

    private String insertionStatus;
    private Handler handler;
    private SQLiteDatabase db;
    private Context context;

    public RewriteSpeedRunnable(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        try{
            String userId = User.getCurrentUserId(context);
            long now = System.currentTimeMillis();
            long nowMinusMinutes = now - TimeUnit.MINUTES.toMillis(REWRITE_MINUTES);

            List<Structs.TimestampedDouble> rewriteData = getData(nowMinusMinutes, now, userId);
            Structs.SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(rewriteData, TOLERANCE);
            List<Integer> significantExtrema = returnData.significantExtremaIndex;
            List<Structs.TimestampedDouble> processedData = returnData.monotoneValues;
            Structs.ExtremaStats extremaStats = Statistics.computeExtremaStats(rewriteData, significantExtrema);

            db.beginTransaction();
            deleteData(nowMinusMinutes, now, userId);
            saveData(processedData, extremaStats, userId);
            db.setTransactionSuccessful();
            insertionStatus = DatabaseContract.SpeedData.TABLE_NAME + " " + context.getResources().getString(R.string.database_rewrite_success);
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.SpeedData.TABLE_NAME + " " + context.getResources().getString(R.string.database_rewrite_failure);
            Log.d("Runnable Exception", "" + e);
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

    private List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String userId) {
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

    private void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.SpeedStats.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.SpeedStats.TABLE_NAME,
                DatabaseContract.SpeedStats.CURRENT_USER_ID + " = ? AND " + DatabaseContract.SpeedStats.START_TIMESTAMP + " >= ? AND " + DatabaseContract.SpeedStats.END_TIMESTAMP + " <= ?",
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

}
