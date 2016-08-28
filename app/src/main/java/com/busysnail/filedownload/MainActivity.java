package com.busysnail.filedownload;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.services.DownloadService;
import com.busysnail.filedownload.utils.NotificationUtil;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ListView mLvFile;
    private List<FileInfo> mFileList;
    private FileListAdapter mAdapter;
    private NotificationUtil mNotificationUtil;
    private Messenger mServiceMessenger;
   private final String TAG="busysnail";

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            FileInfo fileInfo=null;
            switch (msg.what){
                case DownloadService.MSG_UPDATE:
                    int finished =msg.arg1;
                    int fileId = msg.arg2;
                    mAdapter.updateProgress(fileId, finished);
                    //更新通知
                    mNotificationUtil.updateNotification(fileId,finished);
                    break;
                case DownloadService.MSG_FINISHED:
                    //下载成功，更新进度为0
                     fileInfo = (FileInfo) msg.obj;
                    mAdapter.updateProgress(fileInfo.getId(), 0);
                    Toast.makeText(MainActivity.this, mFileList.get(fileInfo.getId()).getFilename() + "下载完毕\n" + "存储位置：" + DownloadService.DOWNLOAD_PATH, Toast.LENGTH_SHORT).show();

                    //取消通知
                    mNotificationUtil.cancelNotification(fileInfo.getId());
                    break;
                case DownloadService.MSG_START:

                    fileInfo = (FileInfo) msg.obj;
                    Log.i(TAG,"接收服务开始下载的MSG_START，显示通知:(FileInfo) msg.obj:"+fileInfo);
                    mNotificationUtil.showNotification(fileInfo);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initViews();
//        initReceiver();

        mAdapter = new FileListAdapter(this, mFileList);
        mLvFile.setAdapter(mAdapter);

        mNotificationUtil=new NotificationUtil(this);
        //绑定Service
        Intent intent=new Intent(this,DownloadService.class);
        Log.i(TAG,"绑定服务");
        bindService(intent,mConnection, DownloadService.BIND_AUTO_CREATE);

    }

    ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger=new Messenger(service);
            Log.i(TAG,"接收ServiceHandler");
            //传给适配器
            mAdapter.setMessenger(mServiceMessenger);
            //创建Activity中的messenger
            Messenger messenger=new Messenger(mHandler);
            //创建Message
            Message msg=new Message();
            msg.what=DownloadService.MSG_BIND;
            msg.replyTo=messenger;
            Log.i(TAG,"发送ActivityMessenger");
            //使用Service的Messenger发送Activity中的Messenger
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initViews() {
        mLvFile = (ListView) findViewById(R.id.lv_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_title);
        toolbar.setSubtitle(R.string.toolbar_subtitle);
        setSupportActionBar(toolbar);

    }

//    private void initReceiver() {
//        //注册更新UI的广播接收器
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(DownloadService.ACTION_START);
//        filter.addAction(DownloadService.ACTION_UPDATE);
//        filter.addAction(DownloadService.ACTION_FINISHED);
//        registerReceiver(mReceiver, filter);
//    }

    void initData() {

        mFileList = new ArrayList<>();

        //创建文件对象
        FileInfo fileInfo1 = new FileInfo(0, "http://dldir1.qq.com/weixin/android/weixin6316android780.apk",
                "weixin.apk", 0, 0);
        FileInfo fileInfo2 = new FileInfo(1, "http://111.202.99.12/sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk",
                "qq.apk", 0, 0);
        FileInfo fileInfo3 = new FileInfo(2, "http://www.imooc.com/mobile/imooc.apk",
                "imooc.apk", 0, 0);
        FileInfo fileInfo4 = new FileInfo(3, "http://www.imooc.com/download/Activator.exe",
                "Activator.exe", 0, 0);

//        FileInfo fileInfo5 = new FileInfo(4, "http://dldir1.qq.com/weixin/android/weixin6316android780.apk",
//                "weixin1.apk", 0, 0);
//        FileInfo fileInfo6 = new FileInfo(5, "http://111.202.99.12/sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk",
//                "qq1.apk", 0, 0);
//        FileInfo fileInfo7 = new FileInfo(6, "http://www.imooc.com/mobile/imooc.apk",
//                "imooc1.apk", 0, 0);
//        FileInfo fileInfo8 = new FileInfo(7, "http://www.imooc.com/download/Activator.exe",
//                "Activator1.exe", 0, 0);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        mFileList.add(fileInfo4);
//        mFileList.add(fileInfo5);
//        mFileList.add(fileInfo6);
//        mFileList.add(fileInfo7);
//        mFileList.add(fileInfo8);
    }

//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(mReceiver);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (DownloadService.ACTION_START.equals(intent.getAction())) {
//                FileInfo fileInfo= (FileInfo) intent.getSerializableExtra(DownloadService.FILEINFO);
//                mNotificationUtil.showNotification(fileInfo);
//
//            } else if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
//                int finished = intent.getIntExtra(DownloadService.FINISHED_RATIO, 0);
//                Log.i("busysnail", "MainActivity finished: " + finished + "");
//                int fileId = intent.getIntExtra(DownloadService.FILE_ID, 0);
//                mAdapter.updateProgress(fileId, finished);
//                //更新通知
//                mNotificationUtil.updateNotification(fileId,finished);
//            } else if (DownloadService.ACTION_FINISHED.equals(intent.getAction())) {
//                //下载成功，更新进度为0
//                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(DownloadService.FILEINFO);
//                mAdapter.updateProgress(fileInfo.getId(), 0);
//                Toast.makeText(MainActivity.this, mFileList.get(fileInfo.getId()).getFilename() + "下载完毕\n" + "存储位置：" + DownloadService.DOWNLOAD_PATH, Toast.LENGTH_SHORT).show();
//
//                //取消通知
//                mNotificationUtil.cancelNotification(fileInfo.getId());
//
//            }
//        }
//    };
}
