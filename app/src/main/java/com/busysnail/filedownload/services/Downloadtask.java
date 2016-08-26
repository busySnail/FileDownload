package com.busysnail.filedownload.services;

import android.content.Context;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.entity.ThreadInfo;

/**
 * 下载任务类
 */

public class Downloadtask  {
    private Context mContext;
    private FileInfo mFileInfo;

    public Downloadtask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
    }

    class DownloadThread extends Thread{
       
    }
}
