package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.tasks.MatchEventTask;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.ArrayList;
import java.util.List;

/**
 * Short-running runnable that periodically (1 min) sends all accumulated data to MonotoneSegmentationAlgorithm before saving the data
 */
public abstract class ComputeAlgorithmRunnable implements Runnable {

    protected List<Structs.TimestampedDouble> data;
    protected Handler handler;
    protected SQLiteDatabase db;
    protected Context context;
    private String insertionStatus;
    private boolean isRunning = false;

    private static int runnableCount = 0;
    private static int runnableDoneCount = 0;

    /**
     * Add to the registered runnables count
     */
    private void addToCount(){
        runnableCount++;
    }

    /**
     * Notify that a runnable has finished running
     * Once all registered runnables have ended, run MatchEventTask
     */
    private void notifyRunnableEnd() {
        runnableDoneCount++;

        //launch task once all runnables are finished running
        if(runnableCount == runnableDoneCount){
            runnableDoneCount = 0;
//            new MatchEventTask(context, Config.SensorDataCollection.SHORT_DELAY).execute();
            Broadcasts.sendStartDTWBroadcast(context);
        }
    }

    ComputeAlgorithmRunnable(Handler handler, Context context) {
        this.data = new ArrayList<>();
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
        addToCount();
    }

    /**
     * Process the accumulated data through the MonotoneSegmentation algorithm and save the processed data
     */
    @Override
    public void run() {
        isRunning = true;

        //send accumulated data through algorithm
        List<Structs.TimestampedDouble> newData = getData();
        Structs.SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(newData, Config.SensorDataCollection.MONOTONE_SEGMENTATION_TOLERANCE);

        try{
            db.beginTransaction();
            //save algorithm returned data
            saveData(returnData.monotoneValues,  returnData.extremaStats);
            db.setTransactionSuccessful();
            resetData();
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_insert_success);
        }
        catch (IndexOutOfBoundsException e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_insert_empty_data);
        }
        catch (Exception e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_insert_failure) + " " + e;
        }
        finally{
            db.endTransaction();

//            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            isRunning = false;
            handler.postDelayed(this, Config.SensorDataCollection.SHORT_DELAY);
            notifyRunnableEnd();
        }
    }

    /**
     * Add a TimestampedDouble to the data list
     * @param d the data to add
     */
    public void accumulateData(Structs.TimestampedDouble d){
        data.add(d);
    }

    /**
     * Checks if the runnable is currently running
     * @return the runnable status
     */
    public boolean isRunning(){
        return isRunning;
    }

    /**
     * Clear the accumulated data
     */
    private void resetData(){
        data.clear();
    }

    /**
     * Get the data accumulated by the runnable
     * @return the data ArrayList
     */
    private List<Structs.TimestampedDouble> getData() {
        return new ArrayList<>(data);
    }

    /**
     * Save the processed data to the database
     * @param processedData the processed data
     * @param extremaStats the associated stats
     */
    protected abstract void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats);

    /**
     * The table associated with the runnable
     * @return the table name
     */
    protected abstract String getTableName();

}
