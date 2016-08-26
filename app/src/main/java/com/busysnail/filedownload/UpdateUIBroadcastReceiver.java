package com.busysnail.filedownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.busysnail.filedownload.services.DownloadService;

/**
 * author: malong on 2016/8/26
 * email: malong_ilp@163.com
 * address: Xidian University
 */

public class UpdateUIBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(DownloadService.ACTION_UPDATE.equals(intent.getAction())){
            int finished=intent.getIntExtra(DownloadService.FINISHED_RATIO,0);
        }
    }
}
