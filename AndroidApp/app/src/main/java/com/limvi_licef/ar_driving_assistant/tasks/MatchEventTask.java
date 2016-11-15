package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
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

            Log.d("DTW", "Event : " + event.label + " From : " + event.startTimestamp + " To : " + event.endTimestamp);
            Log.d("DTW", "Duration : " + event.duration);

            for(long start = startTimestamp, stop = startTimestamp + event.duration; stop < this.endTimestamp;
                            start += Config.DynamicTimeWarping.TIME_BETWEEN_SEGMENTS, stop += Config.DynamicTimeWarping.TIME_BETWEEN_SEGMENTS){

                Log.d("DTW", "Segment / From : " + start + " To : " + stop);

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

                Log.d("DTW", "Distance Acceleration : " + distanceAccel + " Distance Rotation : " + distanceRotation + " Distance Speed : " + distanceSpeed);

                if(distanceAccel < Config.DynamicTimeWarping.ACCELERATION_DISTANCE_CUTOFF
                        && distanceRotation < Config.DynamicTimeWarping.ROTATION_DISTANCE_CUTOFF
                        && distanceSpeed < Config.DynamicTimeWarping.SPEED_DISTANCE_CUTOFF){
                    Log.d("DTW", "Match Found : " + event.label);
                    matchFound(event);
                } else {
                    Log.d("DTW", "No match");
                }
            }
        }
        Broadcasts.sendWriteToUIBroadcast(context, "Closest Acceleration : " + closestAcceleration + " Closest Rotation : " + closestRotation + " Closest Speed : " + closestSpeed);
        Log.d("DTW", "DTW Done");
        Broadcasts.sendWriteToUIBroadcast(context, "DTW done");
        return null;
    }

    private void matchFound(Events.Event e){
        Broadcasts.sendWriteToUIBroadcast(context, context.getResources().getString(R.string.match_event_task_match_found) + e.label);
        String status = Events.sendEvent(context, e.type.name(), e.message);
        Broadcasts.sendWriteToUIBroadcast(context, context.getResources().getString(R.string.match_event_task_status) + status);
    }
}
