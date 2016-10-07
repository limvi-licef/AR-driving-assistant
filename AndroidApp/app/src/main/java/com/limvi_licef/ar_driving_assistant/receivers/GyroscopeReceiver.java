package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;

import com.aware.Gyroscope;
import com.aware.providers.Gyroscope_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.services.InsertDatabaseIntentService;
import com.limvi_licef.ar_driving_assistant.services.InsertTask;

public class GyroscopeReceiver extends BroadcastReceiver {

    public boolean isRegistered;
    private IntentFilter broadcastFilter = new IntentFilter(Gyroscope.ACTION_AWARE_GYROSCOPE);

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
        ContentValues values = (ContentValues) intent.getExtras().get(Gyroscope.EXTRA_DATA);
        if(values == null || values.size() == 0) return;

        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        String userId = prefs.getString(idPref, null);
        if(userId == null) return;

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(DatabaseContract.GyroscopeData.CURRENT_USER_ID, userId);
        valuesToSave.put(DatabaseContract.GyroscopeData.TIMESTAMP, values.getAsLong(Gyroscope_Provider.Gyroscope_Data.TIMESTAMP));
        valuesToSave.put(DatabaseContract.GyroscopeData.AXIS_X, values.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0));
        valuesToSave.put(DatabaseContract.GyroscopeData.AXIS_Y, values.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1));
        valuesToSave.put(DatabaseContract.GyroscopeData.AXIS_Z, values.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2));

//        Intent insertIntent = new Intent(context, InsertDatabaseIntentService.class);
//        insertIntent.putExtra(InsertDatabaseIntentService.TABLE_NAME, DatabaseContract.GyroscopeData.TABLE_NAME);
//        insertIntent.putExtra(InsertDatabaseIntentService.VALUES, valuesToSave);
//        context.startService(insertIntent);
        new InsertTask(context).execute(DatabaseContract.GyroscopeData.TABLE_NAME, valuesToSave);
    }
}
