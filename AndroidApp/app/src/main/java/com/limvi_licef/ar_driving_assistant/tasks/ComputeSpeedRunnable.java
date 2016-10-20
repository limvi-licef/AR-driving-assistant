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

public class ComputeSpeedRunnable implements ComputeAlgorithmRunnable {
    private static final int DELAY = 1000 * 60;
    private static final int TOLERANCE = 0;

    private String insertionStatus;
    private List<TimestampedDouble> data = new ArrayList<>();
    private Handler handler;
    private SQLiteDatabase db;
    private Context context;

    public ComputeSpeedRunnable(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        try{
            SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(data, TOLERANCE);
            List<Integer> significantExtrema = returnData.significantExtremaIndex;

            ExtremaStats extremaStats = Statistics.computeExtremaStats(data, significantExtrema);

            List<TimestampedDouble> processedData = returnData.monotoneValues;
            long firstTimestamp = processedData.get(0).timestamp;
            long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

            String userId = User.getCurrentUserId(context);

            db.beginTransaction();
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

            db.setTransactionSuccessful();
            clearData();
            insertionStatus = DatabaseContract.SpeedData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_success);
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.SpeedData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_failure);
            Log.d("Runnable Exception", "" + e);
        }
        finally{
            db.endTransaction();

            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            handler.postDelayed(this, DELAY);
        }
    }

    @Override
    public void accumulateData(TimestampedDouble d){
        data.add(d);
    }

    @Override
    public void clearData(){
        data.clear();
    }

    @Override
    public void startRunnable(){
        handler.postDelayed(this, DELAY);
    }

    @Override
    public void stopRunnable(){
        handler.removeCallbacksAndMessages(null);
    }
}
