package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.User;

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
        Log.d("Temperature Receiver", "Received intent");
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        ContentValues values = (ContentValues) intent.getExtras().get(extraData);
        if(values == null || values.size() == 0) return;

        String userId = User.getCurrentUserId(context);

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

        boolean success = db.insert(DatabaseContract.TemperatureData.TABLE_NAME, null, valuesToSave) != -1L;

        Broadcasts.sendWriteToUIBroadcast(context, DatabaseContract.TemperatureData.TABLE_NAME + " " +
                (success ? context.getResources().getString(R.string.database_insert_success) : context.getResources().getString(R.string.database_insert_failure)));
    }
}
