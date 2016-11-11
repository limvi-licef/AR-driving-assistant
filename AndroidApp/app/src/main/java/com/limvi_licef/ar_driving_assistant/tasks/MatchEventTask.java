package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Constants;
import com.limvi_licef.ar_driving_assistant.utils.Events;

import java.util.concurrent.TimeUnit;

public class MatchEventTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private final long startTimestamp;
    private final long endTimestamp;

    public MatchEventTask(Context context, int seconds) {
        this.context = context;
        this.endTimestamp = System.currentTimeMillis();
        this.startTimestamp = endTimestamp - TimeUnit.SECONDS.toMillis(seconds);
    }

    @Override
    protected Void doInBackground(Void... params) {

        for (Events.Event event : Events.getAllEvents(context)) {
            long eventDuration = event.endTimestamp - event.startTimestamp;
            Log.d("DTW", "Event : " + event.label + " From : " + event.startTimestamp + " To : " + event.endTimestamp);
            Log.d("DTW", "Duration : " + eventDuration);

            for(long start = startTimestamp, stop = startTimestamp + eventDuration; stop < this.endTimestamp; start += Constants.TIME_BETWEEN_SEGMENTS, stop += Constants.TIME_BETWEEN_SEGMENTS){

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

                double distanceAccel = FastDTW.compare(eventAccel, segmentAccel, Constants.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
                double distanceRotation = FastDTW.compare(eventRotation, segmentRotation, Constants.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
                double distanceSpeed = FastDTW.compare(eventSpeed, segmentSpeed, Constants.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();

                Log.d("DTW", "Distance Acceleration : " + distanceAccel + " Distance Rotation : " + distanceRotation + " Distance Speed : " + distanceSpeed);

                if(distanceAccel < Constants.ACCELERATION_DISTANCE_CUTOFF && distanceRotation < Constants.ROTATION_DISTANCE_CUTOFF && distanceSpeed < Constants.SPEED_DISTANCE_CUTOFF){
                    Log.d("DTW", "Match Found : " + event.label);
                    matchFound(event);
                } else {
                    Log.d("DTW", "No match");
                }
            }
        }
        Log.d("DTW", "DTW Done");
        return null;
    }

    private void matchFound(Events.Event e){
        Broadcasts.sendWriteToUIBroadcast(context, "Match found : " + e.label);
        String status = Events.sendEvent(context, e.type.name(), e.message);
        Broadcasts.sendWriteToUIBroadcast(context, "Event status : " + status);
    }
}
