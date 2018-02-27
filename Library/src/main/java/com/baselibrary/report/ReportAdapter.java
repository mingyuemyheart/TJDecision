package com.baselibrary.report;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baselibrary.R;
import com.baselibrary.utils.WeatherUtil;

public class ReportAdapter extends BaseAdapter {
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<ReportDto> mArrayList = new ArrayList<ReportDto>();
	public int clickPosition = -1;
	
	public final class ViewHolder{
		ImageView imageView;
		TextView tvPhenomenon;
		LinearLayout layout;
	}
	
	public ViewHolder mHolder = null;
	
	public ReportAdapter(Context context, List<ReportDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.report_item, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvPhenomenon = (TextView) convertView.findViewById(R.id.tvPhenomenon);
			mHolder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ReportDto dto = mArrayList.get(position);
		int code = dto.getPhenomenonCode();
		if (code != -1) {//排除掉空的
			mHolder.tvPhenomenon.setVisibility(View.VISIBLE);
			mHolder.imageView.setVisibility(View.VISIBLE);
			mHolder.layout.setVisibility(View.VISIBLE);
			mHolder.tvPhenomenon.setText(WeatherUtil.getWeatherId(code));
			mHolder.imageView.setImageBitmap(WeatherUtil.getBitmap(mContext, code));
			if (clickPosition == position) {
				mHolder.layout.setBackgroundResource(R.drawable.phenomenon_bg_press);
			}else {
				mHolder.layout.setBackgroundResource(R.drawable.phenomenon_bg);
			}
		}else {
			mHolder.tvPhenomenon.setVisibility(View.INVISIBLE);
			mHolder.imageView.setVisibility(View.INVISIBLE);
			mHolder.layout.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

}
