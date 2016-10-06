package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;

import com.aware.Locations;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Locations_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.services.InsertDatabaseIntentService;

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
        //get most recent
        Cursor location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, "timestamp DESC LIMIT 1");
        location.moveToFirst();

        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        String userId = prefs.getString(idPref, null);
        if(userId == null) {
            location.close();
            return;
        }

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(DatabaseContract.LocationData.CURRENT_USER_ID, userId);
        valuesToSave.put(DatabaseContract.LocationData.TIMESTAMP, location.getString(location.getColumnIndex(Locations_Provider.Locations_Data.TIMESTAMP)));
        valuesToSave.put(DatabaseContract.LocationData.LATITUDE, location.getString(location.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.LONGITUDE, location.getString(location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.ALTITUDE, location.getString(location.getColumnIndex(Locations_Provider.Locations_Data.ALTITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.SPEED, location.getString(location.getColumnIndex(Locations_Provider.Locations_Data.SPEED)));
        valuesToSave.put(DatabaseContract.LocationData.BEARING, location.getString(location.getColumnIndex(Locations_Provider.Locations_Data.BEARING)));
        valuesToSave.put(DatabaseContract.LocationData.ACCURACY, location.getString(location.getColumnIndex(Locations_Provider.Locations_Data.ACCURACY)));

        Intent insertIntent = new Intent(context, InsertDatabaseIntentService.class);
        insertIntent.putExtra(InsertDatabaseIntentService.TABLE_NAME, DatabaseContract.LocationData.TABLE_NAME);
        insertIntent.putExtra(InsertDatabaseIntentService.VALUES, valuesToSave);
        context.startService(insertIntent);

        location.close();
    }

}
