package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.ArrayList;
import java.util.List;

public class RewriteAzimuthRunnable extends RewriteAlgorithmRunnable {

    private static final String WHERE_CLAUSE = DatabaseContract.RotationData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.RotationData.TIMESTAMP + " BETWEEN ? AND ?";

    public RewriteAzimuthRunnable(Handler handler, Context context) {
       super(handler, context);
    }

    @Override
    protected void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats, String userId) {
        for(Structs.TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.RotationData.TIMESTAMP, td.timestamp);
            values.put(DatabaseContract.RotationData.AZIMUTH, td.value);
            db.insert(DatabaseContract.RotationData.TABLE_NAME, null, values);
        }
    }

    @Override
    protected List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String userId) {
        List<Structs.TimestampedDouble> data = new ArrayList<>();
        Cursor azimuthCursor = db.query(DatabaseContract.RotationData.TABLE_NAME,
                new String[]{DatabaseContract.RotationData.CURRENT_USER_ID, DatabaseContract.RotationData.TIMESTAMP,DatabaseContract.RotationData.AZIMUTH},
                WHERE_CLAUSE,
                new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)}, null, null, "Timestamp ASC");
        int timestampColumnIndex = azimuthCursor.getColumnIndexOrThrow(DatabaseContract.RotationData.TIMESTAMP);
        int speedColumnIndex = azimuthCursor.getColumnIndexOrThrow(DatabaseContract.RotationData.AZIMUTH);
        while (azimuthCursor.moveToNext()) {
            data.add(new Structs.TimestampedDouble(azimuthCursor.getLong(timestampColumnIndex), azimuthCursor.getDouble(speedColumnIndex)));
        }
        azimuthCursor.close();
        return data;
    }

    @Override
    protected void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.RotationData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.RotationData.TABLE_NAME;
    }
}
