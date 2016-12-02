package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.Context;

import com.limvi_licef.ar_driving_assistant.algorithms.DynamicTimeWarpingAlgorithm;
import com.limvi_licef.ar_driving_assistant.algorithms.EventAlgorithm;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Events;

import java.util.ArrayList;
import java.util.List;

public class MatchEventRunnable implements Runnable {

    private Context context;
    private long endTimestamp;
    private long startTimestamp;

    public MatchEventRunnable (Context context) {
        this.context = context;
        this.endTimestamp = System.currentTimeMillis();
        this.startTimestamp = endTimestamp - Config.SensorDataCollection.SHORT_DELAY;
    }

    /**
     * Run the latest batch of sensor data for each TrainingEvent and for each algorithm
     */
    @Override
    public void run() {

        List<EventAlgorithm> algorithms = new ArrayList<>();
        algorithms.add(new DynamicTimeWarpingAlgorithm(context, startTimestamp, endTimestamp));

        for (Events.Event event : Events.getAllEvents(context)) {
            for(EventAlgorithm algorithm : algorithms) {
                algorithm.processEvent(event);
            }
        }
    }
}
