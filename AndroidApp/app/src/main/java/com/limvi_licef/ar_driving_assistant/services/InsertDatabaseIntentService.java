package com.limvi_licef.ar_driving_assistant.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

public class InsertDatabaseIntentService extends IntentService {

    private DatabaseHelper dbHelper;
    public static String TABLE_NAME = "table_name";
    public static String VALUES = "values";

    public InsertDatabaseIntentService() {
        super("InsertDatabaseIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = DatabaseHelper.getHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String tableName = (String) intent.getExtras().get(TABLE_NAME);
        ContentValues values = (ContentValues) intent.getExtras().get(VALUES);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(tableName, null, values);
    }

}
