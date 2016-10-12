package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.aware.Rotation;
import com.aware.providers.Rotation_Provider;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

public class RotationReceiver extends BroadcastReceiver {

    public boolean isRegistered;
    private int screenRotation;
    private int axisX, axisY;
    private float[] rotationVector = new float[3];
    private float[] orientation = new float[3];
    private float[] rMat = new float[16];
    private float[] rMatRemap = new float[16];
    private IntentFilter broadcastFilter = new IntentFilter(Rotation.ACTION_AWARE_ROTATION);

    public Intent register(Context context, Handler handler) {
        setAxis(context);
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
        Log.d("Rotation Receiver", "Received intent");
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        ContentValues values = (ContentValues) intent.getExtras().get(Rotation.EXTRA_DATA);
        if(values == null || values.size() == 0) return;

        String idPref = context.getResources().getString(R.string.user_id_pref);
        SharedPreferences prefs = context.getSharedPreferences(Settings.USER_SHARED_PREFERENCES , Context.MODE_PRIVATE);
        String userId = prefs.getString(idPref, null);
        if(userId == null) return;

        ContentValues valuesToSave = new ContentValues();
        valuesToSave.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
        valuesToSave.put(DatabaseContract.RotationData.TIMESTAMP, values.getAsLong(Rotation_Provider.Rotation_Data.TIMESTAMP));
        valuesToSave.put(DatabaseContract.RotationData.AXIS_X, values.getAsDouble(Rotation_Provider.Rotation_Data.VALUES_0));
        valuesToSave.put(DatabaseContract.RotationData.AXIS_Y, values.getAsDouble(Rotation_Provider.Rotation_Data.VALUES_1));
        valuesToSave.put(DatabaseContract.RotationData.AXIS_Z, values.getAsDouble(Rotation_Provider.Rotation_Data.VALUES_2));

        //calculate azimuth
        rotationVector[0] = values.getAsFloat(Rotation_Provider.Rotation_Data.VALUES_0);
        rotationVector[1] = values.getAsFloat(Rotation_Provider.Rotation_Data.VALUES_1);
        rotationVector[2] = values.getAsFloat(Rotation_Provider.Rotation_Data.VALUES_2);

        SensorManager.getRotationMatrixFromVector(rMat, rotationVector);
        SensorManager.remapCoordinateSystem(rMat, axisX, axisY, rMatRemap);

        valuesToSave.put(DatabaseContract.RotationData.AZIMUTH, ( Math.toDegrees( SensorManager.getOrientation( rMatRemap, orientation )[0] ) + 360 ) % 360);
        Log.d("Rotate Azimuth", String.valueOf(( Math.toDegrees( orientation[0] ) + 360 ) % 360));
        Log.d("Rotate Pitch", String.valueOf(( Math.toDegrees( orientation[1] ) + 360 ) % 360));
        Log.d("Rotate Roll", String.valueOf(( Math.toDegrees( orientation[2] ) + 360 ) % 360));

        db.insert(DatabaseContract.RotationData.TABLE_NAME, null, valuesToSave);

        Intent localIntent = new Intent(Settings.ACTION_INSERT_DONE).putExtra(Settings.INSERT_STATUS, DatabaseContract.RotationData.TABLE_NAME + System.currentTimeMillis());
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
        Log.d("Rotation Receiver", "Finished insert");
    }

    private void setAxis(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenRotation = wm.getDefaultDisplay().getRotation();

        switch (screenRotation) {
            case Surface.ROTATION_0:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Y;
                break;

            case Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;

            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
                break;

            case Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;

            default:
                break;
        }
    }
}
