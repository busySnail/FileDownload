package com.busysnail.filedownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



 class DBHelper extends SQLiteOpenHelper {

    private static Context mContext;

    /**
     * 表对应Threadinfo类
     */
    private static final String DB_NAME = "download.db";
    private static final int VERSION = 1;
    private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer, url text, start integer, end integer, finished integer)";
    private static final String SQL_DROP = "drop table if exists thread_info";

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }
    /**
     * 静态内部类实现单例模式
     */
    private static class SingletonHolder{
        private static final DBHelper sInstance=new DBHelper(mContext);
    }

     static DBHelper getInstance(Context context){
        mContext=context;
        return SingletonHolder.sInstance;
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
