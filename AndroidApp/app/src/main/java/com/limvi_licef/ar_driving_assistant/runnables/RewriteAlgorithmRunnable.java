package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class RewriteAlgorithmRunnable implements Runnable {
    public final int DELAY = 1000 * 60 * 10;
    protected static final int REWRITE_MINUTES = 10;
    protected static final int TOLERANCE = 1;
    protected Handler handler;
    protected SQLiteDatabase db;
    protected Context context;
    protected String insertionStatus;

    public RewriteAlgorithmRunnable(Handler handler, Context context) {
        this.handler = handler;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
        this.context = context;
    }

    @Override
    public void run() {
        String userId = Preferences.getCurrentUserId(context);
        long now = System.currentTimeMillis();
        long nowMinusMinutes = now - TimeUnit.MINUTES.toMillis(REWRITE_MINUTES);

        List<Structs.TimestampedDouble> newData = getData(nowMinusMinutes, now);
        Structs.SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(newData, TOLERANCE);

        try{
            db.beginTransaction();
            deleteData(nowMinusMinutes, now, userId);
            saveData(returnData.monotoneValues, returnData.extremaStats, userId);
            db.setTransactionSuccessful();
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_rewrite_success);
        }
        catch (IndexOutOfBoundsException e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_rewrite_empty_data);
        }
        catch (Exception e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_rewrite_failure) + " " + e;
        }
        finally{
            db.endTransaction();

            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            handler.postDelayed(this, DELAY);
        }
    }

    protected abstract void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats, String userId);

    protected abstract List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp);

    protected abstract void deleteData(long fromTimestamp, long toTimestamp, String userId);

    protected abstract String getTableName();
}
