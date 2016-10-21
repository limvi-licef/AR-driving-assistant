package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Structs.*;
import com.limvi_licef.ar_driving_assistant.utils.Statistics;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.User;

import java.util.ArrayList;
import java.util.List;

public class ComputeAccelerationRunnable implements Runnable {

    public final int DELAY = 1000 * 60;
    private static final int TOLERANCE = 1;

    private String insertionStatus;
    private List<TimestampedDouble> data;
    private Handler handler;
    private SQLiteDatabase db;
    private Context context;

    public ComputeAccelerationRunnable(Handler handler, Context context) {
        this.data = new ArrayList<>();
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        List<TimestampedDouble> newData = getData();
        SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(newData, TOLERANCE);
        List<TimestampedDouble> processedData = returnData.monotoneValues;
        ExtremaStats extremaStats = Statistics.computeExtremaStats(newData, returnData.significantExtremaIndex);

        try{
            db.beginTransaction();
            saveData(processedData, extremaStats);
            db.setTransactionSuccessful();
            clearData(newData);
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_success);
        }
        catch (IndexOutOfBoundsException e) {
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_empty_data);
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_failure) + " " + e;
        }
        finally{
            db.endTransaction();

            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            handler.postDelayed(this, DELAY);
        }
    }

    public void accumulateData(TimestampedDouble d){
        data.add(d);
    }

    private void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats) {
        String userId = User.getCurrentUserId(context);
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(TimestampedDouble td : processedData) {
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

    private List<TimestampedDouble> getData() {
        return data;
    }

    private void clearData(List<TimestampedDouble> oldData){
        data.removeAll(oldData);
    }
}
