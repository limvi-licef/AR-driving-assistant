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
import com.limvi_licef.ar_driving_assistant.config.SensorDataCollection;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAccelerationRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAccelerationRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Statistics;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;

public class LinearAccelerometerReceiver extends BroadcastReceiver implements SensorReceiver {

    public boolean isRegistered;
    private double offsetX;
    private double offsetY;
    private double offsetZ;
    private ComputeAlgorithmRunnable runnableAxisX;
    private ComputeAlgorithmRunnable runnableAxisY;
    private ComputeAlgorithmRunnable runnableAxisZ;
    private RewriteAlgorithmRunnable rewriteRunnable;
    private IntentFilter broadcastFilter = new IntentFilter(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);
    private long previousTimestamp = 0;

    public void register(Context context, Handler handler) {
        if(!getOffsets(context)) return;
        isRegistered = true;

        //Create a runnable to handle each axis separately
        runnableAxisX = new ComputeAccelerationRunnable(handler, context, DatabaseContract.LinearAccelerometerData.AXIS_X);
        handler.postDelayed(runnableAxisX, SensorDataCollection.SHORT_DELAY);
        runnableAxisY = new ComputeAccelerationRunnable(handler, context, DatabaseContract.LinearAccelerometerData.AXIS_Y);
        handler.postDelayed(runnableAxisY, SensorDataCollection.SHORT_DELAY);
        runnableAxisZ = new ComputeAccelerationRunnable(handler, context, DatabaseContract.LinearAccelerometerData.AXIS_Z);
        handler.postDelayed(runnableAxisZ, SensorDataCollection.SHORT_DELAY);
        rewriteRunnable = new RewriteAccelerationRunnable(handler, context);
        handler.postDelayed(rewriteRunnable, SensorDataCollection.LONG_DELAY);
        context.registerReceiver(this, broadcastFilter, null, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            savePrematurely();
            context.unregisterReceiver(this);
            isRegistered = false;
            return true;
        }
        return false;
    }

    public void savePrematurely(){
        if(!runnableAxisX.isRunning()) runnableAxisX.run();
        if(!runnableAxisY.isRunning()) runnableAxisY.run();
        if(!runnableAxisZ.isRunning()) runnableAxisZ.run();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(SensorDataCollection.LOGGING_ENABLED) {
            Broadcasts.sendWriteToUIBroadcast(context, "Received Linear Accelerometer Data");
        }
        if(System.currentTimeMillis() - previousTimestamp <= SensorDataCollection.MINIMUM_DELAY) return;
        ContentValues values = (ContentValues) intent.getExtras().get(LinearAccelerometer.EXTRA_DATA);
        if(values == null || values.size() == 0) return;

        //Get data
        double axisX = values.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0);
        double axisY = values.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1);
        double axisZ = values.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2);

        //apply offset
        axisX -= offsetX;
        axisY -= offsetY;
        axisZ -= offsetZ;

        //Round off timestamp avoid clutter
        long roundedTimestamp = Statistics.roundOffTimestamp(values.getAsLong(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP), SensorDataCollection.ACCELERATION_PRECISION);

        //Accumulate data
        runnableAxisX.accumulateData(new TimestampedDouble(roundedTimestamp, axisX));
        runnableAxisY.accumulateData(new TimestampedDouble(roundedTimestamp, axisY));
        runnableAxisZ.accumulateData(new TimestampedDouble(roundedTimestamp, axisZ));
        previousTimestamp = System.currentTimeMillis();
    }

    /**
     * Gets the axis offsets from SharedPreferences
     * @param context
     * @return Whether or not the Accelerometer has been calibrated
     */
    private boolean getOffsets(Context context){
        SharedPreferences settings = context.getSharedPreferences(Preferences.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        offsetX = Preferences.getDouble(settings, Preferences.OFFSET_X_PREF, -1);
        offsetY = Preferences.getDouble(settings, Preferences.OFFSET_Y_PREF, -1);
        offsetZ = Preferences.getDouble(settings, Preferences.OFFSET_Z_PREF, -1);
        if(offsetX == -1 || offsetY == -1 || offsetZ == -1){
            Toast t = Toast.makeText(context, context.getResources().getString(R.string.calibrate_acceleration_error), Toast.LENGTH_LONG);
            t.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
            t.show();
            return false;
        }
        return true;
    }
}
