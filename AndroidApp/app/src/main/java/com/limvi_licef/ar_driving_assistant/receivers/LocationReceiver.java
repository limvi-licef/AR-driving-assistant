package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.aware.Locations;
import com.aware.providers.Locations_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.ComputeSpeedRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteAlgorithmRunnable;
import com.limvi_licef.ar_driving_assistant.runnables.RewriteSpeedRunnable;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.utils.User;

public class LocationReceiver extends BroadcastReceiver {

    public boolean isRegistered;

    private ComputeAlgorithmRunnable runnable;
    private RewriteAlgorithmRunnable rewriteRunnable;

    private static final String broadcastAction = "ACTION_AWARE_LOCATIONS";
    private static final String extraData = "data";
    private IntentFilter broadcastFilter = new IntentFilter(Locations.ACTION_AWARE_LOCATIONS);

    public Intent register(Context context, Handler handler) {
        isRegistered = true;
        runnable = new ComputeSpeedRunnable(handler, context);
        handler.postDelayed(runnable, runnable.DELAY);
        rewriteRunnable = new RewriteSpeedRunnable(handler, context);
        handler.postDelayed(rewriteRunnable, rewriteRunnable.DELAY);
        return context.registerReceiver(this, broadcastFilter, null, handler);
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
        Log.d("Location Receiver", "Received intent");
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        //get most recent
        Cursor location = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, "timestamp DESC LIMIT 1");
        location.moveToFirst();

        runnable.accumulateData(new TimestampedDouble(location.getLong(location.getColumnIndex(Locations_Provider.Locations_Data.TIMESTAMP)),
                location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.SPEED))));

        String userId = User.getCurrentUserId(context);

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(DatabaseContract.LocationData.CURRENT_USER_ID, userId);
        valuesToSave.put(DatabaseContract.LocationData.TIMESTAMP, location.getLong(location.getColumnIndex(Locations_Provider.Locations_Data.TIMESTAMP)));
        valuesToSave.put(DatabaseContract.LocationData.LATITUDE, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.LONGITUDE, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.ALTITUDE, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.ALTITUDE)));
        valuesToSave.put(DatabaseContract.LocationData.BEARING, location.getDouble(location.getColumnIndex(Locations_Provider.Locations_Data.BEARING)));
        valuesToSave.put(DatabaseContract.LocationData.ACCURACY, location.getInt(location.getColumnIndex(Locations_Provider.Locations_Data.ACCURACY)));

        boolean success = db.insert(DatabaseContract.LocationData.TABLE_NAME, null, valuesToSave) != -1L;
        location.close();

        Broadcasts.sendWriteToUIBroadcast(context, DatabaseContract.LocationData.TABLE_NAME + " " +
                (success ? context.getResources().getString(R.string.database_insert_success) : context.getResources().getString(R.string.database_insert_failure)));
    }

}
