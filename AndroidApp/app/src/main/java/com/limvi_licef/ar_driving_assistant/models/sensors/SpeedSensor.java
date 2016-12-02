package com.limvi_licef.ar_driving_assistant.models.sensors;

import com.limvi_licef.ar_driving_assistant.config.DynamicTimeWarping;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.models.sensors.SensorType;

public final class SpeedSensor implements SensorType {
    public String getType() {
        return getClass().getSimpleName();
    }
    public String getTableName() {
        return DatabaseContract.SpeedData.TABLE_NAME;
    }
    public String[] getColumns() {
        return new String[]{DatabaseContract.SpeedData.SPEED};
    }
    public String getDistanceColumn() {
        return DatabaseContract.ResultsDTW.DISTANCE_SPEED;
    }
    public double getDistanceCutoff() {
        return DynamicTimeWarping.SPEED_DISTANCE_CUTOFF;
    }
}
