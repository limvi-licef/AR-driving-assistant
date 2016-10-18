package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.Utils;
import com.limvi_licef.ar_driving_assistant.Utils.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ComputeAccelerationRunnable implements Runnable {

    private static final int DELAY = 1000 * 60;
    private static int TOLERANCE = 0;

    private String insertionStatus;
    private List<TimestampedDouble> data = new ArrayList<>();
    private static Handler handler;
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
            List<TimestampedDouble> processedData = MonotoneSegmentationAlgorithm.ComputeData(data, TOLERANCE);
            String userId = Utils.getCurrentUserId(context);

            db.beginTransaction();
            for(TimestampedDouble td : processedData) {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID, userId);
                values.put(DatabaseContract.LinearAccelerometerData.TIMESTAMP, td.timestamp);
                values.put(DatabaseContract.LinearAccelerometerData.ACCEL, td.value);
                db.insert(DatabaseContract.LinearAccelerometerData.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            clearData();
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + R.string.database_insert_success;
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.LinearAccelerometerData.TABLE_NAME + " " + R.string.database_insert_failure;
            Log.d("Runnable Exception", "" + e);
        }
        finally{
            db.endTransaction();

            Intent localIntent = new Intent(Settings.ACTION_INSERT_DONE).putExtra(Settings.INSERT_STATUS, insertionStatus);
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
            handler.postDelayed(this, DELAY);
        }
    }

    public void accumulateData(TimestampedDouble d){
        data.add(d);
    }

    public void clearData(){
        data.clear();
    }

    public void startRunnable(){
        handler.postDelayed(this, DELAY);
    }

    public void stopRunnable(){
        handler.removeCallbacksAndMessages(null);
    }
}
