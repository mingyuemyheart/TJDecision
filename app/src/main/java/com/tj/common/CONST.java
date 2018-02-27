package com.tj.common;

import com.amap.api.maps.model.LatLng;

public class CONST {
	
	public static String APPID = "14";//济南客户端对应服务器的appid
	public static final String TIANJIN_CITYID = "10103";//天津市id
	public static final String TIANJIN_WARNINGID = "12";//天津市预警id
	public static final LatLng tianJinLatLng = new LatLng(39.445371,117.349977);//天津市中点
	public static final double tianJin_LATITUDE = tianJinLatLng.latitude;
	public static final double tianJin_LONGITUDE = tianJinLatLng.longitude;
	
	public static final String DESCRIBE = "describe";//更新频道描述信息
	
    //广播
	public static final String BROADCAST_SERVICE = "broadcast_service";//更新天气服务频道描述信息广播
	public static final String BROADCAST_UNIQUE = "broadcast_unique";//更新特色服务频道描述信息广播
	
	public static final String WAIT_WIND = "http://www.welife100.com/Wap/Fengc/index";//等风地址
	
	//activity对应的销毁map key
	public static final String MainActivity = "MainActivity";
}
