package com.limvi_licef.ar_driving_assistant.models.sensors;

import android.content.Context;
import android.content.SharedPreferences;

import com.limvi_licef.ar_driving_assistant.config.DynamicTimeWarping;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

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
    public double getDistanceCutoff(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return Preferences.getDouble(settings, Preferences.ACCEL_DISTANCE_CUTOFF, DynamicTimeWarping.DEFAULT_ACCELERATION_DISTANCE_CUTOFF);
    }
}
