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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.InflaterOutputStream;


public class DownloadService extends Service {


    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";

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
    private ServiceHandler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler=new ServiceHandler(this);
    }



    /**
     * 初始化子线程
     */
    private static class InitThread extends Thread {
        private FileInfo mFileInfo;
        WeakReference<DownloadService> serviceWeakReference;

         InitThread(FileInfo mFileInfo,WeakReference<DownloadService> service) {
            this.mFileInfo = mFileInfo;
            serviceWeakReference=service;
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

                mFileInfo.setLength(length);
                serviceWeakReference.get().mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();

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
        //创建一个Messenger对象
        Messenger messenger = new Messenger(mHandler);
        //返回Messenger的Binder
        return messenger.getBinder();
    }

    private static class ServiceHandler extends Handler{
        private WeakReference<DownloadService> serviceWeakReference;
        private Messenger activityMessenger;

        ServiceHandler(DownloadService service){
            serviceWeakReference=new WeakReference<>(service);

        }
        @Override
        public void handleMessage(Message msg) {
            FileInfo fileInfo = null;
            DownloadTask task = null;
            switch (msg.what) {
                case MSG_INIT:
                    fileInfo = (FileInfo) msg.obj;
                    //启动下载任务
                    task = new DownloadTask(serviceWeakReference.get(), activityMessenger, fileInfo, THREAD_COUNT);
                    task.download();
                    //把下载任务添加到集合中
                    serviceWeakReference.get().mTasks.put(fileInfo.getId(), task);

                    Message msg1 = new Message();
                    msg1.what = MSG_START;
                    msg1.obj = fileInfo;
                    try {
                        activityMessenger.send(msg1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_BIND:
                    //处理绑定的Messenger
                    activityMessenger = msg.replyTo;
                    break;
                case MSG_START:
                    //获得activity传来的参数
                    fileInfo = (FileInfo) msg.obj;
                    //启动初始化线程
                    InitThread thread = new InitThread(fileInfo,serviceWeakReference);
                    thread.start();
                    break;
                case MSG_STOP:
                    //获得activity传来的参数
                    fileInfo = (FileInfo) msg.obj;
                    task =  serviceWeakReference.get().mTasks.get(fileInfo.getId());
                    if (task != null) {
                        task.setPause(true);
                    }
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
