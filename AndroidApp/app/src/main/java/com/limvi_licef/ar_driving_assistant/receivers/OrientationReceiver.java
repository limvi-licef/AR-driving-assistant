package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.aware.providers.Rotation_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

import static android.content.Context.SENSOR_SERVICE;

public class OrientationReceiver implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor orientationSensor;
    public boolean isRegistered;
    private Context context;

    public void register(Context context, Handler handler) {
        isRegistered = true;
        this.context = context;
        sensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            sensorManager.unregisterListener(this);
            isRegistered = false;
            return true;
        }
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            Log.d("Orientation Receiver", "Received intent");

            double azimuth = event.values[0];
            Log.d("Orientation Azimuth", String.valueOf(azimuth));

            SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();

            String idPref = context.getResources().getString(R.string.user_id_pref);
            SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
            String userId = prefs.getString(idPref, null);
            if(userId == null) return;

            ContentValues valuesToSave = new ContentValues();
            valuesToSave.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
            valuesToSave.put(DatabaseContract.RotationData.TIMESTAMP, System.currentTimeMillis());
            valuesToSave.put(DatabaseContract.RotationData.AZIMUTH, azimuth);

            db.insert(DatabaseContract.RotationData.TABLE_NAME, null, valuesToSave);

            Intent localIntent = new Intent(Settings.ACTION_INSERT_DONE).putExtra(Settings.INSERT_STATUS, DatabaseContract.RotationData.TABLE_NAME + System.currentTimeMillis());
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
            Log.d("Orientation Receiver", "Finished insert");
        }
    }
}
