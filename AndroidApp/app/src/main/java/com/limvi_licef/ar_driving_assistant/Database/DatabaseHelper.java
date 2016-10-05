package com.limvi_licef.ar_driving_assistant.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper
{

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getHelper(Context context)
    {
        if (instance == null)
            instance = new DatabaseHelper(context);

        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        for (String createStatement : DatabaseContract.SQL_CREATE_TABLE_ARRAY) {
            db.execSQL(createStatement);
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Data migration once dev is complete
        for (String deleteStatement : DatabaseContract.SQL_DELETE_TABLE_ARRAY) {
            db.execSQL(deleteStatement);
        }
        onCreate(db);
    }

    public void exportDatabaseAsJSON(){

        if(!isExternalStorageWritable()){
            Log.d("EXTERNAL STORAGE", "The external storage is unavailable");
            return;
        }
        //TODO TEST
        try{
            FileWriter fWriter;
            File jsonFile = new File(Environment.getExternalStorageDirectory() + "/driving_assistant/database_" + System.currentTimeMillis() + ".json");
            Log.d("JSON EXPORT PATH", jsonFile.getPath());
            fWriter = new FileWriter(jsonFile, true);
            fWriter.write( getDatabaseAsJSON().toString() ); //?
            fWriter.flush();
            fWriter.close();
        }catch(Exception e){
            Log.d("EXPORT EXCEPTION", e.getMessage());
        }
    }

    /*
     * Adapted from http://stackoverflow.com/questions/25722585/convert-sqlite-to-json
     */
    private JSONObject getDatabaseAsJSON()
    {
        SQLiteDatabase database = getReadableDatabase();

        JSONObject databaseJSON = new JSONObject();

        for(String tableName : getAllTableNames(database)){
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

    private JSONArray tableToJSON(SQLiteDatabase database, String tableName){

        String searchQuery = "SELECT  * FROM " + tableName;
        Cursor cursor = database.rawQuery(searchQuery, null );
        JSONArray resultSet = new JSONArray();
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

    private ArrayList<String> getAllTableNames(SQLiteDatabase database){
        ArrayList<String> namesArray = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while(c.moveToNext()){
            String s = c.getString(0);
            if(!s.equals("android_metadata")) namesArray.add(s);
        }
        c.close();
        return namesArray;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
