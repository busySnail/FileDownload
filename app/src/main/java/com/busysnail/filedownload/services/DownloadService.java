package com.busysnail.filedownload.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.InflaterOutputStream;



public class DownloadService extends Service {

    public static final String ACTION_START="ACTION_START";
    public static final String ACTION_STOP="ACTION_STOP";
    public static final String ACTION_UPDATE="ACTION_UPDATE";
    public static final String DOWNLOAD_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/downloads/";
    public static final int MSG_INIT=0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获得activity传来的参数
        if(ACTION_START.equals(intent.getAction())){
            FileInfo fileInfo= (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("test","start "+fileInfo.toString());
            //启动初始化线程
            new InitThread(fileInfo).start();
        }else if(ACTION_STOP.equals(intent.getAction())){
            FileInfo fileInfo= (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("test","stop "+fileInfo.toString());
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo= (FileInfo) msg.obj;
                    Log.i("test","Init:"+fileInfo);
                    break;
            }

        }
    };

    /**
     * 初始化子线程
     */
    class InitThread extends Thread{
        private FileInfo mFileInfo;

        public InitThread(FileInfo mFileInfo){
            this.mFileInfo=mFileInfo;
        }

        @Override
        public void run() {

            HttpURLConnection connection=null;
            RandomAccessFile randomAccessFile=null;
            try{
                //连接网络文件
                URL url=new URL(mFileInfo.getUrl());
                connection= (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int length=-1;
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                    //获得文件长度
                    length=connection.getContentLength();
                }
                if(length<=0){
                    return;
                }
                //在本地设置文件
                File dir=new File(DOWNLOAD_PATH);
                if(!dir.exists()){
                    dir.mkdir();
                }
                File file=new File(dir,mFileInfo.getFilename());
                randomAccessFile=new RandomAccessFile(file,"rwd"); //能随机存取的文件，类似于一个大规模数组，指定模式“读写删除”
                //设置文件长度
                randomAccessFile.setLength(length);

                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(connection!=null){
                    connection.disconnect();
                }
                Util.closeQuietly(randomAccessFile);

            }

        }
    }
}
