package com.baselibrary.common;

import java.util.ArrayList;
import java.util.List;

public class CONST {

	public static final float zoom = 7.5f;//地图缩放的zoom值
	public static final String ICON_WARNING_ = "icon_warning_";
	public static final String DEFULT = "defult";//默认
	public static final String TRIPLE = "@3x";//三倍大小的图标
	public static final String imageSuffix = ".png";//图标后缀名
	
	//showType类型，区分本地类或者图文
	public static final String LOCAL = "local";
	public static final String NEWS = "news";
	public static final String NEWSPLUS = "news+";
	public static final String PRODUCT = "product";
	public static final String URL = "url";
	public static final String PDF = "pdf";
	
	//intent传值的标示
	public static final String PROVINCE_NAME = "province_name";//省份名称
	public static final String INTENT_APPID = "intent_appid";
	public static final String WEB_URL = "web_Url";//网页地址的标示
	public static final String ACTIVITY_NAME = "activity_name";//界面名称
	public static final String INTENT_IMGURL = "intent_imgurl";//分享时分享的图片
	public static final String LATITUDE = "latitude";//维度
	public static final String LONGITUDE = "longitude";//经度
	public static final String CITY_ID = "city_id";//城市id
	public static final String WARNING_ID = "warning_id";//预警id
	public static final String CITY_NAME = "city_name";//城市名称
	public static final String RADAR_NAME_ARRAY = "radar_name_array";//雷达站点名称
	
	//下拉刷新progresBar四种颜色
	public static final int color1 = android.R.color.holo_blue_bright;
	public static final int color2 = android.R.color.holo_green_light;
	public static final int color3 = android.R.color.holo_orange_light;
	public static final int color4 = android.R.color.holo_red_light;
	
	//本地保存用户信息参数
	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		public static final String uId = "uId";
		public static final String userName = "uName";
		public static final String passWord = "pwd";
	}
	
	public static String UID = null;//用户id
	public static String USERNAME = null;//用户名
	public static String PASSWORD = null;//用户密码
	
	public static final List<ColumnData> dataList = new ArrayList<ColumnData>();//存放接口数据对象
	
	//预警颜色对应规则
	public static String[] blue = {"01", "_blue"};
	public static String[] yellow = {"02", "_yellow"};
	public static String[] orange = {"03", "_orange"};
	public static String[] red = {"04", "_red"};
	
	//贵州分享
	public static String GUIZHOU_WXAPPID = "wxcffa2038dc29be7d";//微信分享appid
	public static String GUIZHOU_WXSIGN = "5c75c233e138513ad2751e1b8190eb64";//微信分享签名
	
	//贵州接口
	public static String GUIZHOU_BASE = "http://222.85.131.129:1083/decision/Home";
	public static String GUIZHOU_LOGIN = GUIZHOU_BASE+"/Work/login";//登录
	public static String GUIZHOU_FEEDBACK = GUIZHOU_BASE+"/Work/request";//意见反馈
	
	//天津名称
//	public static String TIANJIN_BASE = "http://182.254.247.81/decision/Home";
	public static String TIANJIN_BASE = "http://decision-admin.tianqi.cn/Home";
	public static String TIANJIN_LOGIN = TIANJIN_BASE+"/Work/login";//登录
	public static String TIANJIN_FEEDBACK = TIANJIN_BASE+"/Work/request";//意见反馈
		
	//西藏名称
//	public static String XIZANG_BASE = "http://182.254.247.81/decision/Home";
	public static String XIZANG_BASE = "http://decision-admin.tianqi.cn/Home";
	public static String XIZANG_LOGIN = XIZANG_BASE+"/Work/login";//登录
	public static String XIZANG_FEEDBACK = XIZANG_BASE+"/Work/request";//意见反馈
	
	public static String STYLE = "1";//1为默认，2为节日，3为丧葬
	public static String STYLE_ONE = "1";//1为默认，2为节日，3为丧葬
	public static String STYLE_TWO = "2";//1为默认，2为节日，3为丧葬
	public static String STYLE_THREE = "3";//1为默认，2为节日，3为丧葬
}
