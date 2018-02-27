package com.baselibrary.trend;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baselibrary.R;
import com.baselibrary.stickygridheaders.StickyGridHeadersSimpleAdapter;

public class TrendAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private Context mContext = null;
	private List<TrendDto> mArrayList = null;
	private LayoutInflater mInflater = null;

	public TrendAdapter(Context context, List<TrendDto> mArrayList) {
		this.mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	private ViewHolder mHolder = null;
	
	private class ViewHolder {
		TextView tvStreetName;
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.sticky_grid_item, null);
			mHolder.tvStreetName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		TrendDto dto = mArrayList.get(position);
		mHolder.tvStreetName.setText(dto.streetName);

		return convertView;
	}
	
	private HeaderViewHolder mHeaderHolder = null;

	private class HeaderViewHolder {
		TextView tvAreaName;
	}

	@Override
	public long getHeaderId(int position) {
		return mArrayList.get(position).section;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.sticky_grid_header, null);
			mHeaderHolder.tvAreaName = (TextView) convertView.findViewById(R.id.tvCityName);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}
		
		TrendDto dto = mArrayList.get(position);
		mHeaderHolder.tvAreaName.setText(dto.areaName);

		return convertView;
	}

}
