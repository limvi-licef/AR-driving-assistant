package com.limvi_licef.ar_driving_assistant.models;

import com.limvi_licef.ar_driving_assistant.config.DynamicTimeWarping;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;

public final class RotationSensor implements SensorType {
    public String getType() {
        return getClass().getSimpleName();
    }
    public String getTableName() {
        return DatabaseContract.RotationData.TABLE_NAME;
    }
    public String[] getColumns() {
        return new String[]{DatabaseContract.RotationData.AZIMUTH};
    }
    public String getDistanceColumn() {
        return DatabaseContract.ResultsDTW.DISTANCE_ROTATION;
    }
    public double getDistanceCutoff() {
        return DynamicTimeWarping.ROTATION_DISTANCE_CUTOFF;
    }
}
