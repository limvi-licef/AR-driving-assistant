package com.limvi_licef.ar_driving_assistant.models;

public class Event {
    /*
     * Event types defined in the UnityApp
     */
    public enum EventTypes {
        Information, Advice, Warning, LeftWarning, RightWarning
    }

    public String label;
    public long startTimestamp;
    public long endTimestamp;
    public long duration;
    public EventTypes type;
    public String message;

    public Event(String label, long startTimestamp, long endTimestamp, long duration, EventTypes type, String message) {
        this.label = label;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.duration = duration;
        this.type = type;
        this.message = message;
    }
}
