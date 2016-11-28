package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.List;

/**
 * Long-running runnable that periodically (10 mins) fetches and rewrites the previous data
 */
public abstract class RewriteAlgorithmRunnable implements Runnable {
    protected Handler handler;
    protected SQLiteDatabase db;
    protected Context context;
    protected String insertionStatus;

    public RewriteAlgorithmRunnable(Handler handler, Context context) {
        this.handler = handler;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
        this.context = context;
    }

    /**
     * Process the last Config.SensorDataCollection.LONG_DELAY minutes of data through the MonotoneSegmentation algorithm again
     * and replace the old data with the newly processed data
     */
    @Override
    public void run() {
        String userId = Preferences.getCurrentUserId(context);
        //set time period of rewrite
        long now = System.currentTimeMillis();
        long nowMinusMinutes = now - Config.SensorDataCollection.LONG_DELAY;

        //fetch existing data
        List<Structs.TimestampedDouble> newData = getData(nowMinusMinutes, now);
        Structs.SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(newData, Config.SensorDataCollection.MONOTONE_SEGMENTATION_TOLERANCE);

        try{
            db.beginTransaction();
            //deletes previous data and save new data inside a transaction in case of error
            deleteData(nowMinusMinutes, now, userId);
            saveData(returnData.monotoneValues, returnData.extremaStats, userId);
            db.setTransactionSuccessful();
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_rewrite_success);
        }
        catch (IndexOutOfBoundsException e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_rewrite_empty_data);
        }
        catch (Exception e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_rewrite_failure) + " " + e;
        }
        finally{
            db.endTransaction();

            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            handler.postDelayed(this, Config.SensorDataCollection.LONG_DELAY);
        }
    }

    /**
     * Save processed sensor data to database
     * @param processedData the processed data to save
     * @param extremaStats the associated stats to save
     * @param userId the userId associated with the data
     */
    protected abstract void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats, String userId);

    /**
     * Fetches all data found during given time period
     * @param fromTimestamp timestamp from which to fetch data
     * @param toTimestamp timestamp to which to fetch data
     * @return list of TimestampedDouble found in database
     */
    protected abstract List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp);

    /**
     * Delete data from the database associated with the userId
     * @param fromTimestamp the start timestamp from which to begin deleting
     * @param toTimestamp the end timestamp to which to end deleting
     * @param userId the userId associated with the data to delete
     */
    protected abstract void deleteData(long fromTimestamp, long toTimestamp, String userId);

    /**
     * Get the table name associated with the runnable
     * @return the table name
     */
    protected abstract String getTableName();
}
