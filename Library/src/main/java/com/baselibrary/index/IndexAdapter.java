package com.baselibrary.index;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CommonUtil;

public class IndexAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<IndexDto> mArrayList = null;
	
	private final class ViewHolder {
		private ImageView imageView;
		private TextView tvName;
		private TextView tvLevel_zh;
		private ImageView ivArrow;
		private TextView tvIntro_zh;
	}
	
	private ViewHolder mHolder = null;
	
	public IndexAdapter(Context context, List<IndexDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.index_cell, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvLevel_zh = (TextView) convertView.findViewById(R.id.tvLevel_zh);
			mHolder.ivArrow = (ImageView) convertView.findViewById(R.id.ivArrow);
			mHolder.tvIntro_zh = (TextView) convertView.findViewById(R.id.tvIntro_zh);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		IndexDto dto = mArrayList.get(position);
		
		mHolder.tvName.setText(dto.name);
		mHolder.tvLevel_zh.setText(dto.level_zh);
		if (dto.isExpand) {
			mHolder.ivArrow.setImageResource(R.drawable.iv_arrow_up);
			mHolder.tvIntro_zh.setVisibility(View.VISIBLE);
		}else {
			mHolder.ivArrow.setImageResource(R.drawable.iv_arrow_down);
			mHolder.tvIntro_zh.setVisibility(View.GONE);
		}
		mHolder.tvIntro_zh.setText(dto.intro_zh);
		
		Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,dto.abbr+CONST.imageSuffix);
		if (bitmap != null) {
			mHolder.imageView.setImageBitmap(bitmap);
		}
		
		return convertView;
	}

}
