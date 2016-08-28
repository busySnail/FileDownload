package com.busysnail.filedownload.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.utils.Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.InflaterOutputStream;


public class DownloadService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    public static final String FINISHED_RATIO = "FINISHED_RATIO";
    public static final String FILE_ID = "FILE_ID";
    public static final String FILEINFO = "FILEINFO";

    public static final int MSG_INIT = 0x1;
    public static final int MSG_BIND = 0x2;
    public static final int MSG_START = 0x3;
    public static final int MSG_STOP = 0x4;
    public static final int MSG_UPDATE = 0x5;
    public static final int MSG_FINISHED = 0x6;


    public static final int THREAD_COUNT = 3;
    //    private DownloadTask mTask;
    //下载任务的集合 <文件ID，下载任务>
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();
    private Messenger mActivityMessenger;
    private final String TAG="busysnail";

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//
//        if(ACTION_START.equals(intent.getAction())){
//            //获得activity传来的参数
//            FileInfo fileInfo= (FileInfo) intent.getSerializableExtra(FILEINFO);
////            if(task!=null){
////                task.setPause(false);
////            }
//            //启动初始化线程
//            InitThread thread= new InitThread(fileInfo);
//            thread.start();
//        }else if(ACTION_STOP.equals(intent.getAction())){
//            //获得activity传来的参数
//            FileInfo fileInfo= (FileInfo) intent.getSerializableExtra(FILEINFO);
//            DownloadTask task=mTasks.get(fileInfo.getId());
//            if(task!=null){
//                task.setPause(true);
//            }
//        }
//        return super.onStartCommand(intent,flags,startId);
//    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FileInfo fileInfo = null;
            DownloadTask task = null;
            switch (msg.what) {
                case MSG_INIT:
                    Log.i(TAG,"服务接收到init完成,启动下载任务");
                    fileInfo = (FileInfo) msg.obj;
                    //启动下载任务
                    task = new DownloadTask(DownloadService.this, mActivityMessenger, fileInfo, THREAD_COUNT);
                    task.download();
                    //把下载任务添加到集合中
                    mTasks.put(fileInfo.getId(), task);
//                    //发送启动命令的广播
//                    Intent intent=new Intent(DownloadService.ACTION_START);
//                    intent.putExtra(DownloadService.FILEINFO,fileInfo);
//                    sendBroadcast(intent);

                    Message msg1 = new Message();
                    msg1.what = MSG_START;
                    msg1.obj = fileInfo;
                    Log.i(TAG,"服务接收到init完成,启动下载任务,通知Activity MSG_START  fileinfo:"+fileInfo);
                    try {
                        mActivityMessenger.send(msg1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_BIND:
                    //处理绑定的Messenger
                    Log.i(TAG,"服务接收ActivityMessenger");
                    mActivityMessenger = msg.replyTo;
                    break;
                case MSG_START:
                    Log.i(TAG,"服务接收按钮Start动作，执行init");
                    //获得activity传来的参数
                    fileInfo = (FileInfo) msg.obj;
                    //启动初始化线程
                    InitThread thread = new InitThread(fileInfo);
                    thread.start();
                    break;
                case MSG_STOP:
                    Log.i(TAG,"服务接收按钮Start动作，执行暂停");
                    //获得activity传来的参数
                    fileInfo = (FileInfo) msg.obj;
                    task = mTasks.get(fileInfo.getId());
                    if (task != null) {
                        task.setPause(true);
                    }
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 初始化子线程
     */
    class InitThread extends Thread {
        private FileInfo mFileInfo;

        public InitThread(FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }

        @Override
        public void run() {

            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            try {
                //连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //获得文件长度
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                //在本地设置文件
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, mFileInfo.getFilename());
                randomAccessFile = new RandomAccessFile(file, "rwd"); //能随机存取的文件，类似于一个大规模数组，指定模式“读写删除”
                //设置文件长度
                randomAccessFile.setLength(length);

                Log.i(TAG,"init执行完毕，发送MSG_INIT和fileinfo");
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                Util.closeQuietly(randomAccessFile);

            }

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"接收绑定，返回handler");
        //创建一个Messenger对象
        Messenger messenger = new Messenger(mHandler);
        //返回Messenger的Binder
        return messenger.getBinder();
    }
}
