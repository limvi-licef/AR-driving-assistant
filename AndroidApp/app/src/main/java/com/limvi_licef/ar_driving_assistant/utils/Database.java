package com.limvi_licef.ar_driving_assistant.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.util.ArrayList;

public abstract class Database {

    public static ArrayList<String> getAllTableNames(SQLiteDatabase database){
        ArrayList<String> namesArray = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while(c.moveToNext()){
            String s = c.getString(0);
            if(!s.equals("android_metadata")) namesArray.add(s);
        }
        c.close();
        return namesArray;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
