package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Database;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.ArrayList;
import java.util.List;

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

        boolean matchFound = false;
        String labelFound = "";
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();

        TimeSeries accel;
        TimeSeries tsRotation;
        try {
            accel = createTimeSeriesFromSensor(startTimestamp, endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                    DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
            tsRotation = createTimeSeriesFromSensor(startTimestamp, endTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
        } catch(IndexOutOfBoundsException e) {
            return "No data from sensor found";
        }

        //get training events in db
        Cursor eventCursor = db.query(DatabaseContract.TrainingEvents.TABLE_NAME,
                new String[]{DatabaseContract.TrainingEvents.START_TIMESTAMP, DatabaseContract.TrainingEvents.END_TIMESTAMP, DatabaseContract.TrainingEvents.LABEL},
                DatabaseContract.TrainingEvents.CURRENT_USER_ID + " = ?",
                new String[]{Preferences.getCurrentUserId(context)}, null, null, null);
        int startTimestampColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.START_TIMESTAMP);
        int endTimestampColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.END_TIMESTAMP);
        int labelColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.LABEL);

        //for each event, try to find a match
        while (eventCursor.moveToNext()) {
            long eventStartTimestamp = eventCursor.getLong(startTimestampColumnIndex);
            long eventEndTimestamp = eventCursor.getLong(endTimestampColumnIndex);
            String eventLabel = eventCursor.getString(labelColumnIndex);

            TimeSeries eventAccel = createTimeSeriesFromSensor(eventStartTimestamp, eventEndTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                    DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
            TimeSeries eventRotation = createTimeSeriesFromSensor(eventStartTimestamp, eventEndTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);

            double distanceAccel = FastDTW.compare(accel, eventAccel, 10, Distances.EUCLIDEAN_DISTANCE).getDistance();
            double distanceRotation = FastDTW.compare(tsRotation, eventRotation, 10, Distances.EUCLIDEAN_DISTANCE).getDistance();
            Broadcasts.sendWriteToUIBroadcast(context, "Distance Label : " + eventLabel);
            Broadcasts.sendWriteToUIBroadcast(context, "Distance Accel : " + distanceAccel);
            Broadcasts.sendWriteToUIBroadcast(context, "Distance Rotation : " + distanceRotation);
            if(distanceAccel < 15 && distanceRotation < 200){
                matchFound = true;
                labelFound = eventLabel;
                break;
            }
        }
        eventCursor.close();

        if(matchFound) {
            return "Match Found : " + labelFound;
        } else {
            return saveNewEvent(db);
        }
    }

    @Override
    protected void onPostExecute (String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    private String saveNewEvent(SQLiteDatabase db) {
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
        return result == -1 ? "Label already exists" : "No Match Found, inserting event to database";
    }

    private TimeSeries createTimeSeriesFromSensor(long startTimestamp, long endTimestamp, String tableName, String... valueColumnNames) {
        List<List<Structs.TimestampedDouble>> valuesList = new ArrayList<>();
        for(String column : valueColumnNames) {
            valuesList.add(Database.getSensorData(startTimestamp, endTimestamp, tableName, column, context));
        }

        TimeSeriesBase.Builder b = TimeSeriesBase.builder();
        for(int i = 0; i < valuesList.get(0).size(); i++) {
            double [] values = new double[valueColumnNames.length];
            for(int j = 0; j < valueColumnNames.length; j++){
                values[j] = valuesList.get(j).get(i).value;
            }
            b.add(valuesList.get(0).get(i).timestamp, values);
        }
        return b.build();
    }
}