package com.busysnail.filedownload.services;

import android.content.Context;
import android.content.Intent;

import com.busysnail.filedownload.db.ThreadDAO;
import com.busysnail.filedownload.db.ThreadDAOImpl;
import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.entity.ThreadInfo;
import com.busysnail.filedownload.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 下载任务类
 */

public class DownloadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDao;
    private int mFinished=0;
    private boolean isPause=false;

    public DownloadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDAOImpl(mContext);
    }

    public void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos=mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo=null;
        if(threadInfos.size()==0){
            //初次下载，初始化线程信息对象
            threadInfo=new ThreadInfo(0,mFileInfo.getUrl(),0,mFileInfo.getLength(),0);
        }else{
            threadInfo=threadInfos.get(0);
        }
        //创建子线程进行下载
        new DownloadThread(threadInfo).start();
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {

            //像数据库插入线程信息
            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);
            }

            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile=null;
            InputStream input=null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");

                //设置下载位置
                int start=mThreadInfo.getStart()+mThreadInfo.getFinished();
                /**
                 * 设置资源请求范围
                 * Server通过请求头中的Range: bytes=0-xxx来判断是否是做Range请求，如果这个值存在而且有效，则只发回请求的那部分文件内容，
                 * 响应的状态码变成206，表示Partial Content，并设置Content-Range。如果无效，则返回416状态码，
                 * 表明Request Range Not Satisfiable.如果不包含Range的请求头，则继续通过常规的方式响应。
                 */
                connection.setRequestProperty("Range","bytes="+start+"-"+mThreadInfo.getEnd());

                //设置文件写入位置，和文件下载位置对应
                File file=new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFilename());
               randomAccessFile=new RandomAccessFile(file,"rwd");
                randomAccessFile.seek(start);

                //开始下载
                Intent intent=new Intent(DownloadService.ACTION_UPDATE);
                mFinished=mThreadInfo.getFinished();
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                    input=connection.getInputStream();
                    byte[] buffer=new byte[1024*4];
                    int len=-1;
                    long time=System.currentTimeMillis();
                    while((len=input.read(buffer))!=-1){
                        //写入文件
                        randomAccessFile.write(buffer,0,len);
                        //把下载进度通过广播发送给Activity
                        mFinished+=len;
                        if(System.currentTimeMillis()-time>500){     //每500ms发送一次广播
                            intent.putExtra("finished",mFinished*100/mFileInfo.getLength()); //完成百分比
                            mContext.sendBroadcast(intent);
                        }
                        //暂停时保存下载进度
                        if(isPause){
                            mDao.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mFinished);
                        }

                    }
                    //下载完成后，线程信息就没用了，可以删除
                    mDao.deleteThread(mThreadInfo.getUrl(),mThreadInfo.getId());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(connection!=null){
                    connection.disconnect();
                }
                Util.closeQuietly(input);
                Util.closeQuietly(randomAccessFile);
            }



        }
    }
}
