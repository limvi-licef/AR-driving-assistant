package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs.ExtremaStats;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

import java.util.List;

public class ComputeAzimuthRunnable extends ComputeAlgorithmRunnable {

    public ComputeAzimuthRunnable(Handler handler, Context context) {
        super(handler, context);
    }

    @Override
    protected void saveData(List<TimestampedDouble> processedData, ExtremaStats extremaStats) {
        String userId = Preferences.getCurrentUserId(context);
        for(TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.RotationData.TIMESTAMP, td.timestamp);
            values.put(DatabaseContract.RotationData.AZIMUTH, td.value);
            db.insert(DatabaseContract.RotationData.TABLE_NAME, null, values);
        }
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.RotationData.TABLE_NAME;
    }
}
