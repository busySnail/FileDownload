package com.busysnail.filedownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.busysnail.filedownload.entity.ThreadInfo;
import com.busysnail.filedownload.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * ThreadInfo数据访问接口实现
 *
 */

public class ThreadDAOImpl implements IThreadDAO {

    private DBHelper mHelper;

    public ThreadDAOImpl(Context context) {
        mHelper = DBHelper.getInstance(context);
    }


    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});

        Util.closeQuietly(db);
    }

    @Override
    public synchronized  void deleteThread(String url) {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ?",
                new Object[]{url});

        Util.closeQuietly(db);
    }

    @Override
    public synchronized  void updateThread(String url, int thread_id, long finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
                new Object[]{finished, url, thread_id});

        Util.closeQuietly(db);

    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        List<ThreadInfo> result=new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();

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

        Util.closeQuietly(cursor);
        Util.closeQuietly(db);
        return result;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db=mHelper.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url, thread_id+""});

        boolean exists=cursor.moveToNext();
        Util.closeQuietly(cursor);
        Util.closeQuietly(db);
        return exists;
    }
}
