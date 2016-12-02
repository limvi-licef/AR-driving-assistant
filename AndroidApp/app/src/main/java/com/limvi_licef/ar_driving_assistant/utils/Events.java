package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.database.Cursor;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.limvi_licef.ar_driving_assistant.algorithms.TimeSeriesExtended;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public final class Events {

    private Events(){}

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
     * Creates a TimeSeriesExtended using given parameters
     * @param context
     * @param startTimestamp the timestamp from which to fetch data
     * @param endTimestamp the timestamp to which to fetch data
     * @param tableName the table containing the columns
     * @param valueColumnNames the varargs strings defining the columns to be used to create the time series
     * @return
     */
    public static TimeSeriesExtended createTimeSeriesFromSensor(Context context, long startTimestamp, long endTimestamp, String tableName, String... valueColumnNames) {
        TimeSeriesExtended.Builder b = TimeSeriesExtended.builder();

        List<List<Structs.TimestampedDouble>> valuesList = new ArrayList<>();
        for(String column : valueColumnNames) {
            valuesList.add(Database.getSensorData(startTimestamp, endTimestamp, tableName, column, context));
        }

        for(int i = 0; i < valuesList.get(0).size(); i++) {
            double [] values = new double[valueColumnNames.length];
            try {
                for(int j = 0; j < valueColumnNames.length; j++){
                    values[j] = valuesList.get(j).get(i).value;
                }
                b.add(valuesList.get(0).get(i).timestamp, values);
            } catch (IndexOutOfBoundsException e) {
                //skip
            }
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
