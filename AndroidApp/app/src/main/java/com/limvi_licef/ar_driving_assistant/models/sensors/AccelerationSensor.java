package com.limvi_licef.ar_driving_assistant.models.sensors;

import com.limvi_licef.ar_driving_assistant.config.DynamicTimeWarping;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.models.sensors.SensorType;

public final class AccelerationSensor implements SensorType {
    public String getType() {
        return getClass().getSimpleName();
    }
    public String getTableName() {
        return DatabaseContract.LinearAccelerometerData.TABLE_NAME;
    }
    public String[] getColumns() {
        return new String[]{DatabaseContract.LinearAccelerometerData.AXIS_X, DatabaseContract.LinearAccelerometerData.AXIS_Y, DatabaseContract.LinearAccelerometerData.AXIS_Z};
    }
    public String getDistanceColumn() {
        return DatabaseContract.ResultsDTW.DISTANCE_ACCELERATION;
    }
    public double getDistanceCutoff() {
        return DynamicTimeWarping.ACCELERATION_DISTANCE_CUTOFF;
    }
}
