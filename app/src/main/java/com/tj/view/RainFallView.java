package com.tj.view;

/**
 * 24小时实况降水量曲线图
 */
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.baselibrary.utils.CommonUtil;
import com.tj.dto.FactCheckDto;

public class RainFallView extends View{
	
	private Context mContext = null;
	private List<FactCheckDto> tempList = new ArrayList<FactCheckDto>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	
	public RainFallView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public RainFallView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public RainFallView(Context context, AttributeSet attrs, int defStyleAttr) {
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
				if (maxValue <= tempList.get(i).rainFall && tempList.get(i).rainFall < 999999) {
					maxValue = tempList.get(i).rainFall;
				}
				if (minValue >= tempList.get(i).rainFall) {
					minValue = tempList.get(i).rainFall;
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
			
			float value = tempList.get(i).rainFall;
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
		float itemDivider = 0.1f;
		if (totalDivider <= 0.5f) {
			itemDivider = 0.1f;
		}else if (totalDivider <= 1 && totalDivider > 0.5f) {
			itemDivider = 0.2f;
		}else if (totalDivider <= 2 && totalDivider > 1f) {
			itemDivider = 0.5f;
		}else if (totalDivider <= 5 && totalDivider > 2f) {
			itemDivider = 1f;
		}else if (totalDivider <= 10 && totalDivider > 5f) {
			itemDivider = 2f;
		}else if (totalDivider > 10) {
			itemDivider = 5;
		}
		
		for (float i = 0; i <= totalDivider; i+=itemDivider) {
//			float dividerX = CommonUtil.dip2px(mContext, 5);
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
		
//		//绘制曲线
//		Path linePath = new Path();
//		for (int i = 0; i < tempList.size(); i++) {
//			TrendDto dto = tempList.get(i);
//			if (i == 0) {
//				linePath.moveTo(dto.x, dto.y);
//			}else {
//				linePath.lineTo(dto.x, dto.y);
//			}
//		}
//		lineP.setColor(Color.WHITE);
//		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//		canvas.drawPath(linePath, lineP);
		
		for (int i = 0; i < tempList.size(); i++) {
			FactCheckDto dto = tempList.get(i);
			
			if (dto.rainFall < 999999) {
				lineP.setColor(Color.WHITE);
				lineP.setStyle(Style.FILL);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
				canvas.drawRect(dto.x-CommonUtil.dip2px(mContext, 4), dto.y, dto.x+CommonUtil.dip2px(mContext, 4), h-bottomMargin, lineP);
				
				//绘制曲线上每个点的数据值
				textP.setColor(Color.WHITE);
				textP.setTextSize(CommonUtil.dip2px(mContext, 8));
				float tempWidth = textP.measureText(String.valueOf(tempList.get(i).rainFall));
				canvas.drawText(String.valueOf(tempList.get(i).rainFall), dto.x-tempWidth/2, dto.y-CommonUtil.dip2px(mContext, 5f), textP);
				
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
