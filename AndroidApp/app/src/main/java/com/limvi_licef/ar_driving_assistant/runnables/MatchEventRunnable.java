package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.Context;
import android.database.Cursor;

import com.limvi_licef.ar_driving_assistant.algorithms.DynamicTimeWarpingAlgorithm;
import com.limvi_licef.ar_driving_assistant.algorithms.EventAlgorithm;
import com.limvi_licef.ar_driving_assistant.config.SensorDataCollection;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.models.Event;

import java.util.ArrayList;
import java.util.List;

public class MatchEventRunnable implements Runnable {

    private Context context;
    private long endTimestamp;
    private long startTimestamp;

    public MatchEventRunnable (Context context) {
        this.context = context;
        this.endTimestamp = System.currentTimeMillis();
        this.startTimestamp = endTimestamp - SensorDataCollection.SHORT_DELAY;
    }

    /**
     * Run the latest batch of sensor data for each TrainingEvent and for each EventAlgorithm
     */
    @Override
    public void run() {

        List<EventAlgorithm> algorithms = new ArrayList<>();
        algorithms.add(new DynamicTimeWarpingAlgorithm(context, startTimestamp, endTimestamp));

        for (Event event : getAllEvents(context)) {
            for(EventAlgorithm algorithm : algorithms) {
                algorithm.processEvent(event);
            }
        }
    }

    /**
     * Fetches all TrainingEvents in the database
     * @param context
     * @return
     */
    private static List<Event> getAllEvents(Context context){
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
                    eventCursor.getLong(durationColumnIndex), Event.EventTypes.valueOf(eventCursor.getString(typeColumnIndex)), eventCursor.getString(messageColumnIndex)));
        }

        eventCursor.close();
        return events;
    }
}
