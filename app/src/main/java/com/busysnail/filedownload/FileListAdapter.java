package com.busysnail.filedownload;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.services.DownloadService;
import com.busysnail.filedownload.services.DownloadTask;

import java.util.List;

public class FileListAdapter extends BaseAdapter
{
	private Context mContext;
	private List<FileInfo> mFileList;
	private LayoutInflater mInflater;
    private Messenger mMessenger;
    private final String TAG="busysnail";
	
	public FileListAdapter(Context context, List<FileInfo> fileInfos)
	{
		this.mContext = context;
		this.mFileList = fileInfos;
		this.mInflater=LayoutInflater.from(mContext);
	}

    public void setMessenger(Messenger mMessenger){
        Log.i(TAG,"适配器接收Servicehandler");
        this.mMessenger=mMessenger;
    }
	
	/**
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount()
	{
		return mFileList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ViewHolder holder;
		final FileInfo fileInfo = mFileList.get(position);

		if (null == convertView) {

			convertView = mInflater.inflate(R.layout.list_item, null);
			
			holder = new ViewHolder();

			holder.mTvFileName = (TextView) convertView.findViewById(R.id.tv_filename);
			holder.mTvProgress = (TextView) convertView.findViewById(R.id.tv_progress);
			holder.mBtnStart = (Button) convertView.findViewById(R.id.btn_start);
			holder.mBtnStop = (Button) convertView.findViewById(R.id.btn_stop);
			holder.mPbProgress = (ProgressBar) convertView.findViewById(R.id.pb_progressbar);

			holder.mTvFileName.setText(fileInfo.getFilename());

			convertView.setTag(holder);
		}else{
			holder= (ViewHolder) convertView.getTag();
		}

		int pro= (int) fileInfo.getFinished();
		holder.mPbProgress.setProgress(pro);
		holder.mTvProgress.setText(pro+"%");

		holder.mBtnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				holder.mBtnStart.setEnabled(false);
				holder.mBtnStop.setEnabled(true);
				holder.mTvProgress.setVisibility(View.VISIBLE);
//				Intent intent = new Intent(mContext, DownloadService.class);
//				intent.setAction(DownloadService.ACTION_START);
//				intent.putExtra(DownloadService.FILEINFO,fileInfo);
//				mContext.startService(intent);
                Message msg=new Message();
                msg.what=DownloadService.MSG_START;
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

		return convertView;
	}


	public void updateProgress(int id, int progress)
	{
            FileInfo fileInfo = mFileList.get(id);
            fileInfo.setFinished(progress);
            notifyDataSetChanged();
	}
	
	private static class ViewHolder
	{
		TextView mTvFileName;
		TextView mTvProgress;
		ProgressBar mPbProgress;
		Button mBtnStart;
		Button mBtnStop;
	}

}
