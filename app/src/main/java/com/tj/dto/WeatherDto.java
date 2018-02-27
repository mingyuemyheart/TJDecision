package com.tj.dto;

import java.io.Serializable;

public class WeatherDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//最低温度
	public float[] quarter = new float[2];//四分之一点
	public float[] half = new float[2];//二分之一点
	public float[] threeQuarter = new float[2];//四分之三点
	
	//最高温度
	public float[] quarterH = new float[2];//四分之一点
	public float[] halfH = new float[2];//二分之一点
	public float[] threeQuarterH = new float[2];//四分之三点
	
	
	//平滑曲线
	public int hourlyTemp = 0;//逐小时温度
	public String hourlyTime = null;//逐小时时间
	public int hourlyCode = 0;//天气现象编号
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	
	//列表、趋势
	public String week = null;//周几
	public String date = null;//日期
	public String lowPhe = null;//晚上天气现象
	public int lowPheCode = 0;//晚上天气现象编号
	public int lowTemp = 0;//最低气温
	public float lowX = 0;//最低温度x轴坐标点
	public float lowY = 0;//最低温度y轴坐标点
	public String highPhe = null;//白天天气现象
	public int highPheCode = 0;//白天天气现象编号
	public int highTemp = 0;//最高气温
	public float highX = 0;//最高温度x轴坐标点
	public float highY = 0;//最高温度y轴坐标点
	
}
