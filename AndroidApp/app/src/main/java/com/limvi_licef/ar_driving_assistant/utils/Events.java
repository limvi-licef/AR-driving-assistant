package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

public final class Events {

    private Events(){}

    /**
     * Sends a JSON string to the UnityApp
     * @param context
     * @param data the json string to send
     * @return
     */
    public static String sendJson(Context context, JSONObject data) {
        try {
            String message = data.toString();
            String ipString = Preferences.getIPAddress(context);
            if (ipString == null || ipString.isEmpty()) return context.getResources().getString(R.string.send_event_task_invalid_ip);

            InetAddress ipAddress = InetAddress.getByName(ipString);
            DatagramSocket socket = DatagramChannel.open().socket();
            DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), ipAddress, Config.HoloLens.HOLOLENS_PORT);
            socket.send(dp);
            socket.close();
        } catch (IOException e) {
            Log.d("EventSender", "" + e.getMessage());
            return context.getResources().getString(R.string.send_event_task_failure);
        }
        return context.getResources().getString(R.string.send_event_task_success);
    }

    /**
     * Fetches all TrainingEvents in the database
     * @param context
     * @return
     */
    public static List<Event> getAllEvents(Context context){
        List<Event> events = new ArrayList<>();
        Cursor eventCursor = DatabaseHelper.getHelper(context).getWritableDatabase().query(DatabaseContract.TrainingEvents.TABLE_NAME,
                new String[]{DatabaseContract.TrainingEvents.START_TIMESTAMP,
                        DatabaseContract.TrainingEvents.END_TIMESTAMP,
                        DatabaseContract.TrainingEvents.DURATION,
                        DatabaseContract.TrainingEvents.LABEL,
                        DatabaseContract.TrainingEvents.TYPE,
                        DatabaseContract.TrainingEvents.MESSAGE},
                null, null, null, null, null);
        int startTimestampColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.START_TIMESTAMP);
        int endTimestampColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.END_TIMESTAMP);
        int durationColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.DURATION);
        int labelColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.LABEL);
        int typeColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.TYPE);
        int messageColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.TrainingEvents.MESSAGE);

        while (eventCursor.moveToNext()) {
            events.add(new Event(eventCursor.getString(labelColumnIndex), eventCursor.getLong(startTimestampColumnIndex), eventCursor.getLong(endTimestampColumnIndex),
                    eventCursor.getLong(durationColumnIndex), EventTypes.valueOf(eventCursor.getString(typeColumnIndex)), eventCursor.getString(messageColumnIndex)));
        }

        eventCursor.close();
        return events;
    }

    /**
     * Creates a TimeSeries using given parameters
     * @param context
     * @param startTimestamp the timestamp from which to fetch data
     * @param endTimestamp the timestamp to which to fetch data
     * @param tableName the table containing the columns
     * @param valueColumnNames the varargs strings defining the columns to be used to create the time series
     * @return
     * @throws IndexOutOfBoundsException
     */
    public static TimeSeries createTimeSeriesFromSensor(Context context, long startTimestamp, long endTimestamp, String tableName, String... valueColumnNames) throws IndexOutOfBoundsException {
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

    /*
     * Event types defined in the UnityApp
     */
    public enum EventTypes {
        Information, Advice, Warning, LeftWarning, RightWarning
    }

    public static class Event {
        public String label;
        public long startTimestamp;
        public long endTimestamp;
        public long duration;
        public EventTypes type;
        public String message;

        public Event (String label, long startTimestamp, long endTimestamp, long duration, EventTypes type, String message) {
            this.label = label;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.duration = duration;
            this.type = type;
            this.message = message;
        }
    }
}
