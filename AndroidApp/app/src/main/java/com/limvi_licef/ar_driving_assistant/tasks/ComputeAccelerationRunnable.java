package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Utils;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.database.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;

public class ComputeAccelerationRunnable implements ComputeAlgorithmRunnable {

    private static final int DELAY = 1000 * 60;
    private static final int TOLERANCE = 0;

    private String insertionStatus;
    private List<Utils.TimestampedDouble> data = new ArrayList<>();
    private Handler handler;
    private SQLiteDatabase db;
    private Context context;

    public ComputeAccelerationRunnable(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        try{
            Utils.SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(data, TOLERANCE);
            List<Integer> significantExtrema = returnData.significantExtremaIndex;
            List<Utils.TimestampedDouble> processedData = returnData.monotoneValues;

            String userId = Utils.getCurrentUserId(context);

            db.beginTransaction();
            for(Utils.TimestampedDouble td : processedData) {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID, userId);
                values.put(DatabaseContract.LinearAccelerometerData.TIMESTAMP, td.timestamp);
                values.put(DatabaseContract.LinearAccelerometerData.ACCEL, td.value);
                db.insert(DatabaseContract.LinearAccelerometerData.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            clearData();
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_success);
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_failure);
            Log.d("Runnable Exception", "" + e);
        }
        finally{
            db.endTransaction();

            DatabaseUtils.sendInsertStatusBroadcast(context, insertionStatus);
            handler.postDelayed(this, DELAY);
        }
    }

    @Override
    public void accumulateData(Utils.TimestampedDouble d){
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
