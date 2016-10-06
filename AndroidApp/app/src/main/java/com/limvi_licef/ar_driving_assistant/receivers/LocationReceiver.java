package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;

import com.aware.Locations;
import com.aware.providers.Locations_Provider;

import static android.content.Context.LOCATION_SERVICE;

public class LocationReceiver extends BroadcastReceiver {

    public boolean isRegistered;

    private static final String broadcastAction = "ACTION_AWARE_LOCATIONS";
    private static final String extraData = "data";
    private IntentFilter broadcastFilter = new IntentFilter(Locations.ACTION_AWARE_LOCATIONS);

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
        Cursor location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, "timestamp DESC LIMIT 1");
        location.moveToFirst();
        //TODO
    }

}
