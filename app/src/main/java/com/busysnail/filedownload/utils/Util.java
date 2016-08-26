package com.busysnail.filedownload.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.Closeable;
import java.io.IOException;

/**
 * author: malong on 2016/8/26
 * email: malong_ilp@163.com
 * address: Xidian University
 */

public class Util {

    /**
     * 只关注是否联网
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * Java 中有一个 Closeable 接口,标识了一个可关闭的对象,它只有一个 close 方法.
     */
    public static void closeQuietly(Closeable s){
        try{
            if(s!=null)
                s.close();
        }catch(IOException e){
            //Log or rethrow as unchecked (like RuntimException) ;)
            e.printStackTrace();
        }
    }

}
