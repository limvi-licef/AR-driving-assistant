package com.limvi_licef.ar_driving_assistant.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

}
