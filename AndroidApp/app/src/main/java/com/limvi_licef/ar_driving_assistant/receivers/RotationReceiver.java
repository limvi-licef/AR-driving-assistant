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
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.aware.Rotation;
import com.aware.providers.Rotation_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

import static android.content.Context.SENSOR_SERVICE;

public class RotationReceiver implements SensorEventListener {

    public boolean isRegistered;

    private Context context;
    private int axisX, axisY;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] orientation = new float[3];
    private float[] rMat = new float[16];
    private float[] rMatRemap = new float[16];

    public RotationReceiver() {}

    public void register(Context context, Handler handler) {
        isRegistered = true;
        this.context = context;
        sensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            sensorManager.unregisterListener(this, rotationSensor);
            isRegistered = false;
            return true;
        }
        return false;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.d("Rotation Receiver", "Received event");
            SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
            if (event.values.length == 0) return;

            String idPref = context.getResources().getString(R.string.user_id_pref);
            SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            String userId = prefs.getString(idPref, null);
            if (userId == null) return;

            ContentValues valuesToSave = new ContentValues();
            valuesToSave.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
            valuesToSave.put(DatabaseContract.RotationData.TIMESTAMP, System.currentTimeMillis());
            valuesToSave.put(DatabaseContract.RotationData.AXIS_X, event.values[0]);
            valuesToSave.put(DatabaseContract.RotationData.AXIS_Y, event.values[1]);
            valuesToSave.put(DatabaseContract.RotationData.AXIS_Z, event.values[2]);

            SensorManager.getRotationMatrixFromVector(rMat, event.values);
//            setAxis(context);
//            SensorManager.remapCoordinateSystem(rMat, axisX, axisY, rMatRemap);

            valuesToSave.put(DatabaseContract.RotationData.AZIMUTH, (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360);
            Log.d("Rotate Azimuth", String.valueOf((Math.toDegrees(orientation[0]) + 360) % 360));

            db.insert(DatabaseContract.RotationData.TABLE_NAME, null, valuesToSave);

            Intent localIntent = new Intent(Settings.ACTION_INSERT_DONE).putExtra(Settings.INSERT_STATUS, DatabaseContract.RotationData.TABLE_NAME + System.currentTimeMillis());
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
            Log.d("Rotation Receiver", "Finished insert");
        }
    }

    private void setAxis(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int screenRotation = wm.getDefaultDisplay().getRotation();

        switch (screenRotation) {
            case Surface.ROTATION_0:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Y;
                break;

            case Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;

            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
                break;

            case Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;

            default:
                break;
        }
    }
}
