package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.aware.Accelerometer;
import com.aware.providers.Accelerometer_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

public class AccelerometerReceiver extends BroadcastReceiver {

    public boolean isRegistered;
    private IntentFilter broadcastFilter = new IntentFilter(Accelerometer.ACTION_AWARE_ACCELEROMETER);

    public Intent register(Context context, Handler handler) {
        isRegistered = true;
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
        Log.d("Accelerometer Receiver", "Received intent");
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        ContentValues values = (ContentValues) intent.getExtras().get(Accelerometer.EXTRA_DATA);
        if(values == null || values.size() == 0) return;

        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        String userId = prefs.getString(idPref, null);
        if(userId == null) return;

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(DatabaseContract.AccelerometerData.CURRENT_USER_ID, userId);
        valuesToSave.put(DatabaseContract.AccelerometerData.TIMESTAMP, values.getAsLong(Accelerometer_Provider.Accelerometer_Data.TIMESTAMP));
        valuesToSave.put(DatabaseContract.AccelerometerData.AXIS_X, values.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
        valuesToSave.put(DatabaseContract.AccelerometerData.AXIS_Y, values.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1));
        valuesToSave.put(DatabaseContract.AccelerometerData.AXIS_Z, values.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2));

        db.insert(DatabaseContract.AccelerometerData.TABLE_NAME, null, valuesToSave);

        Log.d("Accelerometer Receiver", "Finished insert");
    }
}
