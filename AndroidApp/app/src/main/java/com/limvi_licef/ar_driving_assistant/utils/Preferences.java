package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;

import com.limvi_licef.ar_driving_assistant.models.sensors.AccelerationSensor;
import com.limvi_licef.ar_driving_assistant.models.sensors.RotationSensor;
import com.limvi_licef.ar_driving_assistant.models.sensors.SensorType;
import com.limvi_licef.ar_driving_assistant.models.sensors.SpeedSensor;

import java.util.ArrayList;
import java.util.List;

public final class Preferences {

    public static final String USER_SHARED_PREFERENCES = "user_shared";
    public static final String ID_PREFERENCE = "user_id";
    public static final String IP_ADDRESS_PREFERENCE = "user_ip";
    public static final String OFFSET_X_PREF = "offset_x";
    public static final String OFFSET_Y_PREF = "offset_y";
    public static final String OFFSET_Z_PREF = "offset_z";
    public static final String ACCEL_DISTANCE_CUTOFF = "acceleration_distance_cutoff";
    public static final String ROTATION_DISTANCE_CUTOFF = "rotation_distance_cutoff";
    public static final String SPEED_DISTANCE_CUTOFF = "speed_distance_cutoff";
    private static final String DEFAULT_ID = "0";

    private Preferences(){}

    /*
     * Returns the current user id defined in 'Setup' Preferences button
     */
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(ID_PREFERENCE, DEFAULT_ID);
    }

    /*
     * Returns the ip address defined in 'Setup' Preferences button
     */
    public static String getIPAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(IP_ADDRESS_PREFERENCE, null);
    }

    public static List<SensorType> getEnabledSensors(Context context) {
        List<SensorType> sensors = new ArrayList<>();
        SharedPreferences settings = context.getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if( settings.getBoolean(AccelerationSensor.class.getSimpleName(), false) ) {
            sensors.add(new AccelerationSensor());
        }
        if( settings.getBoolean(RotationSensor.class.getSimpleName(), false) ) {
            sensors.add(new RotationSensor());
        }
        if( settings.getBoolean(SpeedSensor.class.getSimpleName(), false) ) {
            sensors.add(new SpeedSensor());
        }
        return sensors;
    }

    /*
     * http://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
     * Shared Preferences getter and setter for double
     */
    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}
