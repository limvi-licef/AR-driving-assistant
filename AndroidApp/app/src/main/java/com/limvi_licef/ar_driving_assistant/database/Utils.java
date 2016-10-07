package com.limvi_licef.ar_driving_assistant.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Utils {

    /*
     *  Dump the database into a json file inside the phone storage
     * The json file will only be visible after the device is rebooted
     */
    public static boolean exportDatabaseAsJSON(SQLiteDatabase database){

        if(!isExternalStorageWritable()){
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
    private static JSONObject getDatabaseAsJSON(SQLiteDatabase database)
    {
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

    private static ArrayList<String> getAllTableNames(SQLiteDatabase database){
        ArrayList<String> namesArray = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while(c.moveToNext()){
            String s = c.getString(0);
            if(!s.equals("android_metadata")) namesArray.add(s);
        }
        c.close();
        return namesArray;
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
