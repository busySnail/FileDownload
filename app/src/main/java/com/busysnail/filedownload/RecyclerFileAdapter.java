package com.busysnail.filedownload;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.busysnail.filedownload.entity.FileInfo;

import java.util.List;

/**
 * author: malong on 2016/8/27
 * email: malong_ilp@163.com
 * address: Xidian University
 */

public class RecyclerFileAdapter extends RecyclerView.Adapter<RecyclerFileAdapter.ViewHolder> {

    private Context mContext;
    private List<FileInfo> mFileList;
    private OnItemClickListener itemClickListener;





    public RecyclerFileAdapter(Context mContext, List<FileInfo> mFileList) {
        this.mContext = mContext;
        this.mFileList = mFileList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.bind(mFileList.get(position));

    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mTvFilename;
        private Button mBtnStart,mBtnStop;
        private ProgressBar mPbProgress;
        private TextView mTvStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvFilename= (TextView) itemView.findViewById(R.id.tv_filename);
            mBtnStart= (Button) itemView.findViewById(R.id.btn_start);
            mBtnStop= (Button) itemView.findViewById(R.id.btn_stop);
            mPbProgress= (ProgressBar) itemView.findViewById(R.id.pb_progress);
            mTvStatus= (TextView) itemView.findViewById(R.id.tv_status);

            mPbProgress.setMax(100);

            mBtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onStartBtnClicked();
                }
            });
            mBtnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onStopBtnClicked();
                }
            });

        }

        public void bind(FileInfo fileInfo){
            mTvFilename.setText(fileInfo.getFilename());
            mPbProgress.setProgress(fileInfo.getFinished());
        }
    }

    interface OnItemClickListener {
        void onStartBtnClicked();
        void onStopBtnClicked();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
