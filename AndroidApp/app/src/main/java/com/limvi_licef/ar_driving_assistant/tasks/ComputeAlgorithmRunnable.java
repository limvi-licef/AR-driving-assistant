package com.limvi_licef.ar_driving_assistant.tasks;

import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

public abstract interface ComputeAlgorithmRunnable extends Runnable {

    @Override
    void run();

    void accumulateData(TimestampedDouble d);

    void clearData();

    void startRunnable();

    void stopRunnable();
}

