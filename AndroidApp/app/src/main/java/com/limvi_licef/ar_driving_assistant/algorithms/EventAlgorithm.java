package com.limvi_licef.ar_driving_assistant.algorithms;

import com.limvi_licef.ar_driving_assistant.models.Event;

public interface EventAlgorithm {

    /**
     * Process the event using the algorithm implementation
     * @param event the event to process
     */
    void processEvent(Event event);
}
