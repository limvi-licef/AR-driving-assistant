package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Events;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

public class TrainingTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private Events.Event event;

    public TrainingTask(Events.Event event, Context context) {
        this.event = event;
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
            Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                    DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
            Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
            Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.SpeedData.TABLE_NAME, DatabaseContract.SpeedData.SPEED);
        } catch(IndexOutOfBoundsException e) {
            return context.getResources().getString(R.string.training_task_no_data);
        }

        return saveNewEvent(event);
    }

    @Override
    protected void onPostExecute (String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    private String saveNewEvent(Events.Event event) {
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        db.beginTransaction();
        String userId = Preferences.getCurrentUserId(context);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TrainingEvents.CURRENT_USER_ID, userId);
        values.put(DatabaseContract.TrainingEvents.START_TIMESTAMP, event.startTimestamp);
        values.put(DatabaseContract.TrainingEvents.END_TIMESTAMP, event.endTimestamp);
        values.put(DatabaseContract.TrainingEvents.LABEL, event.label);
        values.put(DatabaseContract.TrainingEvents.TYPE, event.type.name());
        values.put(DatabaseContract.TrainingEvents.MESSAGE, event.message);
        int result = (int) db.insertWithOnConflict(DatabaseContract.TrainingEvents.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.setTransactionSuccessful();
        db.endTransaction();
        return result == -1 ? context.getResources().getString(R.string.training_task_label_exists) : context.getResources().getString(R.string.training_task_success);
    }
}
