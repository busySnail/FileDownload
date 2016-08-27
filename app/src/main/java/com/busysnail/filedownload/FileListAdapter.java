package com.busysnail.filedownload;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.busysnail.filedownload.entity.FileInfo;
import com.busysnail.filedownload.services.DownloadService;

import java.util.List;

public class FileListAdapter extends BaseAdapter
{
	private Context mContext;
	private List<FileInfo> mList;
	
	public FileListAdapter(Context context, List<FileInfo> fileInfos)
	{
		this.mContext = context;
		this.mList = fileInfos;
	}
	
	/**
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount()
	{
		return mList.size();
	}

	/**
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		final FileInfo fileInfo = mList.get(position);
		
		if (convertView != null)
		{
			viewHolder = (ViewHolder) convertView.getTag();
			
			if (!viewHolder.mFileName.getTag().equals(Integer.valueOf(fileInfo.getId())))
			{
				convertView = null;
			}
		}
		
		if (null == convertView)
		{
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.list_item, null);
			
			viewHolder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.tv_filename),
					(ProgressBar) convertView.findViewById(R.id.pb_progress),
					(Button) convertView.findViewById(R.id.btn_start),
					(Button) convertView.findViewById(R.id.btn_stop)
					);
			convertView.setTag(viewHolder);
			
			viewHolder.mFileName.setText(fileInfo.getFilename());
			viewHolder.mProgressBar.setMax(100);
			viewHolder.mStartBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// 通知Service开始下载
					Intent intent = new Intent(mContext, DownloadService.class);
					intent.setAction(DownloadService.ACTION_START);
					intent.putExtra(DownloadService.FILEINFO, fileInfo);
					mContext.startService(intent);
				}
			});
			viewHolder.mStopBtn.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(mContext, DownloadService.class);
					intent.setAction(DownloadService.ACTION_STOP);
					intent.putExtra(DownloadService.FILEINFO, fileInfo);
					mContext.startService(intent);
				}
			});
			
			// 将viewHolder.mFileName的Tag设为fileInfo的ID，用于唯一标识viewHolder.mFileName
			viewHolder.mFileName.setTag(Integer.valueOf(fileInfo.getId()));
		}
		
		viewHolder.mProgressBar.setProgress(fileInfo.getFinished());
		
		return convertView;
	}

	/** 
	 * 更新列表项中的进度条
	 * @param id
	 * @param progress
	 * @return void
	 * @author Yann
	 * @date 2015-8-9 下午1:34:14
	 */ 
	public void updateProgress(int id, int progress)
	{
		FileInfo fileInfo = mList.get(id);
		fileInfo.setFinished(progress);
		notifyDataSetChanged();
	}
	
	private static class ViewHolder
	{
		TextView mFileName;
		ProgressBar mProgressBar;
		Button mStartBtn;
		Button mStopBtn;

		public ViewHolder(TextView mFileName, ProgressBar mProgressBar,
				Button mStartBtn, Button mStopBtn)
		{
			this.mFileName = mFileName;
			this.mProgressBar = mProgressBar;
			this.mStartBtn = mStartBtn;
			this.mStopBtn = mStopBtn;
		}
	}
	

	@Override
	public Object getItem(int position)
	{
		return mList.get(position);
	}


	@Override
	public long getItemId(int position)
	{
		return position;
	}
}
