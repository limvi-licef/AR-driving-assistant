package com.limvi_licef.ar_driving_assistant.models;

/*
* Data structure that holds a Double and a Timestamp
* Used to pass data through an algorithm but still retain its associated timestamp
*/
public class TimestampedDouble {
    public long timestamp;
    public Double value;

    public TimestampedDouble(long timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
