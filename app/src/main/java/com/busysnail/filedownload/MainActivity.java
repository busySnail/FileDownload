package com.busysnail.filedownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.services.DownloadService;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mRvFile;
    private List<FileInfo> mFileList;
    private RecyclerFileAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRvFile= (RecyclerView) findViewById(R.id.rv_file);
        mFileList=new ArrayList<>();

        //创建文件对象
      FileInfo fileInfo1 = new FileInfo(0,
                "http://www.imooc.com/mobile/imooc.apk",
                "1imooc.apk", 0, 0);
        FileInfo fileInfo2 = new FileInfo(1,
                "http://www.imooc.com/mobile/imooc.apk",
                "2imooc.apk", 0, 0);
        FileInfo fileInfo3 = new FileInfo(2,
                "http://www.imooc.com/mobile/imooc.apk",
                "3imooc.apk", 0, 0);
        FileInfo fileInfo4 = new FileInfo(3,
                "http://www.imooc.com/mobile/imooc.apk",
                "4imooc.apk", 0, 0);

        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        mFileList.add(fileInfo4);


//
//        mBtnStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, DownloadService.class);
//                intent.setAction(DownloadService.ACTION_START);
//                intent.putExtra(DownloadService.FILEINFO, fileInfo);
//                startService(intent);
//                mTvStatus.setText("文件下载中...");
//
//            }
//
//        });
//
//
//        mBtnStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, DownloadService.class);
//                intent.setAction(DownloadService.ACTION_STOP);
//                intent.putExtra(DownloadService.FILEINFO, fileInfo);
//                startService(intent);
//                mTvStatus.setText("任务暂停");
//            }
//        });

         mAdapter=new RecyclerFileAdapter(this,mFileList);

        mRvFile.setLayoutManager(new LinearLayoutManager(this));

        mRvFile.setAdapter(mAdapter);



        //注册更新UI的广播接收器
        IntentFilter filter=new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver,filter);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra(DownloadService.FINISHED_RATIO, 0);
                int fileId=intent.getIntExtra(DownloadService.FILE_ID,0);
                mAdapter.updateProgress(fileId,finished);
//                mPbProgress.setProgress(finished);
//                if(finished==100){
//                    mTvStatus.setText("文件下载成功");
//                }
            }else if(DownloadService.ACTION_FINISHED.equals(intent.getAction())){
                //下载成功，更新进度为0
                FileInfo fileInfo= (FileInfo) intent.getSerializableExtra(DownloadService.FILEINFO);
                mAdapter.updateProgress(fileInfo.getId(),0);
                Toast.makeText(MainActivity.this,mFileList.get(fileInfo.getId()).getFilename()+"下载完毕",Toast.LENGTH_SHORT).show();

            }
        }
    };
}
