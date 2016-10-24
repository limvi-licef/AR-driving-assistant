package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.aware.LinearAccelerometer;
import com.aware.providers.Linear_Accelerometer_Provider;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAccelerationRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAccelerationRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

public class LinearAccelerometerReceiver extends BroadcastReceiver {

    public boolean isRegistered;
    private boolean offsetDefined = false;
    private double offsetX;
    private double offsetY;
    private double offsetZ;
    private ComputeAlgorithmRunnable runnable;
    private RewriteAlgorithmRunnable rewriteRunnable;
    private IntentFilter broadcastFilter = new IntentFilter(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);

    public Intent register(Context context, Handler handler) {
        isRegistered = true;
        runnable = new ComputeAccelerationRunnable(handler, context);
        handler.postDelayed(runnable, runnable.DELAY);
        rewriteRunnable = new RewriteAccelerationRunnable(handler, context);
        handler.postDelayed(rewriteRunnable, rewriteRunnable.DELAY);
        return context.registerReceiver(this, broadcastFilter, null, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            context.unregisterReceiver(this);
            isRegistered = false;
            offsetDefined = false;
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Linear Receiver", "Received intent");
        ContentValues values = (ContentValues) intent.getExtras().get(LinearAccelerometer.EXTRA_DATA);
        if(values == null || values.size() == 0) return;

        double axisX = values.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0);
        double axisY = values.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1);
        double axisZ = values.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2);

        //TODO calibration step instead of first sensor data received
        if(!offsetDefined){
            defineOffsets(axisX, axisY, axisZ);
        }

        axisX -= offsetX;
        axisY -= offsetY;
        axisZ -= offsetZ;

        double acceleration = Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
        runnable.accumulateData(new TimestampedDouble(values.getAsLong(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP), acceleration));
    }

    private void defineOffsets(double x, double y , double z){
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        offsetDefined = true;
    }
}
