package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.aware.Accelerometer;

public class AccelerometerReceiver extends BroadcastReceiver {

    public boolean isRegistered;
    private IntentFilter broadcastFilter = new IntentFilter(Accelerometer.ACTION_AWARE_ACCELEROMETER);

    public Intent register(Context context) {
        isRegistered = true;
        return context.registerReceiver(this, broadcastFilter);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            context.unregisterReceiver(this);
            isRegistered = false;
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ContentValues values = (ContentValues) intent.getExtras().get(Accelerometer.EXTRA_DATA);
        if(values == null || values.size() == 0) return;
        //TODO
    }
}
