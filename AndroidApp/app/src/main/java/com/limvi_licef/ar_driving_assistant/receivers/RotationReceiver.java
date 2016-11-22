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

import com.limvi_licef.ar_driving_assistant.runnables.ComputeAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAzimuthRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAzimuthRunnable;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Statistics;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

import static android.content.Context.SENSOR_SERVICE;

public class RotationReceiver implements SensorReceiver, SensorEventListener {

    public boolean isRegistered;

    private ComputeAlgorithmRunnable runnable;
    private RewriteAlgorithmRunnable rewriteRunnable;

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] orientation = new float[3];
    private float[] rMat = new float[16];
    private long previousTimestamp = 0;

    public RotationReceiver() {}

    public void register(Context context, Handler handler) {
        isRegistered = true;
        runnable = new ComputeAzimuthRunnable(handler, context);
        handler.postDelayed(runnable, Config.SensorDataCollection.SHORT_DELAY);
        rewriteRunnable = new RewriteAzimuthRunnable(handler, context);
        handler.postDelayed(rewriteRunnable, Config.SensorDataCollection.LONG_DELAY);
        sensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            savePrematurely();
            sensorManager.unregisterListener(this, rotationSensor);
            isRegistered = false;
            return true;
        }
        return false;
    }

    public void savePrematurely(){
        if(!runnable.isRunning()) runnable.run();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.d("Rotation Receiver", "Received event");
            if(System.currentTimeMillis() - previousTimestamp <= Config.SensorDataCollection.MINIMUM_DELAY) return;
            if (event.values.length == 0) return;

            SensorManager.getRotationMatrixFromVector(rMat, event.values);

            long roundedTimestamp = Statistics.roundOffTimestamp(System.currentTimeMillis(), Config.SensorDataCollection.ROTATION_PRECISION);
            runnable.accumulateData(new TimestampedDouble(roundedTimestamp, (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360));
            previousTimestamp = System.currentTimeMillis();
        }
    }
}
