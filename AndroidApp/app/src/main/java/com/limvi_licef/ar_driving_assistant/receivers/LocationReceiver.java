package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.aware.Locations;
import com.aware.providers.Locations_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.Utils;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.tasks.ComputeSpeedRunnable;

public class LocationReceiver extends BroadcastReceiver {

    public boolean isRegistered;

    private ComputeSpeedRunnable runnable;

    private static final String broadcastAction = "ACTION_AWARE_LOCATIONS";
    private static final String extraData = "data";
    private IntentFilter broadcastFilter = new IntentFilter(Locations.ACTION_AWARE_LOCATIONS);

    public Intent register(Context context, Handler handler) {
        isRegistered = true;
        runnable = new ComputeSpeedRunnable(handler, context);
        runnable.startRunnable();
        return context.registerReceiver(this, broadcastFilter, null, handler);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            context.unregisterReceiver(this);
            runnable.stopRunnable();
            runnable.clearData();
            isRegistered = false;
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Location Receiver", "Received intent");
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        //get most recent
        Cursor location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, "timestamp DESC LIMIT 1");
        location.moveToFirst();

        runnable.accumulateData(new Utils.TimestampedDouble(location.getLong(location.getColumnIndex(Locations_Provider.Locations_Data.TIMESTAMP)),
                location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.SPEED))));

        String userId = Utils.getCurrentUserId(context);

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(DatabaseContract.LocationData.CURRENT_USER_ID, userId);
        valuesToSave.put(DatabaseContract.LocationData.TIMESTAMP, location.getLong(location.getColumnIndex(Locations_Provider.Locations_Data.TIMESTAMP)));
        valuesToSave.put(DatabaseContract.LocationData.LATITUDE, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.LONGITUDE, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.ALTITUDE, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.ALTITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.BEARING, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.BEARING)));
        valuesToSave.put(DatabaseContract.LocationData.ACCURACY, location.getInt(location.getColumnIndex(Locations_Provider.Locations_Data.ACCURACY)));

        db.insert(DatabaseContract.LocationData.TABLE_NAME, null, valuesToSave);
        location.close();

        Intent localIntent = new Intent(Settings.ACTION_INSERT_DONE).putExtra(Settings.INSERT_STATUS, DatabaseContract.LocationData.TABLE_NAME + " " + R.string.database_insert_success);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

}
