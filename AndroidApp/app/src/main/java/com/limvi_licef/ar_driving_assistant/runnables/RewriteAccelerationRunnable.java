package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Database;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.List;

public class RewriteAccelerationRunnable extends RewriteAlgorithmRunnable {

    private static final String WHERE_CLAUSE = DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerData.TIMESTAMP + " BETWEEN ? AND ?";
    private static final String WHERE_CLAUSE_STATS = DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID + " = ? AND " + DatabaseContract.LinearAccelerometerStats.AXIS_NAME + " = ? AND " + DatabaseContract.LinearAccelerometerStats.START_TIMESTAMP + " >= ? AND " + DatabaseContract.LinearAccelerometerStats.END_TIMESTAMP + " <= ?";
    private String currentAxis;

    public RewriteAccelerationRunnable(Handler handler, Context context) {
        super(handler, context);
    }

    //TODO REFACTOR RewriteAlgorithm instead of overriding run() here and using setCurrentAxis
    /**
     * Override RewriteAlgorithmRunnable's run method in order to sync the three axes together,
     * especially in case of insert and delete error on a single axis
     */
    @Override
    public void run() {
        String userId = Preferences.getCurrentUserId(context);
        long now = System.currentTimeMillis();
        long nowMinusMinutes = now - Config.SensorDataCollection.LONG_DELAY;

        setCurrentAxis(DatabaseContract.LinearAccelerometerData.AXIS_X);
        List<Structs.TimestampedDouble> newData = getData(nowMinusMinutes, now);
        Structs.SegmentationAlgorithmReturnData dataAxisX = MonotoneSegmentationAlgorithm.computeData(newData, Config.SensorDataCollection.MONOTONE_SEGMENTATION_TOLERANCE);
        setCurrentAxis(DatabaseContract.LinearAccelerometerData.AXIS_Y);
        newData = getData(nowMinusMinutes, now);
        Structs.SegmentationAlgorithmReturnData dataAxisY = MonotoneSegmentationAlgorithm.computeData(newData, Config.SensorDataCollection.MONOTONE_SEGMENTATION_TOLERANCE);
        setCurrentAxis(DatabaseContract.LinearAccelerometerData.AXIS_Z);
        newData = getData(nowMinusMinutes, now);
        Structs.SegmentationAlgorithmReturnData dataAxisZ = MonotoneSegmentationAlgorithm.computeData(newData, Config.SensorDataCollection.MONOTONE_SEGMENTATION_TOLERANCE);

        try{
            db.beginTransaction();
            deleteData(nowMinusMinutes, now, userId);
            setCurrentAxis(DatabaseContract.LinearAccelerometerData.AXIS_X);
            saveData(dataAxisX.monotoneValues, dataAxisX.extremaStats, userId);
            setCurrentAxis(DatabaseContract.LinearAccelerometerData.AXIS_Y);
            saveData(dataAxisY.monotoneValues, dataAxisY.extremaStats, userId);
            setCurrentAxis(DatabaseContract.LinearAccelerometerData.AXIS_Z);
            saveData(dataAxisZ.monotoneValues, dataAxisZ.extremaStats, userId);
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
            handler.postDelayed(this, Config.SensorDataCollection.LONG_DELAY);
        }
    }

    @Override
    protected void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats, String userId) {
        long firstTimestamp = processedData.get(0).timestamp;
        long lastTimestamp = processedData.get(processedData.size()-1).timestamp;

        for(Structs.TimestampedDouble td : processedData) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.LinearAccelerometerData.CURRENT_USER_ID, userId);
            values.put(DatabaseContract.LinearAccelerometerData.TIMESTAMP, td.timestamp);
            values.put(getCurrentAxis(), td.value);

            //Create new row if none exist for this timestamp
            int id = (int) db.insertWithOnConflict(DatabaseContract.LinearAccelerometerData.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                //if row exists, add axis data to correct column
                db.update(DatabaseContract.LinearAccelerometerData.TABLE_NAME, values, "Timestamp=?", new String[] {String.valueOf(td.timestamp)});
            }
        }

        ContentValues stats = new ContentValues();
        stats.put(DatabaseContract.LinearAccelerometerStats.CURRENT_USER_ID, userId);
        stats.put(DatabaseContract.LinearAccelerometerStats.AXIS_NAME, getCurrentAxis());
        stats.put(DatabaseContract.LinearAccelerometerStats.START_TIMESTAMP, firstTimestamp);
        stats.put(DatabaseContract.LinearAccelerometerStats.END_TIMESTAMP, lastTimestamp);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_AVERAGE, extremaStats.positiveAverage);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_STD_DEVIATION, extremaStats.positiveStandardDeviation);
        stats.put(DatabaseContract.LinearAccelerometerStats.ACCEL_COUNT, extremaStats.positiveCount);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_COUNT, extremaStats.negativeCount);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_AVERAGE, extremaStats.negativeAverage);
        stats.put(DatabaseContract.LinearAccelerometerStats.DECEL_STD_DEVIATION, extremaStats.negativeStandardDeviation);
        db.insert(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, null, stats);
    }

    @Override
    protected List<Structs.TimestampedDouble> getData(long fromTimestamp, long toTimestamp) {
        return Database.getSensorData(fromTimestamp, toTimestamp, DatabaseContract.LinearAccelerometerData.TABLE_NAME, getCurrentAxis(), context);
    }

    @Override
    protected void deleteData(long fromTimestamp, long toTimestamp, String userId) {
        db.delete(DatabaseContract.LinearAccelerometerData.TABLE_NAME, WHERE_CLAUSE, new String[]{userId, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, WHERE_CLAUSE_STATS, new String[]{userId, DatabaseContract.LinearAccelerometerData.AXIS_X, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, WHERE_CLAUSE_STATS, new String[]{userId, DatabaseContract.LinearAccelerometerData.AXIS_Y, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
        db.delete(DatabaseContract.LinearAccelerometerStats.TABLE_NAME, WHERE_CLAUSE_STATS, new String[]{userId, DatabaseContract.LinearAccelerometerData.AXIS_Z, String.valueOf(fromTimestamp), String.valueOf(toTimestamp)});
    }

    @Override
    protected String getTableName(){
        return DatabaseContract.LinearAccelerometerData.TABLE_NAME;
    }

    private void setCurrentAxis(String currentAxis){
        this.currentAxis = currentAxis;
    }

    private String getCurrentAxis(){
        return currentAxis;
    }
}
