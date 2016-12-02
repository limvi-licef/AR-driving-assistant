package com.limvi_licef.ar_driving_assistant.algorithms;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.threads.TCPListenerThread;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Database;
import com.limvi_licef.ar_driving_assistant.utils.Events;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DynamicTimeWarpingAlgorithm implements EventAlgorithm {

    private Context context;
    private final long startTimestamp;
    private final long endTimestamp;
    private Map<String,List<Structs.TimestampTuple>> matches = new HashMap<>();

    private List<SensorType> sensors = new ArrayList<>();

    /**
     * Create DynamicTimeWarping object and define the time period by which the sensor data will be fetched
     * @param context
     * @param startTimestamp the start timestamp of the time period
     * @param endTimestamp the end timestamp of the data of the time period
     */
    public DynamicTimeWarpingAlgorithm(Context context, long startTimestamp, long endTimestamp){
        this.context = context;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        enableCheckedSensors();
    }

    /**
     * Compare the training event to each possible segment in the recent sensor data to try to find a match
     * A segment is a continuous list of data with the same length as the event
     * @param event The event to process
     */
    public void processEvent(Events.Event event) {
        long logTimestamp = System.currentTimeMillis();
        Broadcasts.sendWriteToUIBroadcast(context, "DTW start : " + logTimestamp);

        for(SensorType sensor : sensors) {
            processSensor(event, sensor);
        }
        if(isMatchFound(event)) {
            matchFound(event);
        }

        Broadcasts.sendWriteToUIBroadcast(context, "DTW done : " + (System.currentTimeMillis() - logTimestamp));
    }

    /**
     * Process an Event using a specific SensorType
     * @param event the Event to process
     * @param sensor the SensorType to process
     */
    private void processSensor(Events.Event event, SensorType sensor){
        TimeSeries eventTS = Events.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, sensor.getTableName(), sensor.getColumns());
        if(eventTS.size() == 0) {
            Broadcasts.sendWriteToUIBroadcast(context, "DTW : Could not create event TimeSeries for " + sensor.getType());
            return;
        }
        TimeSeriesExtended segmentTS = Events.createTimeSeriesFromSensor(context, startTimestamp, startTimestamp + event.duration, sensor.getTableName(), sensor.getColumns());

        List<List<Structs.TimestampedDouble>> remaining = new ArrayList<>();
        for(String column : sensor.getColumns()) {
            remaining.add(Database.getSensorData(startTimestamp + event.duration, endTimestamp, sensor.getTableName(), column, context));
        }

        //set segment start/stop
        long start = startTimestamp;
        long stop = startTimestamp + event.duration;
        double distance;

        //Find Euclidean distance between each time series
        if(segmentTS.size() != 0){
            distance = FastDTW.compare(eventTS, segmentTS, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
            saveResults(event, start, stop, distance, sensor.getDistanceColumn());
        }

        //Find Euclidean distance for remaining data
        for(int i = 0; i < remaining.get(0).size(); i++){

            //get next value from remaining data
            double[] values = new double[remaining.size()];
            try{
                for(int j = 0; j < remaining.size(); j++) {
                    values[j] = remaining.get(j).get(i).value;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }

            //remove first and add one from remaining
            segmentTS.shiftRightByOne(eventTS.size(), remaining.get(0).get(i).timestamp, values);

            //update segment start/stop
            start = (long)segmentTS.getTimeAtNthPoint(0);
            stop = (long)segmentTS.getTimeAtNthPoint(segmentTS.size()-1);

            distance = FastDTW.compare(eventTS, segmentTS, Config.DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
            saveResults(event, start, stop, distance, sensor.getDistanceColumn());

            //Match is considered found if within particular distance
            if(distance < sensor.getDistanceCutoff()){
                matches.get(sensor.getType()).add(new Structs.TimestampTuple(start, stop));
            }
        }
    }

    /**
     * Checks if a match was found for all SensorTypes that were used to process the event
     * @return true if all sensors matches
     */
    private boolean isMatchFound(Events.Event event) {
        if(matches.size() == 1) {
            return matches.entrySet().iterator().next().getValue().size() != 0;
        }
        List<Structs.TimestampTuple> toRemove = new ArrayList<>();
        Iterator<Map.Entry<String,List<Structs.TimestampTuple>>> it = matches.entrySet().iterator();
        List<Structs.TimestampTuple> first = it.next().getValue(), next;
        while(it.hasNext()) {
            next = it.next().getValue();

            for(Structs.TimestampTuple i : first) {
                boolean match = false;
                for(Structs.TimestampTuple j : next) {
                    if(i.first - (event.duration / 2) <= j.first && i.second + (event.duration / 2) >= j.second ) {
                        match = true;
                        break;
                    }
                }
                if(!match) {
                    toRemove.add(i);
                }
            }
            first.removeAll(toRemove);

        }
        return first.size() != 0;
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
     * @param distance
     * @param distanceColumn
     */
    private void saveResults(Events.Event event, long segmentStart, long segmentStop, double distance, String distanceColumn) {
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ResultsDTW.EVENT_LABEL, event.label);
        values.put(DatabaseContract.ResultsDTW.EVENT_DURATION, event.duration);
        values.put(DatabaseContract.ResultsDTW.SEGMENT_START, segmentStart);
        values.put(DatabaseContract.ResultsDTW.SEGMENT_STOP, segmentStop);
        values.put(distanceColumn, distance);
        db.insert(DatabaseContract.ResultsDTW.TABLE_NAME, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Verify which sensor have been checked in the DTW dialog and enable them for the algorithm
     */
    private void enableCheckedSensors() {
        SharedPreferences settings = context.getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if( settings.getBoolean(AccelerationSensor.class.getSimpleName(), false) ) {
            sensors.add(new AccelerationSensor());
        }
        if( settings.getBoolean(RotationSensor.class.getSimpleName(), false) ) {
            sensors.add(new RotationSensor());
        }
        if( settings.getBoolean(SpeedSensor.class.getSimpleName(), false) ) {
            sensors.add(new SpeedSensor());
        }
        for(SensorType sensor : sensors) {
            matches.put(sensor.getType(), new ArrayList<Structs.TimestampTuple>());
        }
    }

    /**
     * Interface to keep track of each sensor's info
     */
    private interface SensorType {
        String getType();
        String getTableName();
        String[] getColumns();
        String getDistanceColumn();
        double getDistanceCutoff();
    }

    public final class AccelerationSensor implements SensorType {
        public String getType() { return getClass().getSimpleName(); }
        public String getTableName() { return DatabaseContract.LinearAccelerometerData.TABLE_NAME; }
        public String[] getColumns() { return new String[]{DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z}; }
        public String getDistanceColumn() { return DatabaseContract.ResultsDTW.DISTANCE_ACCELERATION; }
        public double getDistanceCutoff() { return Config.DynamicTimeWarping.ACCELERATION_DISTANCE_CUTOFF; }
    }

    public final class RotationSensor implements SensorType {
        public String getType() { return getClass().getSimpleName(); }
        public String getTableName() { return DatabaseContract.RotationData.TABLE_NAME; }
        public String[] getColumns() { return new String[]{DatabaseContract.RotationData.AZIMUTH}; }
        public String getDistanceColumn() { return DatabaseContract.ResultsDTW.DISTANCE_ROTATION; }
        public double getDistanceCutoff() { return Config.DynamicTimeWarping.ROTATION_DISTANCE_CUTOFF; }
    }

    public final class SpeedSensor implements SensorType {
        public String getType() { return getClass().getSimpleName(); }
        public String getTableName() { return DatabaseContract.SpeedData.TABLE_NAME; }
        public String[] getColumns() { return new String[]{DatabaseContract.SpeedData.SPEED}; }
        public String getDistanceColumn() { return DatabaseContract.ResultsDTW.DISTANCE_SPEED; }
        public double getDistanceCutoff() { return Config.DynamicTimeWarping.SPEED_DISTANCE_CUTOFF; }
    }
}
