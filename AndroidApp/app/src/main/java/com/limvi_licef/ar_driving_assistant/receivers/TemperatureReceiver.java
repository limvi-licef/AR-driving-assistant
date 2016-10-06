package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class TemperatureReceiver extends BroadcastReceiver {

    public boolean isRegistered;

    private static final String broadcastAction = "ACTION_AWARE_PLUGIN_OPENWEATHER";
    private static final String extraData = "openweather";
    private IntentFilter broadcastFilter = new IntentFilter(broadcastAction);

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
        ContentValues values = (ContentValues) intent.getExtras().get(extraData);
        if(values == null || values.size() == 0) return;
        //TODO
    }
}
