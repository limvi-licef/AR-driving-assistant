package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Events;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

public class TrainingTask extends AsyncTask<Void, Void, String> {

    private final long startTimestamp;
    private final long endTimestamp;
    private Context context;
    private String label;

    public TrainingTask(long startTimestamp, long stopTimestamp, String label, Context context) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = stopTimestamp;
        this.label = label;
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            //wait for sensor runnables to save data
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.d("TrainingTask", "" + e.getMessage());
        }

        try {
            Events.createTimeSeriesFromSensor(context, startTimestamp, endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                    DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
            Events.createTimeSeriesFromSensor(context, startTimestamp, endTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
            Events.createTimeSeriesFromSensor(context, startTimestamp, endTimestamp, DatabaseContract.SpeedData.TABLE_NAME, DatabaseContract.SpeedData.SPEED);
        } catch(IndexOutOfBoundsException e) {
            return "Error inserting event : no data from sensor found";
        }

        return saveNewEvent(startTimestamp, endTimestamp, label);
    }

    @Override
    protected void onPostExecute (String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    private String saveNewEvent(long startTimestamp, long endTimestamp, String label) {
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        db.beginTransaction();
        String userId = Preferences.getCurrentUserId(context);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TrainingEvents.CURRENT_USER_ID, userId);
        values.put(DatabaseContract.TrainingEvents.START_TIMESTAMP, startTimestamp);
        values.put(DatabaseContract.TrainingEvents.END_TIMESTAMP, endTimestamp);
        values.put(DatabaseContract.TrainingEvents.LABEL, label);
        int result = (int) db.insertWithOnConflict(DatabaseContract.TrainingEvents.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.setTransactionSuccessful();
        db.endTransaction();
        return result == -1 ? "Error inserting event : the label already exists" : "Inserting event to database";
    }
}
