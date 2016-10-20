package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Structs.*;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.User;

import java.util.ArrayList;
import java.util.List;

public class ComputeAzimuthRunnable implements Runnable {

    public final int DELAY = 1000 * 60;
    private static final int TOLERANCE = 0;

    private String insertionStatus;
    private List<TimestampedDouble> data;
    private Handler handler;
    private SQLiteDatabase db;
    private Context context;

    public ComputeAzimuthRunnable(Handler handler, Context context) {
        this.data = new ArrayList<>();
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        try{
            List<TimestampedDouble> newData = getData();
            List<TimestampedDouble> processedData =  MonotoneSegmentationAlgorithm.computeData(newData, TOLERANCE).monotoneValues;

            db.beginTransaction();
            saveData(processedData);
            db.setTransactionSuccessful();
            clearData(newData);
            insertionStatus = DatabaseContract.RotationData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_success);
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.RotationData.TABLE_NAME + " " + context.getResources().getString(R.string.database_insert_failure);
            Log.d("Runnable Exception", "" + e);
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

    private void saveData(List<TimestampedDouble> processedData) {
        String userId = User.getCurrentUserId(context);
        for(TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.RotationData.TIMESTAMP, td.timestamp);
            values.put(DatabaseContract.RotationData.AZIMUTH, td.value);
            db.insert(DatabaseContract.RotationData.TABLE_NAME, null, values);
        }
    }

    private List<TimestampedDouble> getData() {
        return data;
    }

    private void clearData(List<TimestampedDouble> oldData){
        data.removeAll(oldData);
    }
}
