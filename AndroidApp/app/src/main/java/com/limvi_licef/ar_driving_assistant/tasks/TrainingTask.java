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
    protected void onPreExecute() {
        try {
            //wait for sensor runnables to save data
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.d("TrainingTask", "" + e.getMessage());
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        boolean matchFound = false;
        String labelFound = "";
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();

//        TimeSeries tsAxisX = createTimeSeriesFromSensor(DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_X, startTimestamp, endTimestamp);
//        TimeSeries tsAxisY = createTimeSeriesFromSensor(DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_Y, startTimestamp, endTimestamp);

        List<Structs.TimestampedDouble> valuesX = Database.getSensorData(startTimestamp, endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_X, context);
        List<Structs.TimestampedDouble> valuesY = Database.getSensorData(startTimestamp, endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_Y, context);
//        List<Structs.TimestampedDouble> valuesZ = Database.getSensorData(startTimestamp, endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_Z, context);

        if(valuesX.size() == 0) {
            return "No acceleration data found";
        }

        TimeSeriesBase.Builder b = TimeSeriesBase.builder();
        for(int i = 0; i < valuesX.size() ;i++) {
            b.add(i, valuesX.get(i).value, valuesY.get(i).value);
        }
        TimeSeries accel = b.build();

        TimeSeries tsRotation = createTimeSeriesFromSensor(DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH, startTimestamp, endTimestamp);

        Cursor eventCursor = db.query(DatabaseContract.TrainingEvents.TABLE_NAME,
                new String[]{DatabaseContract.TrainingEvents.START_TIMESTAMP, DatabaseContract.TrainingEvents.END_TIMESTAMP, DatabaseContract.TrainingEvents.LABEL},
                DatabaseContract.TrainingEvents.CURRENT_USER_ID + " = ?",
                new String[]{Preferences.getCurrentUserId(context)}, null, null, null);
        int startTimestampColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.START_TIMESTAMP);
        int endTimestampColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.END_TIMESTAMP);
        int labelColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.LABEL);
        while (eventCursor.moveToNext()) {
            long eventStartTimestamp = eventCursor.getLong(startTimestampColumnIndex);
            long eventEndTimestamp = eventCursor.getLong(endTimestampColumnIndex);
            String eventLabel = eventCursor.getString(labelColumnIndex);

//            TimeSeries eventAxisX = createTimeSeriesFromSensor(DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_X, eventStartTimestamp, eventEndTimestamp);
//            TimeSeries eventAxisY = createTimeSeriesFromSensor(DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_Y, eventStartTimestamp, eventEndTimestamp);

            List<Structs.TimestampedDouble> eventX = Database.getSensorData(eventStartTimestamp, eventEndTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_X, context);
            List<Structs.TimestampedDouble> eventY = Database.getSensorData(eventStartTimestamp, eventEndTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_Y, context);
//            List<Structs.TimestampedDouble> eventZ = Database.getSensorData(eventStartTimestamp, eventEndTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, DatabaseContract.LinearAccelerometerData.AXIS_Z, context);
            TimeSeriesBase.Builder c = TimeSeriesBase.builder();
            for(int i = 0; i < eventX.size() ;i++) {
                c.add(i, eventX.get(i).value, eventY.get(i).value);
            }
            TimeSeries eventAccel = c.build();

            TimeSeries eventRotation = createTimeSeriesFromSensor(DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH, eventStartTimestamp, eventEndTimestamp);

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

    private TimeSeries createTimeSeriesFromSensor(String tableName, String valueColumnName, long startTimestamp, long endTimestamp) {
        List<Structs.TimestampedDouble> values = Database.getSensorData(startTimestamp, endTimestamp, tableName, valueColumnName, context);
        TimeSeriesBase.Builder b = TimeSeriesBase.builder();
        for(Structs.TimestampedDouble d : values) {
            b.add(d.timestamp, d.value);
        }
        return b.build();
    }
}
