package com.limvi_licef.ar_driving_assistant.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Statistics;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Task to calibrate the linear accelerometer sensor since each axis is off even when the device is still
 * To calibrate, an average of each axis is calculated in order to save an offset to be used when receiving data
 */
public class CalibrateTask extends AsyncTask<Void, Void, String> implements SensorEventListener {

    private final int SLEEP_TIME = 2000;
    private ProgressDialog dialog;
    private Context context;
    private SensorManager sensorManager;
    private Sensor linearAccelerationSensor;
    private List<Double> sensorDataX;
    private List<Double> sensorDataY;
    private List<Double> sensorDataZ;

    public CalibrateTask (Context context){
        Log.d("AsyncTask", "Created Calibration Task");
        this.context = context;
        dialog = new ProgressDialog(context);
        sensorDataX = new ArrayList<>();
        sensorDataY = new ArrayList<>();
        sensorDataZ = new ArrayList<>();
        sensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setTitle(context.getResources().getString(R.string.calibrate_task_title));
        this.dialog.setMessage(context.getResources().getString(R.string.calibrate_task_message));
        this.dialog.show();
        sensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            //let the sensor stabilize for 2 seconds first
            Thread.sleep(SLEEP_TIME);
            sensorDataX.clear();
            sensorDataY.clear();
            sensorDataZ.clear();
            Thread.sleep(SLEEP_TIME);
            return (sensorDataX.size() != 0 && sensorDataY.size() != 0  && sensorDataZ.size() != 0 ) ?
                    context.getResources().getString(R.string.calibrate_task_success) :
                    context.getResources().getString(R.string.calibrate_task_failure);
        } catch (InterruptedException e) {
            return context.getResources().getString(R.string.calibrate_task_failure);
        }
    }

    @Override
    protected void onPostExecute (String result) {
        sensorManager.unregisterListener(this, linearAccelerationSensor);
        if(result.equals(context.getResources().getString(R.string.calibrate_task_success))) {
            //save offset averages
            saveOffsets(Statistics.calculateAverage(sensorDataX), Statistics.calculateAverage(sensorDataY), Statistics.calculateAverage(sensorDataZ));
        }
        sensorDataX.clear();
        sensorDataY.clear();
        sensorDataZ.clear();
        if (dialog.isShowing()) { dialog.dismiss(); }
        new AlertDialog.Builder(context)
                .setMessage(result)
                .setNegativeButton(context.getResources().getString(R.string.calibrate_task_close), null)
                .show();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //Accumulate axis data
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            sensorDataX.add((double)event.values[0]);
            sensorDataY.add((double)event.values[1]);
            sensorDataZ.add((double)event.values[2]);
        }
    }

    /**
     * Save axis offsets to SharedPreferences
     * @param x
     * @param y
     * @param z
     */
    private void saveOffsets(double x, double y, double z){
        SharedPreferences.Editor editor = context.getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        Preferences.putDouble(editor, Preferences.OFFSET_X_PREF, x);
        Preferences.putDouble(editor, Preferences.OFFSET_Y_PREF, y);
        Preferences.putDouble(editor, Preferences.OFFSET_Z_PREF, z);
        editor.apply();
    }
}
