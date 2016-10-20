package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Structs;
import com.limvi_licef.ar_driving_assistant.utils.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RewriteAzimuthRunnable implements Runnable {

    public final int DELAY = 1000 * 60 * 10;
    private static final int REWRITE_MINUTES = 10;
    private static final int TOLERANCE = 0;
    private static final String WHERE_CLAUSE = DatabaseContract.RotationData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.RotationData.TIMESTAMP + " BETWEEN ? AND ?";

    private String insertionStatus;
    private Handler handler;
    private SQLiteDatabase db;
    private Context context;

    public RewriteAzimuthRunnable(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        try{
            String userId = User.getCurrentUserId(context);
            long now = System.currentTimeMillis();
            long nowMinusMinutes = now - TimeUnit.MINUTES.toMillis(REWRITE_MINUTES);

            List<Structs.TimestampedDouble> processedData = MonotoneSegmentationAlgorithm.computeData(getData(nowMinusMinutes, now, userId), TOLERANCE).monotoneValues;

            db.beginTransaction();
            deleteData(nowMinusMinutes, now, userId);
            saveData(processedData, userId);
            db.setTransactionSuccessful();
            insertionStatus = DatabaseContract.RotationData.TABLE_NAME + " " + context.getResources().getString(R.string.database_rewrite_success);
        }
        catch (Exception e) {
            insertionStatus = DatabaseContract.RotationData.TABLE_NAME + " " + context.getResources().getString(R.string.database_rewrite_failure);
            Log.d("Runnable Exception", "" + e);
        }
        finally{
            db.endTransaction();

            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            handler.postDelayed(this, DELAY);
        }
    }

    private void saveData(List<Structs.TimestampedDouble> processedData, String userId) {
        for(Structs.TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.RotationData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.RotationData.TIMESTAMP, td.timestamp);
            values.put(DatabaseContract.RotationData.AZIMUTH, td.value);
            db.insert(DatabaseContract.RotationData.TABLE_NAME, null, values);
        }
    }

    private List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp, String userId) {
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

    private void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.RotationData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

}
