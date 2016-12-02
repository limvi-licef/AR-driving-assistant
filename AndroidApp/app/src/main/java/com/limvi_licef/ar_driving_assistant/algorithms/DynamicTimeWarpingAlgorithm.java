package com.limvi_licef.ar_driving_assistant.algorithms;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.util.Distances;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.config.Communication;
import com.limvi_licef.ar_driving_assistant.config.DynamicTimeWarping;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.models.sensors.AccelerationSensor;
import com.limvi_licef.ar_driving_assistant.models.Event;
import com.limvi_licef.ar_driving_assistant.models.sensors.RotationSensor;
import com.limvi_licef.ar_driving_assistant.models.sensors.SensorType;
import com.limvi_licef.ar_driving_assistant.models.sensors.SpeedSensor;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.network.TCPListenerThread;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Database;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DynamicTimeWarpingAlgorithm implements EventAlgorithm {

    private Context context;
    private final long startTimestamp;
    private final long endTimestamp;
    private Map<String,List<TimestampTuple>> matches = new HashMap<>();

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
    public void processEvent(Event event) {
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
    private void processSensor(Event event, SensorType sensor){

        //create event timeseries
        TimeSeries eventTS = TimeSeriesExtended.createTimeSeriesFromSensor(context, event.startTimestamp, event.endTimestamp, sensor.getTableName(), sensor.getColumns());
        if(eventTS.size() == 0) {
            Broadcasts.sendWriteToUIBroadcast(context, "DTW : Could not create event TimeSeries for " + sensor.getType());
            return;
        }
        //create first segment timeseries
        TimeSeriesExtended segmentTS = TimeSeriesExtended.createTimeSeriesFromSensor(context, startTimestamp, startTimestamp + event.duration, sensor.getTableName(), sensor.getColumns());

        //get remaining data
        List<List<TimestampedDouble>> remaining = new ArrayList<>();
        for(String column : sensor.getColumns()) {
            remaining.add(Database.getSensorData(startTimestamp + event.duration, endTimestamp, sensor.getTableName(), column, context));
        }

        //set segment start/stop
        long start = startTimestamp;
        long stop = startTimestamp + event.duration;
        double distance;

        //Find Euclidean distance between each time series
        if(segmentTS.size() != 0){
            distance = FastDTW.compare(eventTS, segmentTS, DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
            saveResults(event, start, stop, distance, sensor.getDistanceColumn());
        }

        //Find Euclidean distance for each new segment the remaining data can produce
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

            distance = FastDTW.compare(eventTS, segmentTS, DynamicTimeWarping.SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
            saveResults(event, start, stop, distance, sensor.getDistanceColumn());

            //Match is considered found if within particular distance
            if(distance < sensor.getDistanceCutoff()){
                matches.get(sensor.getType()).add(new TimestampTuple(start, stop));
            }
        }
    }

    /**
     * Checks if a match was found for all SensorTypes that were used to process the event
     * @return true if all sensors matches
     */
    private boolean isMatchFound(Event event) {
        if(matches.size() == 1) {
            return matches.entrySet().iterator().next().getValue().size() != 0;
        }
        List<TimestampTuple> toRemove = new ArrayList<>();
        Iterator<Map.Entry<String,List<TimestampTuple>>> it = matches.entrySet().iterator();
        List<TimestampTuple> first = it.next().getValue(), next;
        while(it.hasNext()) {
            next = it.next().getValue();

            for(TimestampTuple i : first) {
                boolean match = false;
                for(TimestampTuple j : next) {
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
    private void matchFound(Event e){
        Broadcasts.sendWriteToUIBroadcast(context, context.getResources().getString(R.string.match_event_task_match_found) + e.label);
        String status;
        JSONObject json = new JSONObject();
        try {
            json.put(Communication.JSON_REQUEST_TYPE, Communication.JSON_REQUEST_TYPE_PARAM_EVENT);
            json.put(Communication.JSON_EVENT_TYPE, e.type.name());
            json.put(Communication.JSON_EVENT_MESSAGE, e.message);
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
    private void saveResults(Event event, long segmentStart, long segmentStop, double distance, String distanceColumn) {
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
            matches.put(sensor.getType(), new ArrayList<TimestampTuple>());
        }
    }

    /**
     * Data structure that holds two timestamps
     */
    private static class TimestampTuple {
        long first;
        long second;

        TimestampTuple(long first, long second) {
            this.first = first;
            this.second = second;
        }
    }
}
