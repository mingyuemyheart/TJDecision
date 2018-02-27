package com.baselibrary.news;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baselibrary.R;
import com.baselibrary.utils.CommonUtil;

public class NewsFragmentAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<NewsDto> mArrayList = new ArrayList<NewsDto>();
	private String appid = null;
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvTitle;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public NewsFragmentAdapter(Context context, List<NewsDto> mArrayList, String appId) {
		mContext = context;
		this.appid = appId;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.news_cell, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		NewsDto dto = mArrayList.get(position);
		mHolder.tvTitle.setText(dto.title);
		mHolder.tvTime.setText(dto.time);
		
		if (TextUtils.isEmpty(dto.imgUrl)) {
			if (appid.equals("15")) {//贵州
				mHolder.imageView.setImageResource(R.drawable.iv_guizhou);
			}else if (appid.equals("14")) {//津南
				mHolder.imageView.setImageResource(R.drawable.iv_jinnan);
			}else if (appid.equals("13")) {//西藏
				mHolder.imageView.setImageResource(R.drawable.iv_xizang);
			}
		}else {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.imgUrl, null, (int)(CommonUtil.dip2px(mContext, 5)));
			mHolder.imageView.setBackgroundResource(R.drawable.corner_image_border);
			int dp = (int) CommonUtil.dip2px(mContext, 1);
			mHolder.imageView.setPadding(dp, dp, dp, dp);
		}
		
		return convertView;
	}

}
