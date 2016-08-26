package com.busysnail.filedownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

    private TextView mTvFilename;
    private ProgressBar mPbProgress;
    private Button mBtnStart;
    private Button mBtnStop;
    private TextView mTvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvFilename = (TextView) findViewById(R.id.tv_filename);
        mPbProgress = (ProgressBar) findViewById(R.id.pb_progress);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mTvStatus= (TextView) findViewById(R.id.tv_status);
        mPbProgress.setMax(100);


        //创建文件对象
        final FileInfo fileInfo = new FileInfo(0,
                "http://www.imooc.com/mobile/imooc.apk",
                "imooc.apk", 0, 0);

        mTvFilename.setText(fileInfo.getFilename());

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra(DownloadService.FILEINFO, fileInfo);
                startService(intent);
                mTvStatus.setText("文件下载中...");

            }

        });


        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra(DownloadService.FILEINFO, fileInfo);
                startService(intent);
                mTvStatus.setText("任务暂停");
            }
        });


        //注册更新UI的广播接收器
        IntentFilter filter=new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
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
                mPbProgress.setProgress(finished);
                if(finished==100){
                    mTvStatus.setText("文件下载成功");
                }
            }
        }
    };
}
