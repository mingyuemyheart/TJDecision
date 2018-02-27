package com.tj.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baselibrary.news.NewsDto;
import cxwl.shawn.tj.decision.R;

public class DistrictForecastAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<NewsDto> mArrayList = new ArrayList<NewsDto>();
	
	private final class ViewHolder{
		TextView tvTitle;
	}
	
	private ViewHolder mHolder = null;
	
	public DistrictForecastAdapter(Context context, List<NewsDto> mArrayList) {
		mContext = context;
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
			convertView = mInflater.inflate(R.layout.district_forecast_item, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		NewsDto dto = mArrayList.get(position);
		mHolder.tvTitle.setText(dto.title);
		
		if (dto.isSelected) {
			mHolder.tvTitle.setBackgroundResource(R.drawable.bg_selected);
		}else {
			mHolder.tvTitle.setBackgroundResource(R.drawable.bg_unselected);
		}
		
		return convertView;
	}

}
