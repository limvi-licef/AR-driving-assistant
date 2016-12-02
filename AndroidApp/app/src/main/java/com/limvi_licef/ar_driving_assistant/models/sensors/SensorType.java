package com.limvi_licef.ar_driving_assistant.models.sensors;

/**
 * Interface to keep track of each sensor's info
 */
public interface SensorType {
    String getType();
    String getTableName();
    String[] getColumns();
    String getDistanceColumn();
    double getDistanceCutoff();
}
