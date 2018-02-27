package com.tj.adapter;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baselibrary.common.ColumnData;
import cxwl.shawn.tj.decision.R;

public class UniqueAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<ColumnData> mArrayList = new ArrayList<ColumnData>();
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvName;
	}
	
	private ViewHolder mHolder = null;
	
	public UniqueAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.unique_item, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ColumnData dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.name);
		
		FinalBitmap finalBitmap = FinalBitmap.create(mContext);
		finalBitmap.display(mHolder.imageView, dto.icon2, null, 0);
		
		return convertView;
	}

}
