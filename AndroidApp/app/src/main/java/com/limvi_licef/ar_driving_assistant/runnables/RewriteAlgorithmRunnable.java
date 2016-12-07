package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.config.SensorDataCollection;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.models.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.models.SegmentationAlgorithmReturnData;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Process the last SensorDataCollection.LONG_DELAY minutes of data through the MonotoneSegmentation algorithm again
     * and replace the old data with the newly processed data
     */
    @Override
    public void run() {
        String userId = Preferences.getCurrentUserId(context);
        //set time period of rewrite
        long now = System.currentTimeMillis();
        long nowMinusMinutes = now - SensorDataCollection.LONG_DELAY;

        Map<String,SegmentationAlgorithmReturnData> dataList = new HashMap<>();
        for(String column : getColumns()) {
            List<TimestampedDouble> newData = getData(nowMinusMinutes, now, column);
            dataList.put(column, MonotoneSegmentationAlgorithm.computeData(newData, SensorDataCollection.MONOTONE_SEGMENTATION_TOLERANCE));
        }

        try{
            db.beginTransaction();

            //deletes previous data and save new data inside a transaction in case of error
            deleteData(nowMinusMinutes, now, userId);

            //save each column data
            for(Map.Entry<String, SegmentationAlgorithmReturnData> entry : dataList.entrySet()){
                saveData(entry.getValue().monotoneValues, entry.getValue().extremaStats, userId, entry.getKey());
            }

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
            handler.postDelayed(this, SensorDataCollection.LONG_DELAY);
        }
    }

    /**
     * Save processed sensor data to database
     * @param processedData the processed data to save
     * @param extremaStats the associated stats to save
     * @param userId the userId associated with the data
     * @param column the table column to save data to
     */
    protected abstract void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats, String userId, String column);

    /**
     * Fetches all data found during given time period
     * @param fromTimestamp timestamp from which to fetch data
     * @param toTimestamp timestamp to which to fetch data
     * @param column the table column to get data from
     * @return list of TimestampedDouble found in database
     */
    protected abstract List<TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String column);

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

    /**
     * Get the table columns associated with the runnable
     * @return the table columns to be processed
     */
    protected abstract String[] getColumns();
}
