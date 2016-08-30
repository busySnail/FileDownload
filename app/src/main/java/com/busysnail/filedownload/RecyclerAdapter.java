package com.busysnail.filedownload;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.services.DownloadService;

import java.util.List;

/**
 * author: malong on 2016/8/29
 * email: malong_ilp@163.com
 * address: Xidian University
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    private Context mContext;
    private List<FileInfo> mFileList;
    private LayoutInflater mInflater;
    private Messenger mMessenger;
    private final String TAG="busysnail";

    public RecyclerAdapter(Context context, List<FileInfo> fileInfos)
    {
        this.mContext = context;
        this.mFileList = fileInfos;
        this.mInflater=LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.list_item,parent,false));
    }

    public void setMessenger(Messenger mMessenger){
        Log.i(TAG,"适配器接收Servicehandler");
        this.mMessenger=mMessenger;
    }

    public void updateProgress(int id, int progress)
    {
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        final FileInfo fileInfo=mFileList.get(position);


            int pro= (int) fileInfo.getFinished();
            holder.mTvFileName.setText(fileInfo.getFilename());
            holder.mPbProgress.setProgress(pro);
            holder.mTvProgress.setText(pro+"%");
            holder.mBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mBtnStart.setEnabled(false);
                    holder.mBtnStop.setEnabled(true);
                    holder.mTvProgress.setVisibility(View.VISIBLE);

                    Message msg=new Message();
                    msg.what= DownloadService.MSG_START;
                    msg.obj=fileInfo;
                    Log.i(TAG,"适配器Start按钮发送 MSG_START");
                    try {
                        mMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });

            holder.mBtnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mBtnStop.setEnabled(false);
                    holder.mBtnStart.setEnabled(true);

                    Message msg=new Message();
                    msg.what=DownloadService.MSG_STOP;
                    msg.obj=fileInfo;
                    Log.i(TAG,"适配器Start按钮发送 MSG_START");
                    try {
                        mMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });


    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTvFileName;
        TextView mTvProgress;
        ProgressBar mPbProgress;
        Button mBtnStart;
        Button mBtnStop;


        public ViewHolder(View itemView) {
            super(itemView);
            mTvFileName= (TextView) itemView.findViewById(R.id.tv_filename);
            mTvProgress= (TextView) itemView.findViewById(R.id.tv_progress);
            mPbProgress= (ProgressBar) itemView.findViewById(R.id.pb_progressbar);
            mBtnStart= (Button) itemView.findViewById(R.id.btn_start);
            mBtnStop= (Button) itemView.findViewById(R.id.btn_stop);

        }
    }
}
