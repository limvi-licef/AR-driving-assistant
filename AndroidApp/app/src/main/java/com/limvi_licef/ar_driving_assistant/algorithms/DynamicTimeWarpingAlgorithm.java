package com.limvi_licef.ar_driving_assistant.algorithms;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.threads.TCPListenerThread;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Events;

import org.json.JSONException;
import org.json.JSONObject;

public class DynamicTimeWarpingAlgorithm implements EventAlgorithm {

    private Context context;
    private final long startTimestamp;
    private final long endTimestamp;

    public DynamicTimeWarpingAlgorithm(Context context, long startTimestamp, long endTimestamp){
        this.context = context;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    /**
     * Compare the event to recent sensor data and try to find a match
     * A new set of TimeSeries to compare is generated each Config.DynamicTimeWarping.TIME_BETWEEN_SEGMENTS
     * @param event The event to process
     */
    public void processEvent(Events.Event event) {
        Double closestAcceleration = null;
        Double closestRotation = null;
        Double closestSpeed = null;

        //Generate the event TimeSeries
        TimeSeries eventAccel, eventRotation, eventSpeed;
        eventAccel = Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
        eventRotation = Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
        eventSpeed = Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, DatabaseContract.SpeedData.TABLE_NAME, DatabaseContract.SpeedData.SPEED);

        for(long start = startTimestamp, stop = startTimestamp + event.duration; stop < endTimestamp;
            start += Config.DynamicTimeWarping.TIME_BETWEEN_SEGMENTS, stop += Config.DynamicTimeWarping.TIME_BETWEEN_SEGMENTS){

            //Generate TimeSeries for the segment to be compared
            TimeSeries segmentAccel, segmentRotation, segmentSpeed;
            try {
                segmentAccel = Events.createTimeSeriesFromSensor(context, start, stop, DatabaseContract.LinearAccelerometerData.TABLE_NAME,
                        DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z);
                segmentRotation = Events.createTimeSeriesFromSensor(context, start, stop, DatabaseContract.RotationData.TABLE_NAME, DatabaseContract.RotationData.AZIMUTH);
                segmentSpeed = Events.createTimeSeriesFromSensor(context, start, stop, DatabaseContract.SpeedData.TABLE_NAME, DatabaseContract.SpeedData.SPEED);
            } catch(IndexOutOfBoundsException e) {
                Log.d("DTW", "No data in segment");
                continue;
            }

            //Find Euclidean distance between each time series
            double distanceAccel = FastDTW.compare(eventAccel, segmentAccel, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
            double distanceRotation = FastDTW.compare(eventRotation, segmentRotation, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
            double distanceSpeed = FastDTW.compare(eventSpeed, segmentSpeed, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();

            closestAcceleration = (closestAcceleration == null || distanceAccel < closestAcceleration ) ? distanceAccel : closestAcceleration;
            closestRotation = (closestRotation == null || distanceRotation < closestRotation ) ? distanceRotation : closestRotation;
            closestSpeed = (closestSpeed == null || distanceSpeed < closestSpeed ) ? distanceSpeed : closestSpeed;

            //Match is considered found if within particular distance
            if(distanceAccel < Config.DynamicTimeWarping.ACCELERATION_DISTANCE_CUTOFF
                    && distanceRotation < Config.DynamicTimeWarping.ROTATION_DISTANCE_CUTOFF
                    && distanceSpeed < Config.DynamicTimeWarping.SPEED_DISTANCE_CUTOFF){
                matchFound(event);
            }
            saveResults(event, start, stop, distanceAccel, distanceRotation, distanceSpeed);
        }
        Broadcasts.sendWriteToUIBroadcast(context, "Closest Acceleration : " + closestAcceleration + " Closest Rotation : " + closestRotation + " Closest Speed : " + closestSpeed);
        Broadcasts.sendWriteToUIBroadcast(context, "DTW done");
    }

    /**
     * Send event to Unity app when a match is found
     * @param e The event to send
     */
    private void matchFound(Events.Event e){
        Broadcasts.sendWriteToUIBroadcast(context, context.getResources().getString(R.string.match_event_task_match_found) + e.label);
        String status;
        JSONObject json = new JSONObject();
        try {
            json.put(Config.HoloLens.JSON_REQUEST_TYPE, Config.HoloLens.JSON_REQUEST_TYPE_PARAM_EVENT);
            json.put(Config.HoloLens.JSON_EVENT_TYPE, e.type.name());
            json.put(Config.HoloLens.JSON_EVENT_MESSAGE, e.message);
            status = TCPListenerThread.sendJson(context, json);
        } catch (JSONException ex) {
            status = context.getResources().getString(R.string.send_event_task_failure);
        }
        Broadcasts.sendWriteToUIBroadcast(context, context.getResources().getString(R.string.match_event_task_status) + status);
    }

    /**
     * Save algorithm results
     * @param event
     * @param segmentStart
     * @param segmentStop
     * @param distanceAccel
     * @param distanceRotation
     * @param distanceSpeed
     */
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
