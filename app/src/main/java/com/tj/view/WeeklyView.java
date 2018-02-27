package com.tj.view;

/**
 * 一周预报曲线图
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;

import cxwl.shawn.tj.decision.R;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.WeatherUtil;
import com.tj.dto.WeatherDto;

public class WeeklyView extends View{
	
	private Context mContext = null;
	private MyThread mThread = null;
	private List<WeatherDto> tempList = new ArrayList<WeatherDto>();
	private int maxTemp = 0;//最高温度
	private int minTemp = 0;//最低温度
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private Bitmap lowBit = null;//低温图标
	private Bitmap highBit = null;//高温图标
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");
	
	public WeeklyView(Context context) {
		super(context);
		mContext = context;
//		startThread();
		init();
	}
	
	public WeeklyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
//		startThread();
		init();
	}
	
	public WeeklyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
//		startThread();
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
		
		lowBit = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_low), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 30)));
		highBit = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_high), 
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 30)));
	}
	
	/**
	 * 对polyView进行赋值
	 */
	public void setData(List<WeatherDto> dataList) {
		if (!dataList.isEmpty()) {
			tempList.addAll(dataList);
			
			maxTemp = tempList.get(0).highTemp;
			minTemp = tempList.get(0).lowTemp;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxTemp <= tempList.get(i).highTemp) {
					maxTemp = tempList.get(i).highTemp;
				}
				if (minTemp >= tempList.get(i).lowTemp) {
					minTemp = tempList.get(i).lowTemp;
				}
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 80);
		float chartH = h-CommonUtil.dip2px(mContext, 240);
		float leftMargin = CommonUtil.dip2px(mContext, 40);
		float rightMargin = CommonUtil.dip2px(mContext, 40);
		float topMargin = CommonUtil.dip2px(mContext, 140);
		float bottomMargin = CommonUtil.dip2px(mContext, 100);
		float chartMaxH = chartH * maxTemp / (Math.abs(maxTemp)+Math.abs(minTemp));//同时存在正负值时，正值高度
		float chartMinH = chartH * minTemp / (Math.abs(maxTemp)+Math.abs(minTemp));//同时存在正负值时，负值高度
		
		int size = tempList.size();
		
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);
			
			//获取最低温度的对应的坐标点信息
			dto.lowX = (chartW/(size-1))*i + leftMargin;
			float lowTemp = tempList.get(i).lowTemp;
			if (lowTemp >= 0) {
				dto.lowY = chartMaxH - chartH*Math.abs(lowTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				if (minTemp >= 0) {
					dto.lowY = chartH - chartH*Math.abs(lowTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				}
			}else {
				dto.lowY = chartMaxH + chartH*Math.abs(lowTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				if (maxTemp < 0) {
					dto.lowY = chartH*Math.abs(lowTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				}
			}
			
			//获取最高温度对应的坐标点信息
			dto.highX = (chartW/(size-1))*i + leftMargin;
			float highTemp = tempList.get(i).highTemp;
			if (highTemp >= 0) {
				dto.highY = chartMaxH - chartH*Math.abs(highTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				if (minTemp >= 0) {
					dto.highY = chartH - chartH*Math.abs(highTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				}
			}else {
				dto.highY = chartMaxH + chartH*Math.abs(highTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				if (maxTemp < 0) {
					dto.highY = chartH*Math.abs(highTemp)/(Math.abs(maxTemp)+Math.abs(minTemp)) + topMargin;
				}
			}
			
			tempList.set(i, dto);
		}
		
		for (int i = 0; i < tempList.size(); i++) {
			WeatherDto dto = tempList.get(i);
			
			//绘制周几、日期、天气现象和天气现象图标
			textP.setColor(getResources().getColor(R.color.white));
			textP.setTextSize(getResources().getDimension(R.dimen.level_5));
			String week = mContext.getString(R.string.week)+dto.week.substring(dto.week.length()-1, dto.week.length());
			if (i == 0) {
				week = mContext.getString(R.string.today);
			}
			float weekText = textP.measureText(week);
			canvas.drawText(week, dto.highX-weekText/2, CommonUtil.dip2px(mContext, 20), textP);
			
			try {
				String date = sdf2.format(sdf1.parse(dto.date));
				float dateText = textP.measureText(date);
				canvas.drawText(date, dto.highX-dateText/2, CommonUtil.dip2px(mContext, 40), textP);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			float highPheText = textP.measureText(dto.highPhe);//天气现象字符串占像素宽度
			canvas.drawText(dto.highPhe, dto.highX-highPheText/2, CommonUtil.dip2px(mContext, 70), textP);
			
			Bitmap b = WeatherUtil.getBitmap(mContext, dto.highPheCode);
			Bitmap newBit = ThumbnailUtils.extractThumbnail(b, (int)(CommonUtil.dip2px(mContext, 20)), (int)(CommonUtil.dip2px(mContext, 20)));
			canvas.drawBitmap(newBit, dto.highX-newBit.getWidth()/2, CommonUtil.dip2px(mContext, 80), textP);
			
			Bitmap lb = WeatherUtil.getBitmap(mContext, dto.lowPheCode);
			Bitmap newLbit = ThumbnailUtils.extractThumbnail(lb, (int)(CommonUtil.dip2px(mContext, 20)), (int)(CommonUtil.dip2px(mContext, 20)));
			canvas.drawBitmap(newLbit, dto.lowX-newLbit.getWidth()/2, h-CommonUtil.dip2px(mContext, 50), textP);
			
			float lowPheText = textP.measureText(dto.lowPhe);//天气现象字符串占像素宽度
			canvas.drawText(dto.lowPhe, dto.lowX-lowPheText/2, h-CommonUtil.dip2px(mContext, 10), textP);
			
			//绘制纵向分隔线
			if (i < tempList.size()-1) {
				lineP.setColor(0x30ffffff);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
				canvas.drawLine((tempList.get(i+1).highX-dto.highX)/2+dto.highX, CommonUtil.dip2px(mContext, 60), (tempList.get(i+1).highX-dto.highX)/2+dto.highX, h-CommonUtil.dip2px(mContext, 10), lineP);
			}
			
			//绘制曲线上每个时间点上的圆点marker
			lineP.setColor(getResources().getColor(R.color.low_color));
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
			canvas.drawPoint(dto.lowX, dto.lowY, lineP);
			lineP.setColor(getResources().getColor(R.color.high_color));
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
			canvas.drawPoint(dto.highX, dto.highY, lineP);
			
			//绘制曲线上每个时间点的温度值
			textP.setColor(getResources().getColor(R.color.white));
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			canvas.drawBitmap(highBit, dto.highX-highBit.getWidth()/2, dto.highY-highBit.getHeight()-CommonUtil.dip2px(mContext, 2.5f), textP);
			float highText = textP.measureText(String.valueOf(tempList.get(i).highTemp));//高温字符串占像素宽度
			canvas.drawText(String.valueOf(tempList.get(i).highTemp), dto.highX-highText/2, dto.highY-highBit.getHeight()/2, textP);
			
			textP.setColor(getResources().getColor(R.color.white));
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			canvas.drawBitmap(lowBit, dto.lowX-lowBit.getWidth()/2, dto.lowY+CommonUtil.dip2px(mContext, 2.5f), textP);
			float lowText = textP.measureText(String.valueOf(tempList.get(i).lowTemp));//低温字符串所占的像素宽度
			canvas.drawText(String.valueOf(tempList.get(i).lowTemp), dto.lowX-lowText/2, dto.lowY+lowBit.getHeight()/2+CommonUtil.dip2px(mContext, 10), textP);
		}
		//绘制两个边缘的纵向分隔线
		lineP.setColor(0x30ffffff);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
		canvas.drawLine(tempList.get(0).highX - (tempList.get(1).highX-tempList.get(0).highX)/2, CommonUtil.dip2px(mContext, 60), tempList.get(0).highX - (tempList.get(1).highX-tempList.get(0).highX)/2, h-CommonUtil.dip2px(mContext, 10), lineP);
		canvas.drawLine(tempList.get(size-1).highX + (tempList.get(1).highX-tempList.get(0).highX)/2, CommonUtil.dip2px(mContext, 60), tempList.get(size-1).highX + (tempList.get(1).highX-tempList.get(0).highX)/2, h-CommonUtil.dip2px(mContext, 10), lineP);
//		canvas.drawLine(CommonUtil.dip2px(mContext, 5), CommonUtil.dip2px(mContext, 50), CommonUtil.dip2px(mContext, 5), h-bottomMargin, lineP);
//		canvas.drawLine(w-CommonUtil.dip2px(mContext, 5), CommonUtil.dip2px(mContext, 50), w-CommonUtil.dip2px(mContext, 5), h-bottomMargin, lineP);
		
		//绘制最低温度、最高温度曲线
		Path pathLow = new Path();
		Path pathHigh = new Path();
		for (int i = 0; i < tempList.size(); i++) {
			WeatherDto dto = tempList.get(i);
			if (i == 0) {
				pathLow.moveTo(dto.lowX, dto.lowY);
				pathHigh.moveTo(dto.highX, dto.highY);
			}else {
				pathLow.lineTo(dto.lowX, dto.lowY);
				pathHigh.lineTo(dto.highX, dto.highY);
			}
		}
		lineP.setColor(getResources().getColor(R.color.low_color));
		lineP.setStrokeWidth(5.0f);
		canvas.drawPath(pathLow, lineP);
		lineP.setColor(getResources().getColor(R.color.high_color));
		lineP.setStrokeWidth(5.0f);
		canvas.drawPath(pathHigh, lineP);
		
//		//获取曲线上控制点的坐标
//		cubicList.clear();//再次重绘钱要清空list
//		for (int i = 0; i < size; i++) {
//			if (i == size-1) {//这里要获取9个点之间的8条线段，所以少遍历一次
//				break;
//			}
//			if (i % 2 == 0) {
//				float[] quarter = getPoint(tempList.get(i).x, tempList.get(i).y, tempList.get(i+1).x, tempList.get(i+1).y, 0.25f);
//				float[] half = getPoint(tempList.get(i).x, tempList.get(i).y, tempList.get(i+1).x, tempList.get(i+1).y, 0.5f);
//				float[] threeQuarter = getPoint(tempList.get(i).x, tempList.get(i).y, tempList.get(i+1).x, tempList.get(i+1).y, 0.75f);
//				CubicViewDto dto = new CubicViewDto();
//				dto.quarter = quarter;
//				dto.half = half;
//				dto.threeQuarter = threeQuarter;
//				cubicList.add(dto);
//			}else {
//				float[] quarter = getPoint2(tempList.get(i).x, tempList.get(i).y, tempList.get(i+1).x, tempList.get(i+1).y, 0.25f);
//				float[] half = getPoint2(tempList.get(i).x, tempList.get(i).y, tempList.get(i+1).x, tempList.get(i+1).y, 0.5f);
//				float[] threeQuarter = getPoint2(tempList.get(i).x, tempList.get(i).y, tempList.get(i+1).x, tempList.get(i+1).y, 0.75f);
//				CubicViewDto dto = new CubicViewDto();
//				dto.quarter = quarter;
//				dto.half = half;
//				dto.threeQuarter = threeQuarter;
//				cubicList.add(dto);
//			}
//		}
//		
//		//绘制贝塞尔曲线
//		Path path = new Path();
//		path.moveTo(tempList.get(0).x, tempList.get(0).y);
//		for (int j = 0; j < cubicList.size(); j++) {
//			path.cubicTo(cubicList.get(j).half[0], cubicList.get(j).quarter[1], cubicList.get(j).half[0], cubicList.get(j).threeQuarter[1], tempList.get(j+1).x, tempList.get(j+1).y);
//		}
	}
	
	/**
	 * 获取四分之一、二分之一、四分之三点的信息
	 * @param x0 第一个点x轴坐标
	 * @param y0 第一个点y轴坐标
	 * @param x1 第二个点x轴坐标
	 * @param y1 第二个点y轴坐标
	 * @param section 0.25、0.5、0.75
	 * @return
	 */
	private float[] getPoint(float x0, float y0, float x1, float y1, float section) {
		float x = Math.abs(x1-x0)*section + x0;
		float y = Math.abs(Math.abs(y1-y0)*section - y0);
		return new float[] {x, y};
	}
	
	private float[] getPoint2(float x0, float y0, float x1, float y1, float section) {
		float x = Math.abs(x1-x0)*section + x0;
		float y = Math.abs(Math.abs(y1-y0)*section + y0);
		return new float[] {x, y};
	}
	
	private void startThread() {
		if (mThread != null) {
			mThread.cancel();
			mThread = null;
		}
		
		mThread = new MyThread();
		mThread.start();
	}
	
	private class MyThread extends Thread {
		static final int STATE_START = 0;
		static final int STATE_CANCEL = 1;
		private int state;
		
		@Override
		public void run() {
			super.run();
			this.state = STATE_START;
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				postInvalidate();
			}
		}
		
		public void cancel() {
			this.state = STATE_CANCEL;
		}
		
	}

}
