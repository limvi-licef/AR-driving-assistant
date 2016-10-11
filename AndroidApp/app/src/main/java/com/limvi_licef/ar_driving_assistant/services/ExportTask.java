package com.limvi_licef.ar_driving_assistant.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.limvi_licef.ar_driving_assistant.activities.MainActivity;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.database.Utils;

public class ExportTask extends AsyncTask<Void, Void, String> {

    private SQLiteDatabase db;
    private Context context;
    public ExportTask (Context context){
        db =  DatabaseHelper.getHelper(context).getWritableDatabase();
        this.context = context;
        Log.d("Insert Service", "Created Insert Task");
    }

    @Override
    protected String doInBackground(Void... params) {
        return Utils.exportDatabaseAsJSON(db) ? "Database Export Successful" : "Database Export Failure";
    }

    @Override
    protected void onPostExecute (String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

}