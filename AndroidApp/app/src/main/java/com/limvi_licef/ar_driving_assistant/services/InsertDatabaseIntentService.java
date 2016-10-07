package com.limvi_licef.ar_driving_assistant.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

public class InsertDatabaseIntentService extends IntentService {

    private SQLiteDatabase db;
    public static String TABLE_NAME = "table_name";
    public static String VALUES = "values";

    public InsertDatabaseIntentService() {
        super("InsertDatabaseIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = DatabaseHelper.getHelper(this).getWritableDatabase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        Log.d("Insert Service", "Started Insert Service");

        return START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Insert Service", "Started Intent Handling");
        String tableName = (String) intent.getExtras().get(TABLE_NAME);
        ContentValues values = (ContentValues) intent.getExtras().get(VALUES);
        db.insert(tableName, null, values);
        Log.d("Insert Service Values", values.toString());
    }

}
