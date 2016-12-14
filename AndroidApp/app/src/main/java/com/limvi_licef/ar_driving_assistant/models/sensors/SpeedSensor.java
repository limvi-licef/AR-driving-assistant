package com.limvi_licef.ar_driving_assistant.models.sensors;

import android.content.Context;
import android.content.SharedPreferences;

import com.limvi_licef.ar_driving_assistant.config.DynamicTimeWarping;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

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
    public double getDistanceCutoff(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return Preferences.getDouble(settings, Preferences.SPEED_DISTANCE_CUTOFF, DynamicTimeWarping.DEFAULT_SPEED_DISTANCE_CUTOFF);
    }
}
