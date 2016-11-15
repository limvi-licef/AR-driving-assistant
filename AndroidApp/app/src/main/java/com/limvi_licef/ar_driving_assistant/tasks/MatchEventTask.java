package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Events;

public class MatchEventTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private final long startTimestamp;
    private final long endTimestamp;

    /**
     * Task that tries to match each TrainingEvent to recent sensor data
     * @param context
     * @param duration Defines how far back to fetch the sensor data, in milliseconds
     */
    public MatchEventTask(Context context, long duration) {
        this.context = context;
        this.endTimestamp = System.currentTimeMillis();
        this.startTimestamp = endTimestamp - duration;
    }

    @Override
    protected Void doInBackground(Void... params) {

        Double closestAcceleration = null;
        Double closestRotation = null;
        Double closestSpeed = null;

        for (Events.Event event : Events.getAllEvents(context)) {

            for(long start = startTimestamp, stop = startTimestamp + event.duration; stop < this.endTimestamp;
                            start += Config.DynamicTimeWarping.TIME_BETWEEN_SEGMENTS, stop += Config.DynamicTimeWarping.TIME_BETWEEN_SEGMENTS){

                TimeSeries eventAccel, eventRotation, eventSpeed, segmentAccel, segmentRotation, segmentSpeed;
                try {
                    eventAccel = Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                            DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
                    eventRotation = Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
                    eventSpeed = Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.SpeedData.TABLE_NAME, DatabaseContract.SpeedData.SPEED);

                    segmentAccel = Events.createTimeSeriesFromSensor(context, start, stop, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                            DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
                    segmentRotation = Events.createTimeSeriesFromSensor(context, start, stop, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
                    segmentSpeed = Events.createTimeSeriesFromSensor(context, start, stop, DatabaseContract.SpeedData.TABLE_NAME, DatabaseContract.SpeedData.SPEED);
                } catch(IndexOutOfBoundsException e) {
                    Log.d("DTW", "No data in segment");
                    continue;
                }

                double distanceAccel = FastDTW.compare(eventAccel, segmentAccel, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
                double distanceRotation = FastDTW.compare(eventRotation, segmentRotation, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
                double distanceSpeed = FastDTW.compare(eventSpeed, segmentSpeed, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();

                closestAcceleration = (closestAcceleration == null || distanceAccel < closestAcceleration ) ? distanceAccel : closestAcceleration;
                closestRotation = (closestRotation == null || distanceRotation < closestRotation ) ? distanceRotation : closestRotation;
                closestSpeed = (closestSpeed == null || distanceSpeed < closestSpeed ) ? distanceSpeed : closestSpeed;

                if(distanceAccel < Config.DynamicTimeWarping.ACCELERATION_DISTANCE_CUTOFF
                        && distanceRotation < Config.DynamicTimeWarping.ROTATION_DISTANCE_CUTOFF
                        && distanceSpeed < Config.DynamicTimeWarping.SPEED_DISTANCE_CUTOFF){
                    matchFound(event);
                }
                saveResults(event, start, stop, distanceAccel, distanceRotation, distanceSpeed);
            }
        }
        Broadcasts.sendWriteToUIBroadcast(context, "Closest Acceleration : " + closestAcceleration + " Closest Rotation : " + closestRotation + " Closest Speed : " + closestSpeed);
        Broadcasts.sendWriteToUIBroadcast(context, "DTW done");
        return null;
    }

    private void matchFound(Events.Event e){
        Broadcasts.sendWriteToUIBroadcast(context, context.getResources().getString(R.string.match_event_task_match_found) + e.label);
        String status = Events.sendEvent(context, e.type.name(), e.message);
        Broadcasts.sendWriteToUIBroadcast(context, context.getResources().getString(R.string.match_event_task_status) + status);
    }

    private void saveResults(Events.Event event, long segmentStart, long segmentStop, double distanceAccel, double distanceRotation, double distanceSpeed) {
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ResultsDTW.EVENT_LABEL, event.label);
        values.put(DatabaseContract.ResultsDTW.EVENT_DURATION, event.duration);
        values.put(DatabaseContract.ResultsDTW.SEGMENT_START, segmentStart);
        values.put(DatabaseContract.ResultsDTW.SEGMENT_STOP, segmentStop);
        values.put(DatabaseContract.ResultsDTW.DISTANCE_ACCELERATION, distanceAccel);
        values.put(DatabaseContract.ResultsDTW.DISTANCE_ROTATION, distanceRotation);
        values.put(DatabaseContract.ResultsDTW.DISTANCE_SPEED, distanceSpeed);
        db.insert(DatabaseContract.ResultsDTW.TABLE_NAME, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
