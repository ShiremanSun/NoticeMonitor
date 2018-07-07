package com.example.sunday.noicemonitor.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static volatile MySQLiteOpenHelper mySQLiteOpenHelper;
    private final static String DATABASE_NAME="noise.db";
    private final static String TABLE_NAME="NOISE_TIME";
    private final static int version=1;
    private final static String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"(ID INT PRIMARY KEY AUTOINCREMENT,"+
            "TIME varchar(15),"+"DECIBEL smallint)";

    private MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static SQLiteDatabase getInstance(Context context){
             if(mySQLiteOpenHelper==null){
                 synchronized (MySQLiteOpenHelper.class){
                     if(mySQLiteOpenHelper==null){
                         mySQLiteOpenHelper=new MySQLiteOpenHelper(context);
                     }
                 }
             }
             return mySQLiteOpenHelper.getReadableDatabase();
    }
}
