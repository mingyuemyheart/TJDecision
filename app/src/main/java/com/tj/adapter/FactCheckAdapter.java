package com.tj.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cxwl.shawn.tj.decision.R;
import com.tj.dto.FactCheckDto;

public class FactCheckAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<FactCheckDto> mArrayList = new ArrayList<FactCheckDto>();
	
	private final class ViewHolder{
		TextView tvStationName;
		TextView tvStationId;
	}
	
	private ViewHolder mHolder = null;
	
	public FactCheckAdapter(Context context, List<FactCheckDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.fact_check_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvStationName = (TextView) convertView.findViewById(R.id.tvStationName);
			mHolder.tvStationId = (TextView) convertView.findViewById(R.id.tvStationId);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		FactCheckDto dto = mArrayList.get(position);
		mHolder.tvStationName.setText(dto.stationName);
		mHolder.tvStationId.setText(mContext.getString(R.string.station_id) + dto.stationId);
		
		return convertView;
	}

}
