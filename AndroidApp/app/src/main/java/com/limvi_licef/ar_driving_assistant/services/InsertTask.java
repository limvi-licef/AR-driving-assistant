package com.limvi_licef.ar_driving_assistant.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;

public class InsertTask extends AsyncTask<Object, Void, Void> {

    private SQLiteDatabase db;
    public InsertTask (Context context){
       db =  DatabaseHelper.getHelper(context).getWritableDatabase();
        Log.d("Insert Service", "Created Insert Task");
    }

    @Override
    protected Void doInBackground(Object... params) {
        Log.d("Insert Service", "Started Intent Task");
        String tableName = (String) params[0];
        ContentValues values = (ContentValues) params[1];
        db.insert(tableName, null, values);
        Log.d("Insert Task Values", values.toString());
        return null;
    }

}