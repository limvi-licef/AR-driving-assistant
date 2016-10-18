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
import com.limvi_licef.ar_driving_assistant.Utils;
import com.limvi_licef.ar_driving_assistant.tasks.ComputeAccelerationRunnable;

public class LinearAccelerometerReceiver extends BroadcastReceiver {

    public boolean isRegistered;
    private ComputeAccelerationRunnable runnable;
    private IntentFilter broadcastFilter = new IntentFilter(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);

    public Intent register(Context context, Handler handler) {
        isRegistered = true;
        runnable = new ComputeAccelerationRunnable(handler, context);
        runnable.startRunnable();
        return context.registerReceiver(this, broadcastFilter, null, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            context.unregisterReceiver(this);
            isRegistered = false;
            runnable.stopRunnable();
            runnable.clearData();
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
        double acceleration = Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
        runnable.accumulateData(new Utils.TimestampedDouble(values.getAsLong(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP), acceleration));
    }
}
