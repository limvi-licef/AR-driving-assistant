package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.services.InsertDatabaseIntentService;
import com.limvi_licef.ar_driving_assistant.services.InsertTask;

public class TemperatureReceiver extends BroadcastReceiver {

    public boolean isRegistered;

    private static final String broadcastAction = "ACTION_AWARE_PLUGIN_OPENWEATHER";
    private static final String extraData = "openweather";
    private IntentFilter broadcastFilter = new IntentFilter(broadcastAction);

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
        ContentValues values = (ContentValues) intent.getExtras().get(extraData);
        if(values == null || values.size() == 0) return;

        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        String userId = prefs.getString(idPref, null);
        if(userId == null) return;

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(DatabaseContract.TemperatureData.CURRENT_USER_ID, userId);
        valuesToSave.put(DatabaseContract.TemperatureData.TIMESTAMP, values.getAsLong("timestamp"));
        valuesToSave.put(DatabaseContract.TemperatureData.CITY, values.getAsString("city"));
        valuesToSave.put(DatabaseContract.TemperatureData.TEMPERATURE, values.getAsDouble("temperature"));
        valuesToSave.put(DatabaseContract.TemperatureData.WIND_SPEED, values.getAsDouble("wind_speed"));
        valuesToSave.put(DatabaseContract.TemperatureData.WIND_DIRECTION, values.getAsDouble("wind_degrees"));
        valuesToSave.put(DatabaseContract.TemperatureData.RAIN, values.getAsDouble("rain"));
        valuesToSave.put(DatabaseContract.TemperatureData.SNOW, values.getAsDouble("snow"));
        valuesToSave.put(DatabaseContract.TemperatureData.CLOUDINESS, values.getAsDouble("cloudiness"));

//        Intent insertIntent = new Intent(context, InsertDatabaseIntentService.class);
//        insertIntent.putExtra(InsertDatabaseIntentService.TABLE_NAME, DatabaseContract.TemperatureData.TABLE_NAME);
//        insertIntent.putExtra(InsertDatabaseIntentService.VALUES, valuesToSave);
//        context.startService(insertIntent);
        new InsertTask(context).execute(DatabaseContract.TemperatureData.TABLE_NAME, valuesToSave);
    }
}
