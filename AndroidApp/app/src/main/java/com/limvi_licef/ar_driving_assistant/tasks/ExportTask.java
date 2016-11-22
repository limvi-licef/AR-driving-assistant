package com.limvi_licef.ar_driving_assistant.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;

/**
 * Export the entire database as a json file
 * The device needs to be rebooted for the file to be visible
 */
public class ExportTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog dialog;
    private SQLiteDatabase db;
    private Context context;

    public ExportTask (Context context){
        db =  DatabaseHelper.getHelper(context).getWritableDatabase();
        dialog = new ProgressDialog(context);
        this.context = context;
        Log.d("AsyncTask", "Created Insert Task");
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage(context.getResources().getString(R.string.export_task_message));
        this.dialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        return exportDatabaseAsJSON(db) ?
                context.getResources().getString(R.string.export_task_success) :
                context.getResources().getString(R.string.export_task_failure);
    }

    @Override
    protected void onPostExecute (String result) {
        if (dialog.isShowing()) { dialog.dismiss(); }
        //display alert when done
        new AlertDialog.Builder(context)
                .setMessage(result)
                .setNegativeButton(context.getResources().getString(R.string.export_task_close), null)
                .show();
    }

    /*
     * Dump the database into a json file inside the phone storage
     */
    private boolean exportDatabaseAsJSON(SQLiteDatabase database){

        if(!Database.isExternalStorageWritable()){
            Log.d("EXTERNAL STORAGE", "The external storage is unavailable");
            return false;
        }
        try{
            FileWriter fWriter;
            File jsonDir = new File(Environment.getExternalStorageDirectory() + "/AR-driving-assistant");
            jsonDir.mkdirs();
            File jsonFile = new File(jsonDir, "database_" + System.currentTimeMillis() + ".json");
            Log.d("JSON EXPORT PATH", jsonFile.getPath());
            fWriter = new FileWriter(jsonFile, true);
            fWriter.write( getDatabaseAsJSON(database).toString() );
            fWriter.flush();
            fWriter.close();
        }catch(Exception e){
            Log.d("EXPORT EXCEPTION", e.getMessage());
            return false;
        }
        return true;
    }

    /*
     * Adapted from http://stackoverflow.com/questions/25722585/convert-sqlite-to-json
     */
    private JSONObject getDatabaseAsJSON(SQLiteDatabase database)
    {
        JSONObject databaseJSON = new JSONObject();

        //Translate every table to json
        for(String tableName : Database.getAllTableNames(database)){
            JSONArray tableArray = tableToJSON(database, tableName);
            try {
                Log.d("EXPORT TABLE", tableName);
                databaseJSON.put(tableName, tableArray);
            } catch (JSONException e) {
                Log.d("EXPORT TABLE", e.getMessage());
            }
        }
        return databaseJSON;
    }

    /**
     * Translates the param tableName to a jsonArray
     * @param database
     * @param tableName
     * @return
     */
    private static JSONArray tableToJSON(SQLiteDatabase database, String tableName){

        String searchQuery = "SELECT  * FROM " + tableName;
        Cursor cursor = database.rawQuery(searchQuery, null );
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i < totalColumn ; i++ ) {

                if( cursor.getColumnName(i) != null ) {
                    try {
                        if( cursor.getString(i) != null ) {
                            Log.d("EXPORT COLUMN", cursor.getString(i) );
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    } catch( Exception e ) {
                        Log.d("EXPORT COLUMN", e.getMessage()  );
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("EXPORT COLUMN", resultSet.toString() );
        return resultSet;
    }
}