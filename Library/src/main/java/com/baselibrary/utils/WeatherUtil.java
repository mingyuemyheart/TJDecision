package com.baselibrary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.baselibrary.R;

public class WeatherUtil {
	
	private static final String SEPARATER = "\\|";
	private static final String NONE = "\\?";
	
	/**
	 * 获取最后一个值
	 * @param values
	 * @return
	 */
	public static final String lastValue(String values) {
		String temp = values.replaceAll(NONE, "");
		String[] vs = TextUtils.isEmpty(temp) ? null : temp.split(SEPARATER);
		String value = (vs == null || vs.length <= 0) ? null : vs[vs.length - 1];
		return value;
	}
	
	/**
	 * 获取空气质量
	 * @param context
	 * @param aqi
	 * @return
	 */
	public static String getAqi(Context context, int aqi) {
		if (aqi <= 50) {
			return context.getString(R.string.aqi0);
		} else if (aqi >= 51 && aqi <= 100)  {
			return context.getString(R.string.aqi1);
		} else if (aqi >= 101 && aqi <= 150)  {
			return context.getString(R.string.aqi2);
		} else if (aqi >= 151 && aqi <= 200)  {
			return context.getString(R.string.aqi3);
		} else if (aqi >= 201 && aqi <= 300)  {
			return context.getString(R.string.aqi4);
		} else if (aqi >= 301)  {
			return context.getString(R.string.aqi5);
		}
		return "";
	}
	
	/**
	 * 根据天气现象code获取天气现象字符串
	 * @param code
	 * @return
	 */
	public static final int getWeatherId(int code) {
		int id = 0;
		switch (code) {
		case 0:
			id = R.string.weather0;
			break;
		case 1:
			id = R.string.weather1;
			break;
		case 2:
			id = R.string.weather2;
			break;
		case 3:
			id = R.string.weather3;
			break;
		case 4:
			id = R.string.weather4;
			break;
		case 5:
			id = R.string.weather5;
			break;
		case 6:
			id = R.string.weather6;
			break;
		case 7:
			id = R.string.weather7;
			break;
		case 8:
			id = R.string.weather8;
			break;
		case 9:
			id = R.string.weather9;
			break;
		case 10:
			id = R.string.weather10;
			break;
		case 11:
			id = R.string.weather11;
			break;
		case 12:
			id = R.string.weather12;
			break;
		case 13:
			id = R.string.weather13;
			break;
		case 14:
			id = R.string.weather14;
			break;
		case 15:
			id = R.string.weather15;
			break;
		case 16:
			id = R.string.weather16;
			break;
		case 17:
			id = R.string.weather17;
			break;
		case 18:
			id = R.string.weather18;
			break;
		case 19:
			id = R.string.weather19;
			break;
		case 20:
			id = R.string.weather20;
			break;
		case 21:
			id = R.string.weather21;
			break;
		case 22:
			id = R.string.weather22;
			break;
		case 23:
			id = R.string.weather23;
			break;
		case 24:
			id = R.string.weather24;
			break;
		case 25:
			id = R.string.weather25;
			break;
		case 26:
			id = R.string.weather26;
			break;
		case 27:
			id = R.string.weather27;
			break;
		case 28:
			id = R.string.weather28;
			break;
		case 29:
			id = R.string.weather29;
			break;
		case 30:
			id = R.string.weather30;
			break;
		case 31:
			id = R.string.weather31;
			break;
		case 32:
			id = R.string.weather32;
			break;
		case 33:
			id = R.string.weather33;
			break;
		case 49:
			id = R.string.weather49;
			break;
		case 53:
			id = R.string.weather53;
			break;
		case 54:
			id = R.string.weather54;
			break;
		case 55:
			id = R.string.weather55;
			break;
		case 56:
			id = R.string.weather56;
			break;
		case 57:
			id = R.string.weather57;
			break;
		case 58:
			id = R.string.weather58;
			break;
		default:
			id = R.string.weather0;
			break;
		}
		return id;
	}
	
	/**
	 * 根据风向编号获取风向字符串
	 * @param code
	 * @return
	 */
	public static final int getWindDirection(int code) {
		int id = 0;
		switch (code) {
		case 0:
			id = R.string.wind_dir0;
			break;
		case 1:
			id = R.string.wind_dir1;
			break;
		case 2:
			id = R.string.wind_dir2;
			break;
		case 3:
			id = R.string.wind_dir3;
			break;
		case 4:
			id = R.string.wind_dir4;
			break;
		case 5:
			id = R.string.wind_dir5;
			break;
		case 6:
			id = R.string.wind_dir6;
			break;
		case 7:
			id = R.string.wind_dir7;
			break;
		case 8:
			id = R.string.wind_dir8;
			break;
		case 9:
			id = R.string.wind_dir9;
			break;
		default:
			id = R.string.wind_dir0;
			break;
		}
		return id;
	}
	
	/**
	 * 根据风力编号获取风力字符串
	 * @param code
	 * @return
	 */
	public static final int getWindForce(int code) {
		int id = 0;
		switch (code) {
		case 0:
			id = R.string.wind_force0;
			break;
		case 1:
			id = R.string.wind_force1;
			break;
		case 2:
			id = R.string.wind_force2;
			break;
		case 3:
			id = R.string.wind_force3;
			break;
		case 4:
			id = R.string.wind_force4;
			break;
		case 5:
			id = R.string.wind_force5;
			break;
		case 6:
			id = R.string.wind_force6;
			break;
		case 7:
			id = R.string.wind_force7;
			break;
		case 8:
			id = R.string.wind_force8;
			break;
		case 9:
			id = R.string.wind_force9;
			break;
		default:
			id = R.string.wind_force0;
			break;
		}
		return id;
	}
	
	/**
	 * 根据天气现象编号获取相应的图标
	 * @param context
	 * @param code 天气现象编号
	 * @return
	 */
	public static final Bitmap getBitmap(Context context, int code) {
		Bitmap bitmap = null;
		
		if (code == 0) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day00_mini);
		}else if (code == 1) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day01_mini);
		}else if (code == 2) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day02_mini);
		}else if (code == 3) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day03_mini);
		}else if (code == 4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day04_mini);
		}else if (code == 5) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day05_mini);
		}else if (code == 6) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day06_mini);
		}else if (code == 7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 8) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 9) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 10) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 11) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 12) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 13) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day13_mini);
		}else if (code == 14) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 15) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 16) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 17) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 18) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day18_mini);
		}else if (code == 19) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 20) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day29_mini);
		}else if (code == 21) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 22) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 23) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 24) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 25) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day07_mini);
		}else if (code == 26) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 27) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 28) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 29) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day29_mini);
		}else if (code == 30) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day29_mini);
		}else if (code == 31) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day29_mini);
		}else if (code == 32) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day18_mini);
		}else if (code == 33) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day14_mini);
		}else if (code == 49) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day18_mini);
		}else if (code == 53) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day53_mini);
		}else if (code == 54) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day53_mini);
		}else if (code == 55) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day53_mini);
		}else if (code == 56) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day53_mini);
		}else if (code == 57) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day18_mini);
		}else if (code == 58) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.day18_mini);
		}
		
		return bitmap;
	}
	
}
