package com.limvi_licef.ar_driving_assistant.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Database;

public class ExportTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog dialog;
    private SQLiteDatabase db;
    private Context context;

    public ExportTask (Context context){
        db =  DatabaseHelper.getHelper(context).getWritableDatabase();
        dialog = new ProgressDialog(context);
        this.context = context;
        Log.d("Insert Service", "Created Insert Task");
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Exporting...");
        this.dialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        return Database.exportDatabaseAsJSON(db) ? "Database Export Successful" : "Database Export Failure";
    }

    @Override
    protected void onPostExecute (String result) {
        if (dialog.isShowing()) { dialog.dismiss(); }
        new AlertDialog.Builder(context)
                .setMessage(result)
                .setNegativeButton("Close", null)
                .show();
    }

}