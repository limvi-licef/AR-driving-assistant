package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Events;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MatchEventTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private final long startTimestamp;
    private final long endTimestamp;

    public MatchEventTask(Context context, int seconds) {
        this.context = context;
        this.startTimestamp = System.currentTimeMillis();
        this.endTimestamp = startTimestamp - TimeUnit.SECONDS.toMillis(seconds);
    }

    @Override
    protected Void doInBackground(Void... params) {

        boolean matchFound = false;
        String labelFound = "";
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();

        TimeSeries accel;
        TimeSeries tsRotation;
        try {
            accel = Events.createTimeSeriesFromSensor(context, startTimestamp, endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                    DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
            tsRotation = Events.createTimeSeriesFromSensor(context, startTimestamp, endTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
        } catch(IndexOutOfBoundsException e) {
            return null;
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

            TimeSeries eventAccel = Events.createTimeSeriesFromSensor(context, eventStartTimestamp, eventEndTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                    DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
            TimeSeries eventRotation = Events.createTimeSeriesFromSensor(context, eventStartTimestamp, eventEndTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);

            double distanceAccel = FastDTW.compare(accel, eventAccel, 10, Distances.EUCLIDEAN_DISTANCE).getDistance();
            double distanceRotation = FastDTW.compare(tsRotation, eventRotation, 10, Distances.EUCLIDEAN_DISTANCE).getDistance();

            if(distanceAccel < 15 && distanceRotation < 200){
                matchFound = true;
                labelFound = eventLabel;
                break;
            }
        }
        eventCursor.close();

        if(matchFound) {
            try {
                //TODO send actual message
                Events.sendEvent(context, null, labelFound);
            } catch (IOException e) {
                Log.d("EventSender", "Could not send event : " + e.getMessage());
            }
        }
        return null;
    }
}
