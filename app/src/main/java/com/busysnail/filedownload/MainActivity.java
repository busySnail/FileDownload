package com.busysnail.filedownload;

import android.content.ComponentName;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.services.DownloadService;
import com.busysnail.filedownload.utils.NotificationUtil;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mRvList;
    private List<FileInfo> mFileList;
    private RecyclerAdapter mAdapter;
    private NotificationUtil mNotificationUtil;
    private final String TAG = "busysnail";

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initViews();

        mAdapter = new RecyclerAdapter(this, mFileList);
        mRvList.setLayoutManager(new LinearLayoutManager(this));
        mRvList.setAdapter(mAdapter);

//        mNotificationUtil = new NotificationUtil(this);
        mNotificationUtil=new NotificationUtil(this);
        mHandler= new ActivityHandler(this);

        //绑定Service
        Intent intent = new Intent(this, DownloadService.class);
        Log.i(TAG, "绑定服务");
        bindService(intent, mConnection, DownloadService.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
             Messenger mServiceMessenger= new Messenger(service);
            Log.i(TAG, "接收ServiceHandler");
            //传给适配器
            mAdapter.setMessenger(mServiceMessenger);
            //创建Activity中的messenger
            Messenger messenger = new Messenger(mHandler);
            //创建Message
            Message msg = new Message();
            msg.what = DownloadService.MSG_BIND;
            msg.replyTo = messenger;
            Log.i(TAG, "发送ActivityMessenger");
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
        mRvList = (RecyclerView) findViewById(R.id.lv_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar!=null){
            toolbar.setTitle(R.string.toolbar_title);
            toolbar.setSubtitle(R.string.toolbar_subtitle);
            setSupportActionBar(toolbar);
        }


    }


   private void initData() {

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

        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        mFileList.add(fileInfo4);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class ActivityHandler extends Handler {
        private WeakReference<MainActivity> activityWeakReference;
        private RecyclerAdapter adapter;
        private NotificationUtil notificationUtil;

        ActivityHandler(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
            adapter = this.activityWeakReference.get().mAdapter;
            notificationUtil = this.activityWeakReference.get().mNotificationUtil;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadService.MSG_UPDATE:
                    int finished = msg.arg1;
                    int fileId = msg.arg2;
                    adapter.updateProgress(fileId, finished);
                    //更新通知
                    notificationUtil.updateNotification(fileId, finished);
                    break;
                case DownloadService.MSG_FINISHED:
                    //下载成功，更新进度为0
                    FileInfo fileInfoFinished = (FileInfo) msg.obj;
                    adapter.updateProgress(fileInfoFinished.getId(), 0);
                    Toast.makeText(activityWeakReference.get(),
                            activityWeakReference.get().mFileList.get(fileInfoFinished.getId()).getFilename() + "下载完毕\n" + "存储位置：" + DownloadService.DOWNLOAD_PATH, Toast.LENGTH_SHORT)
                            .show();

                    //取消通知
                    notificationUtil.cancelNotification(fileInfoFinished.getId());
                    break;
                case DownloadService.MSG_START:
                    FileInfo fileInfoStart = (FileInfo) msg.obj;
                    Log.i("busysnail","接收MSG_START"+fileInfoStart+" notificationUtil "+notificationUtil);
                    notificationUtil.showNotification(fileInfoStart);
                    break;
                default:
                    break;
            }
        }
    }

}
