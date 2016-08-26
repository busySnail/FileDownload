package com.busysnail.filedownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.busysnail.filedownload.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * ThreadInfo数据访问接口实现
 *
 */

public class ThreadDAOImpl implements ThreadDAO {

    DBHelper mHelper;

    public ThreadDAOImpl(Context context) {
        mHelper = new DBHelper(context);
    }


    @Override
    public void insertThread(ThreadInfo threadInfo) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});

        db.close();
    }

    @Override
    public void deleteThread(String url, int thread_id) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? and thread_id = ?",
                new String[]{url, String.valueOf(thread_id)});

        db.close();
    }

    @Override
    public void updateThread(String url, int thread_id, int finishded) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
                new String[]{String.valueOf(finishded), url, String.valueOf(thread_id)});

        db.close();

    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        List<ThreadInfo> result=new ArrayList<>();
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
        while (cursor.moveToNext()){
            ThreadInfo threadInfo=new ThreadInfo();
            threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));

            result.add(threadInfo);
        }

        cursor.close();
        db.close();
        return result;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db=mHelper.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url, String.valueOf(thread_id)});

        boolean exists=cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}
