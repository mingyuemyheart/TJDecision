package com.baselibrary.routeweather;

/**
 * 高德地图路径搜索适配器
 * @author shawn_sun
 *
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.baselibrary.R;

public class RouteSearchAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PoiItem> mArrayList = new ArrayList<PoiItem>();
	
	private final class ViewHolder{
		TextView tvTitle;
		TextView tvProvince;
	}
	
	private ViewHolder mHolder = null;
	
	public RouteSearchAdapter(Context context, List<PoiItem> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.route_search_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvProvince = (TextView) convertView.findViewById(R.id.tvProvince);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PoiItem dto = mArrayList.get(position);
		if (dto.getTitle() != null) {
			mHolder.tvTitle.setText(dto.getTitle());
		}
		if (dto.getProvinceName() != null && dto.getCityName() != null && dto.getAdName() != null) {
			mHolder.tvProvince.setText(dto.getProvinceName()+dto.getCityName()+dto.getAdName());
		}
		
		return convertView;
	}

}
