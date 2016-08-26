package com.busysnail.filedownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHelper extends SQLiteOpenHelper {


    /**
     * 表对应Threadinfo类
     */
    private static final String DB_NAME="download.db";
    private static final int VERSION=1;
    private static final String SQL_CREATE=" create table thread_info "+
            "(_id integer primary key autoincrement," +
            "thread_id integer," +
            "url text,"+
            "start integer,"+
            "end integer,"+
            "end integer,"+
            "finished integer)";
    private static final String SQL_DROP="drop if exists thread_info";




    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);


    }
}
