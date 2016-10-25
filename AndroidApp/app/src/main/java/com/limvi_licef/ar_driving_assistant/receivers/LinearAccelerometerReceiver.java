package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.aware.LinearAccelerometer;
import com.aware.providers.Linear_Accelerometer_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAccelerationRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAccelerationRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.tasks.CalibrateTask;
import com.limvi_licef.ar_driving_assistant.utils.Constants;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

public class LinearAccelerometerReceiver extends BroadcastReceiver implements SensorReceiver {

    public boolean isRegistered;
    private double offsetX;
    private double offsetY;
    private double offsetZ;
    private ComputeAlgorithmRunnable runnable;
    private RewriteAlgorithmRunnable rewriteRunnable;
    private IntentFilter broadcastFilter = new IntentFilter(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);

    public void register(Context context, Handler handler) {
        if(!getOffsets(context)) return;
        isRegistered = true;
        runnable = new ComputeAccelerationRunnable(handler, context);
        handler.postDelayed(runnable, runnable.DELAY);
        rewriteRunnable = new RewriteAccelerationRunnable(handler, context);
        handler.postDelayed(rewriteRunnable, rewriteRunnable.DELAY);
        context.registerReceiver(this, broadcastFilter, null, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            runnable.run();
            context.unregisterReceiver(this);
            isRegistered = false;
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

        axisX -= offsetX;
        axisY -= offsetY;
        axisZ -= offsetZ;

        double acceleration = Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
        Log.d("Acceleration data", "" + acceleration);
        runnable.accumulateData(new TimestampedDouble(values.getAsLong(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP), acceleration));
    }

    private boolean getOffsets(Context context){
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        offsetX = Preferences.getDouble(settings, Constants.OFFSET_X_PREF, -1);
        offsetY = Preferences.getDouble(settings, Constants.OFFSET_Y_PREF, -1);
        offsetZ = Preferences.getDouble(settings, Constants.OFFSET_Z_PREF, -1);
        if(offsetX == -1 || offsetY == -1 || offsetZ == -1){
            Toast t = Toast.makeText(context, context.getResources().getString(R.string.calibrate_acceleration_error), Toast.LENGTH_LONG);
            t.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
            t.show();
            return false;
        }
        return true;
    }
}
