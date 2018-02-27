package com.tj.view;

/**
 * 24小时实况风速曲线图
 */
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.baselibrary.utils.CommonUtil;
import cxwl.shawn.tj.decision.R;
import com.tj.dto.FactCheckDto;

public class WindSpeedView extends View{
	
	private Context mContext = null;
	private List<FactCheckDto> tempList = new ArrayList<FactCheckDto>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private Bitmap newBit = null;
	
	public WindSpeedView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public WindSpeedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public WindSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.iv_winddir);
		newBit = ThumbnailUtils.extractThumbnail(bitmap, (int)(CommonUtil.dip2px(mContext, 10)), (int)(CommonUtil.dip2px(mContext, 10)));
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<FactCheckDto> dataList) {
		if (!dataList.isEmpty()) {
			tempList.addAll(dataList);
			
			maxValue = 0;
			minValue = 0;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxValue <= tempList.get(i).windSpeed && tempList.get(i).windSpeed < 999999) {
					maxValue = tempList.get(i).windSpeed;
				}
				if (minValue >= tempList.get(i).windSpeed) {
					minValue = tempList.get(i).windSpeed;
				}
			}
			
			if (maxValue == 0 && minValue == 0) {
				maxValue = 10;
				minValue = 0;
			}
			
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}
		
		canvas.drawColor(0x30ffffff);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 40);
		float leftMargin = CommonUtil.dip2px(mContext, 30);
		float rightMargin = CommonUtil.dip2px(mContext, 10);
		float topMargin = CommonUtil.dip2px(mContext, 20);
		float bottomMargin = CommonUtil.dip2px(mContext, 20);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度
		
//		canvas.drawColor(Color.BLACK);
//		Paint pp = new Paint();
//		pp.setColor(Color.RED);
//		pp.setStyle(Style.FILL);
//		canvas.drawRect(CommonUtil.dip2px(mContext, 10), CommonUtil.dip2px(mContext, 10), w-CommonUtil.dip2px(mContext, 10), h-bottomMargin, pp);
		
		int size = tempList.size();
		
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			FactCheckDto dto = tempList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;
			
			float value = tempList.get(i).windSpeed;
			if (value >= 0) {
				dto.y = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (minValue >= 0) {
					dto.y = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}else {
				dto.y = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (maxValue < 0) {
					dto.y = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}
			tempList.set(i, dto);
		}
		
		//绘制刻度线，每间隔为20
		float totalDivider = (Math.abs(maxValue)+Math.abs(minValue));
		float itemDivider = 0.5f;
		if (totalDivider <= 1) {
			itemDivider = 0.5f;
		}else if (totalDivider <= 5 && totalDivider > 1) {
			itemDivider = 1;
		}else if (totalDivider <= 10 && totalDivider > 5) {
			itemDivider = 2;
		}else if (totalDivider > 10) {
			itemDivider = 5;
		}
		for (float i = 0; i <= totalDivider; i+=itemDivider) {
			float dividerY = 0;
			float value = i;
			if (value >= 0) {
				dividerY = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (minValue >= 0) {
					dividerY = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}else {
				dividerY = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (maxValue < 0) {
					dividerY = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}
			lineP.setColor(0x30ffffff);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			canvas.drawText(String.valueOf(i), CommonUtil.dip2px(mContext, 5), dividerY-CommonUtil.dip2px(mContext, 2), textP);
		}
		
		//绘制曲线
		Path linePath = new Path();
		for (int i = 0; i < tempList.size(); i++) {
			FactCheckDto dto = tempList.get(i);
			if (i == 0) {
				linePath.moveTo(dto.x, dto.y);
			}else {
				linePath.lineTo(dto.x, dto.y);
			}
		}
		lineP.setColor(Color.WHITE);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		canvas.drawPath(linePath, lineP);
		
		for (int i = 0; i < tempList.size(); i++) {
			FactCheckDto dto = tempList.get(i);
			
			if (dto.windSpeed < 999999) {
				//绘制区域
				if (i != tempList.size()-1) {
					Path rectPath = new Path();
					rectPath.moveTo(dto.x, dto.y);
					rectPath.lineTo(tempList.get(i+1).x, tempList.get(i+1).y);
					rectPath.lineTo(tempList.get(i+1).x, h-bottomMargin);
					rectPath.lineTo(dto.x, h-bottomMargin);
					rectPath.close();
					lineP.setColor(0x30ffffff);
					lineP.setStyle(Style.FILL);
					canvas.drawPath(rectPath, lineP);
				}
				
				//绘制风向标
				if (dto.windSpeed > 0) {
					Matrix matrix = new Matrix();
					matrix.postScale(1, 1);
					matrix.postRotate(dto.windDir);
					Bitmap b = Bitmap.createBitmap(newBit, 0, 0, newBit.getWidth(), newBit.getHeight(), matrix, true);
					canvas.drawBitmap(b, dto.x-b.getWidth()/2, dto.y-b.getHeight()/2, textP);
				}
				
				//绘制曲线上每个点的数据值
				textP.setColor(Color.WHITE);
				textP.setTextSize(CommonUtil.dip2px(mContext, 8));
				float tempWidth = textP.measureText(String.valueOf(tempList.get(i).windSpeed));
				canvas.drawText(String.valueOf(tempList.get(i).windSpeed), dto.x-tempWidth/2, dto.y-CommonUtil.dip2px(mContext, 10f), textP);
				
				//绘制24小时
				if (!TextUtils.isEmpty(dto.time)) {
					textP.setColor(Color.WHITE);
					textP.setTextSize(CommonUtil.dip2px(mContext, 10));
					float text = textP.measureText(dto.time);
					canvas.drawText(dto.time, dto.x-text/2, h-CommonUtil.dip2px(mContext, 5f), textP);
				}
			}
			
		}
		
	}

}
