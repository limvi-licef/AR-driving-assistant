package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.limvi_licef.ar_driving_assistant.algorithms.DynamicTimeWarpingAlgorithm;
import com.limvi_licef.ar_driving_assistant.algorithms.EventAlgorithm;
import com.limvi_licef.ar_driving_assistant.utils.Events;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Run the latest batch of sensor data for each TrainingEvent and for each algorithm
     * @param params
     * @return
     */
    @Override
    protected Void doInBackground(Void... params) {

        List<EventAlgorithm> algorithms = new ArrayList<>();
        algorithms.add(new DynamicTimeWarpingAlgorithm(context, startTimestamp, endTimestamp));

        for (Events.Event event : Events.getAllEvents(context)) {
            for(EventAlgorithm algorithm : algorithms) {
                algorithm.processEvent(event);
            }
        }
        return null;
    }

}
