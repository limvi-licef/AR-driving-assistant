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
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;


public class ComputeSpeedRunnable implements Runnable {
    private static final int DELAY = 1000 * 60;
    private static int TOLERANCE = 0;

    private String insertionStatus;
    private List<Utils.TimestampedDouble> data = new ArrayList<>();
    private static Handler handler;
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
            List<Utils.TimestampedDouble> processedData = MonotoneSegmentationAlgorithm.ComputeData(data, TOLERANCE);
            String userId = Utils.getCurrentUserId(context);

            db.beginTransaction();
            for(Utils.TimestampedDouble td : processedData) {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.SpeedData.CURRENT_USER_ID, userId);
                values.put(DatabaseContract.SpeedData.TIMESTAMP, td.timestamp);
                values.put(DatabaseContract.SpeedData.SPEED, td.value);
                db.insert(DatabaseContract.SpeedData.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            clearData();
            insertionStatus = DatabaseContract.SpeedData.TABLE_NAME + " " + R.string.database_insert_success;
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.SpeedData.TABLE_NAME + " " + R.string.database_insert_failure;
            Log.d("Runnable Exception", "" + e);
        }
        finally{
            db.endTransaction();

            Intent localIntent = new Intent(Settings.ACTION_INSERT_DONE).putExtra(Settings.INSERT_STATUS, insertionStatus);
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
            handler.postDelayed(this, DELAY);
        }
    }

    public void accumulateData(Utils.TimestampedDouble d){
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
