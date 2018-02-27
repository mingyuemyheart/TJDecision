package com.tj.adapter;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baselibrary.common.ColumnData;
import cxwl.shawn.tj.decision.R;

public class QuickPaperAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<ColumnData> mArrayList = new ArrayList<ColumnData>();
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvTitle;
		TextView tvCount;
	}
	
	private ViewHolder mHolder = null;
	
	public QuickPaperAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.quick_paper_cell, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvCount = (TextView) convertView.findViewById(R.id.tvCount);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ColumnData dto = mArrayList.get(position);
		mHolder.tvTitle.setText(dto.name);
		
		String str1 = mContext.getString(R.string.count_person);
		String str2 = mContext.getString(R.string.count_pian);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(dto.newsType).append(str1);
		buffer.append(dto.newsCount).append(str2);
		SpannableStringBuilder builder = new SpannableStringBuilder(buffer.toString());
		ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(mContext.getResources().getColor(R.color.yellow));
		ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(mContext.getResources().getColor(R.color.white));
		ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(mContext.getResources().getColor(R.color.yellow));
		ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(mContext.getResources().getColor(R.color.white));
		
		builder.setSpan(builderSpan1, 0, dto.newsType.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.setSpan(builderSpan2, dto.newsType.length(), dto.newsType.length()+str1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.setSpan(builderSpan3, dto.newsType.length()+str1.length(), dto.newsType.length()+str1.length()+dto.newsCount.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.setSpan(builderSpan4, dto.newsType.length()+str1.length()+dto.newsCount.length(), dto.newsType.length()+str1.length()+dto.newsCount.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		mHolder.tvCount.setText(builder);
		
		FinalBitmap finalBitmap = FinalBitmap.create(mContext);
		finalBitmap.display(mHolder.imageView, dto.icon, null, 0);
		
		return convertView;
	}

}
