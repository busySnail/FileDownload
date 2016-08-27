package com.busysnail.filedownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ListView mLvFile;
    private List<FileInfo> mFileList;
    private FileListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initData();
        initViews();
        initReceiver();

        mAdapter = new FileListAdapter(this, mFileList);
        mLvFile.setAdapter(mAdapter);

    }

    private void initViews() {
        mLvFile = (ListView) findViewById(R.id.lv_file);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_title);
        toolbar.setSubtitle(R.string.toolbar_subtitle);
        setSupportActionBar(toolbar);

    }

    private void initReceiver() {
        //注册更新UI的广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    void initData(){

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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

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

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra(DownloadService.FINISHED_RATIO, 10);
                Log.i("busysnail","MainActivity finished: "+finished+"");
                int fileId = intent.getIntExtra(DownloadService.FILE_ID, 0);
                mAdapter.updateProgress(fileId, finished);
//                mPbProgress.setProgress(finished);
//                if(finished==100){
//                    mTvStatus.setText("文件下载成功");
//                }
            } else if (DownloadService.ACTION_FINISHED.equals(intent.getAction())) {
                //下载成功，更新进度为0
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(DownloadService.FILEINFO);
                mAdapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(MainActivity.this, mFileList.get(fileInfo.getId()).getFilename() + "下载完毕\n"+"存储位置："+DownloadService.DOWNLOAD_PATH, Toast.LENGTH_SHORT).show();

            }
        }
    };
}
