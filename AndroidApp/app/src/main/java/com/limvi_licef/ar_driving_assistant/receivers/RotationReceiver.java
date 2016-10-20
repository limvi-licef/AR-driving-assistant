package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.tasks.ComputeAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.tasks.ComputeAzimuthRunnable;

import static android.content.Context.SENSOR_SERVICE;

public class RotationReceiver implements SensorEventListener {

    public boolean isRegistered;

    private ComputeAlgorithmRunnable runnable;

    private int axisX, axisY;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] orientation = new float[3];
    private float[] rMat = new float[16];
    private float[] rMatRemap = new float[16];

    public RotationReceiver() {}

    public void register(Context context, Handler handler) {
        isRegistered = true;
        runnable = new ComputeAzimuthRunnable(handler, context);
        runnable.startRunnable();
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
            if (event.values.length == 0) return;

            SensorManager.getRotationMatrixFromVector(rMat, event.values);
//            setAxis(context);
//            SensorManager.remapCoordinateSystem(rMat, axisX, axisY, rMatRemap);

            runnable.accumulateData(new TimestampedDouble(System.currentTimeMillis(), (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360));
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
