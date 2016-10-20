package com.limvi_licef.ar_driving_assistant.tasks;

import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

import java.util.List;

public abstract interface ComputeAlgorithmRunnable extends Runnable {

    @Override
    void run();

    void accumulateData(TimestampedDouble d);

    void clearAllData();

    void clearData(List<TimestampedDouble> oldData);

    void startRunnable();

    void stopRunnable();
}

