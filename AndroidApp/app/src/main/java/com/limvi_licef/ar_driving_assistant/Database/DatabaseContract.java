package com.limvi_licef.ar_driving_assistant.database;

public final class DatabaseContract {

    public static final int    DATABASE_VERSION   = 1;
    public static final String DATABASE_NAME      = "drivingAssistant.db";
    public static final String SQL_CREATE_ENTRIES = "";
//    private static final String SQL_CREATE_ENTRIES =
//            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
//                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
//                    FeedEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
//                    FeedEntry.COLUMN_NAME_SUBTITLE + TEXT_TYPE + " )";
    public static final String SQL_DELETE_ENTRIES = "";

    private static final String TEXT_TYPE          = " TEXT";
    private static final String COMMA_SEP          = ",";

    private DatabaseContract() {}

    //TODO Tables
//    public static abstract class Table1 implements BaseColumns {
//        public static final String TABLE_NAME       = "nameOfTable";
//        public static final String COLUMN_NAME_COL1 = "column1";
//        public static final String COLUMN_NAME_COL2 = "column2";
//        public static final String COLUMN_NAME_COL3 = "column3";
//
//
//        public static final String CREATE_TABLE = "CREATE TABLE " +
//                TABLE_NAME + " (" +
//                _ID + " INTEGER PRIMARY KEY," +
//                COLUMN_NAME_COL1 + TEXT_TYPE + COMMA_SEP +
//                COLUMN_NAME_COL2 + TEXT_TYPE + COMMA_SEP +
//                COLUMN_NAME_COL3 + TEXT_TYPE + " )";
//        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
//    }
}