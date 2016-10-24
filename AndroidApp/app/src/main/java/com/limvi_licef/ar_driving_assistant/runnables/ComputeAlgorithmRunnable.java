package com.limvi_licef.ar_driving_assistant.runnables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.algorithms.MonotoneSegmentationAlgorithm;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Statistics;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import java.util.ArrayList;
import java.util.List;

public abstract class ComputeAlgorithmRunnable implements Runnable {

    private static final int TOLERANCE = 1;
    public final int DELAY = 1000 * 60;
    protected List<Structs.TimestampedDouble> data;
    protected Handler handler;
    protected SQLiteDatabase db;
    protected Context context;
    private String insertionStatus;

    ComputeAlgorithmRunnable(Handler handler, Context context) {
        this.data = new ArrayList<>();
        this.handler = handler;
        this.context = context;
        this.db = DatabaseHelper.getHelper(context).getWritableDatabase();
    }

    @Override
    public void run() {
        List<Structs.TimestampedDouble> newData = getData();
        Structs.SegmentationAlgorithmReturnData returnData = MonotoneSegmentationAlgorithm.computeData(newData, TOLERANCE);

        try{
            db.beginTransaction();
            saveData(returnData.monotoneValues,  returnData.extremaStats);
            db.setTransactionSuccessful();
            clearData(newData);
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_insert_success);
        }
        catch (IndexOutOfBoundsException e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_insert_empty_data);
        }
        catch (Exception e) {
            insertionStatus = getTableName() + " " + context.getResources().getString(R.string.database_insert_failure) + " " + e;
        }
        finally{
            db.endTransaction();

            Broadcasts.sendWriteToUIBroadcast(context, insertionStatus);
            handler.postDelayed(this, DELAY);
        }
    }

    public void clearData(List<Structs.TimestampedDouble> oldData){
        data.removeAll(oldData);
    }

    public void accumulateData(Structs.TimestampedDouble d){
        data.add(d);
    }

    private List<Structs.TimestampedDouble> getData() {
        return data;
    }

    protected abstract void saveData(List<Structs.TimestampedDouble> processedData, Structs.ExtremaStats extremaStats);

    protected abstract String getTableName();

}
