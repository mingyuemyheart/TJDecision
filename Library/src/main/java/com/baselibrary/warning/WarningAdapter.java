package com.baselibrary.warning;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import android.annotation.SuppressLint;
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

@SuppressLint("SimpleDateFormat")
public class WarningAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<WarningDto> mArrayList = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	private final class ViewHolder {
		ImageView imageView;//预警icon
		TextView tvName;//预警信息名称
		TextView tvTime;//时间
	}
	
	private ViewHolder mHolder = null;
	
	public WarningAdapter(Context context, List<WarningDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.warning_cell, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		
        Bitmap bitmap = null;
		if (dto.color.equals(CONST.blue[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,dto.id.substring(0, 5).substring(0, 5)+CONST.blue[1]+CONST.imageSuffix);
			if (bitmap != null) {
				mHolder.imageView.setImageBitmap(bitmap);
			}else {
				mHolder.imageView.setImageResource(R.drawable.default_blue);
			}
		}else if (dto.color.equals(CONST.yellow[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,dto.id.substring(0, 5)+CONST.yellow[1]+CONST.imageSuffix);
			if (bitmap != null) {
				mHolder.imageView.setImageBitmap(bitmap);
			}else {
				mHolder.imageView.setImageResource(R.drawable.default_yellow);
			}
		}else if (dto.color.equals(CONST.orange[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,dto.id.substring(0, 5)+CONST.orange[1]+CONST.imageSuffix);
			if (bitmap != null) {
				mHolder.imageView.setImageBitmap(bitmap);
			}else {
				mHolder.imageView.setImageResource(R.drawable.default_orange);
			}
		}else if (dto.color.equals(CONST.red[0])) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,dto.id.substring(0, 5)+CONST.red[1]+CONST.imageSuffix);
			if (bitmap != null) {
				mHolder.imageView.setImageBitmap(bitmap);
			}else {
				mHolder.imageView.setImageResource(R.drawable.default_red);
			}
		}
		
		mHolder.tvName.setText(dto.name);
		try {
			mHolder.tvTime.setText(sdf2.format(sdf1.parse(dto.time)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
