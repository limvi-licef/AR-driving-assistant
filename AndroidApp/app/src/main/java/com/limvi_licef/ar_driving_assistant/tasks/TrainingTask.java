package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fastdtw.timeseries.TimeSeries;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.TimeSeriesExtended;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.models.Event;
import com.limvi_licef.ar_driving_assistant.models.sensors.AccelerationSensor;
import com.limvi_licef.ar_driving_assistant.models.sensors.RotationSensor;
import com.limvi_licef.ar_driving_assistant.models.sensors.SensorType;
import com.limvi_licef.ar_driving_assistant.models.sensors.SpeedSensor;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

public class TrainingTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private Event event;

    public TrainingTask(Event event, Context context) {
        this.event = event;
        this.context = context;
    }

    /**
     * Verifies there is sensor data associated with the event before inserting it
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(Void... params) {
        return saveNewEvent(event);
    }

    /**
     * Show the result as a toast in case of error
     * @param result the result of the insert
     */
    @Override
    protected void onPostExecute (String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    /**
     * Save a new event to the database
     * @param event the event to save
     * @return the status of the insert
     */
    private String saveNewEvent(Event event) {
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        db.beginTransaction();
        String userId = Preferences.getCurrentUserId(context);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TrainingEvents.CURRENT_USER_ID, userId);
        values.put(DatabaseContract.TrainingEvents.START_TIMESTAMP, event.startTimestamp);
        values.put(DatabaseContract.TrainingEvents.END_TIMESTAMP, event.endTimestamp);
        values.put(DatabaseContract.TrainingEvents.DURATION, event.duration);
        values.put(DatabaseContract.TrainingEvents.LABEL, event.label);
        values.put(DatabaseContract.TrainingEvents.TYPE, event.type.name());
        values.put(DatabaseContract.TrainingEvents.MESSAGE, event.message);
        int result = (int) db.insertWithOnConflict(DatabaseContract.TrainingEvents.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.setTransactionSuccessful();
        db.endTransaction();
        return result == -1 ? context.getResources().getString(R.string.training_task_label_exists) : context.getResources().getString(R.string.training_task_success);
    }
}
