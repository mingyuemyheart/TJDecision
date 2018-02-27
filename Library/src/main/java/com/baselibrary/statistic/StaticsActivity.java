package com.baselibrary.statistic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.district.DistrictSearchQuery;
import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.manager.RainManager;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;

public class StaticsActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener, InfoWindowAdapter, OnMapClickListener, OnCameraChangeListener, OnDistrictSearchListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private List<WeatherStaticsDto> videoList = new ArrayList<WeatherStaticsDto>();
	private CircularProgressBar mCircularProgressBar1 = null;
	private CircularProgressBar mCircularProgressBar2 = null;
	private CircularProgressBar mCircularProgressBar3 = null;
	private CircularProgressBar mCircularProgressBar4 = null;
	private CircularProgressBar mCircularProgressBar5 = null;
	private TextView tvName = null;
	private TextView tvBar1 = null;
	private TextView tvBar2 = null;
	private TextView tvBar3 = null;
	private TextView tvBar4 = null;
	private TextView tvBar5 = null;
	private TextView tvDetail = null;
	private RelativeLayout reDetail = null;
	public final static String SANX_DATA_99 = "sanx_data_99";//加密秘钥名称
	public final static String APPID = "f63d329270a44900";//机密需要用到的AppId
	private MyDialog mDialog = null;
	private String cityId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statics);
		mContext = this;
		initDialog();
		initWidget();
		initMap(savedInstanceState);
		getData();
	}
	
	/**
	 * 初始化dialog
	 */
	private void initDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog(mContext);
		}
		mDialog.show();
	}
	private void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvName = (TextView) findViewById(R.id.tvName);
		tvBar1 = (TextView) findViewById(R.id.tvBar1);
		tvBar2 = (TextView) findViewById(R.id.tvBar2);
		tvBar3 = (TextView) findViewById(R.id.tvBar3);
		tvBar4 = (TextView) findViewById(R.id.tvBar4);
		tvBar5 = (TextView) findViewById(R.id.tvBar5);
		tvDetail = (TextView) findViewById(R.id.tvDetail);
		mCircularProgressBar1 = (CircularProgressBar) findViewById(R.id.bar1);
		mCircularProgressBar2 = (CircularProgressBar) findViewById(R.id.bar2);
		mCircularProgressBar3 = (CircularProgressBar) findViewById(R.id.bar3);
		mCircularProgressBar4 = (CircularProgressBar) findViewById(R.id.bar4);
		mCircularProgressBar5 = (CircularProgressBar) findViewById(R.id.bar5);
		reDetail = (RelativeLayout) findViewById(R.id.reDetail);
		
		String title = getIntent().getStringExtra(com.baselibrary.common.CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		cityId = getIntent().getStringExtra(CONST.CITY_ID);
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap(Bundle bundle) {
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		LatLng centerLatLng = new LatLng(getIntent().getDoubleExtra(CONST.LATITUDE, 0), getIntent().getDoubleExtra(CONST.LONGITUDE, 0));
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, CONST.zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setOnCameraChangeListener(this);
		aMap.setInfoWindowAdapter(this);
		
		String name = getIntent().getStringExtra(CONST.PROVINCE_NAME);
		if (name != null) {
			drawDistrict(name);
		}
	}
	
	/**
	 * 绘制区域
	 */
	private void drawDistrict(String keywords) {
		DistrictSearch search = new DistrictSearch(mContext);
		DistrictSearchQuery query = new DistrictSearchQuery();
		query.setKeywords(keywords);//传入关键字
//		query.setKeywordsLevel(DistrictSearchQuery.KEYWORDS_CITY);
		query.setShowBoundary(true);//是否返回边界值
		search.setQuery(query);
		search.setOnDistrictSearchListener(this);//绑定监听器
		search.searchDistrictAnsy();//开始搜索
	}
	
	@Override
	public void onDistrictSearched(DistrictResult districtResult) {
		if (districtResult == null|| districtResult.getDistrict()==null) {
			return;
		}
		
		final DistrictItem item = districtResult.getDistrict().get(0);
		if (item == null) {
			return;
		}
//		LatLonPoint centerLatLng=item.getCenter();
//		if(centerLatLng!=null){
//			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLatLng.getLatitude(), centerLatLng.getLongitude()), CONST.zoom));
//		}
		
		new Thread() {
			public void run() {
				String[] polyStr = item.districtBoundary();
				if (polyStr == null || polyStr.length == 0) {
					return;
				}
				for (String str : polyStr) {
					String[] lat = str.split(";");
					PolylineOptions polylineOption = new PolylineOptions();
					boolean isFirst=true;
					LatLng firstLatLng=null;
					for (String latstr : lat) {
						String[] lats = latstr.split(",");
						if(isFirst){
							isFirst=false;
							firstLatLng=new LatLng(Double.parseDouble(lats[1]), Double.parseDouble(lats[0]));
						}
						polylineOption.add(new LatLng(Double.parseDouble(lats[1]), Double.parseDouble(lats[0])));
					}
					if(firstLatLng!=null){
						polylineOption.add(firstLatLng);
					}
					
					polylineOption.width(10).color(Color.BLUE);	 
					aMap.addPolyline(polylineOption);
				}
			}
 		}.start();
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
//		DisplayMetrics dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
//		Point leftPoint = new Point(0, dm.heightPixels);
//		Point rightPoint = new Point(dm.widthPixels, 0);
//		LatLng leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
//		LatLng rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
//		
//		if (leftlatlng.latitude > CONST.leftLatLng.latitude || rightLatlng.longitude < CONST.rightLatLng.longitude) {
//			aMap.getUiSettings().setZoomGesturesEnabled(true);//缩放
//			aMap.getUiSettings().setScrollGesturesEnabled(true);//拖动
//		}else {
//			aMap.getUiSettings().setZoomGesturesEnabled(false);
//			aMap.getUiSettings().setScrollGesturesEnabled(false);
//		}
//		aMap.getUiSettings().setRotateGesturesEnabled(false);//旋转
	}
	
	/**
	 * 加密请求字符串
	 * @param url 基本串
	 * @param lng 经度
	 * @param lat 维度
	 * @return
	 */
	private String getSecretUrl() {
		String URL = "http://scapi.weather.com.cn/weather/stationinfo";//天气统计地址
		String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmm");//系统时间
		StringBuffer buffer = new StringBuffer();
		buffer.append(URL);
		buffer.append("?");
		buffer.append("date=").append(sysdate);
		buffer.append("&");
		buffer.append("appid=").append(APPID);
		
		String key = RainManager.getKey(SANX_DATA_99, buffer.toString());
		buffer.delete(buffer.lastIndexOf("&"), buffer.length());
		
		buffer.append("&");
		buffer.append("appid=").append(APPID.substring(0, 6));
		buffer.append("&");
		buffer.append("key=").append(key.substring(0, key.length() - 3));
		String result = buffer.toString();
		return result;
	}
	
	/**
	 * 获取天气网眼数据
	 */
	private void getData() {
		//异步请求数据
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(getSecretUrl());
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				videoList.clear();
				parseGuiZhouStationInfo(result, "level1");
				parseGuiZhouStationInfo(result, "level2");
				parseGuiZhouStationInfo(result, "level3");
				addMarker(videoList);
				cancelDialog();
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 解析贵州省内的站点信息
	 */
	private void parseGuiZhouStationInfo(String result, String level) {
		try {
			JSONObject obj = new JSONObject(result.toString());
			if (!obj.isNull(level)) {
				JSONArray array = new JSONArray(obj.getString(level));
				for (int i = 0; i < array.length(); i++) {
					WeatherStaticsDto dto = new WeatherStaticsDto();
					JSONObject itemObj = array.getJSONObject(i);
					if (!itemObj.isNull("name")) {
						dto.name = itemObj.getString("name");
					}
					if (!itemObj.isNull("stationid")) {
						dto.stationId = itemObj.getString("stationid");
					}
					if (!itemObj.isNull("level")) {
						dto.level = itemObj.getString("level");
					}
					if (!itemObj.isNull("areaid")) {
						dto.areaId = itemObj.getString("areaid");
					}
					if (!itemObj.isNull("lat")) {
						dto.latitude = itemObj.getString("lat");
					}
					if (!itemObj.isNull("lon")) {
						dto.longitude = itemObj.getString("lon");
					}
					
					dto.tag = "tag"+i;
					
					//在全国所有站点里筛选出贵州省内的站点
					if (dto.areaId != null && dto.areaId.substring(0, 5).equals(cityId)) {
						videoList.add(dto);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 加密请求字符串
	 * @param url 基本串
	 * @param lng 经度
	 * @param lat 维度
	 * @return
	 */
	private String getSecretUrl2(String stationid, String areaid) {
		String URL = "http://scapi.weather.com.cn/weather/historycount";
		String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmm");//系统时间
		StringBuffer buffer = new StringBuffer();
		buffer.append(URL);
		buffer.append("?");
		buffer.append("stationid=").append(stationid);
		buffer.append("&");
		buffer.append("areaid=").append(areaid);
		buffer.append("&");
		buffer.append("date=").append(sysdate);
		buffer.append("&");
		buffer.append("appid=").append(APPID);
		
		String key = RainManager.getKey(SANX_DATA_99, buffer.toString());
		buffer.delete(buffer.lastIndexOf("&"), buffer.length());
		
		buffer.append("&");
		buffer.append("appid=").append(APPID.substring(0, 6));
		buffer.append("&");
		buffer.append("key=").append(key.substring(0, key.length() - 3));
		String result = buffer.toString();
		return result;
	}
	
	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap(String name) {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.statics_item, null);
		if (view == null) {
			return null;
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		tvName.setText(name);
		return view;
	}
	
	/**
	 * 添加marker
	 */
	private void addMarker(List<WeatherStaticsDto> list) {
		if (list.isEmpty()) {
			return;
		}
		
		int length = list.size();
		for (int i = 0; i < length; i++) {
			MarkerOptions options = new MarkerOptions();
			options.title(list.get(i).tag);
			options.anchor(0.5f, 0.5f);
			options.position(new LatLng(Double.valueOf(list.get(i).latitude), Double.valueOf(list.get(i).longitude)));
			options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(list.get(i).name)));
			aMap.addMarker(options);
		}
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(500);
		if (reDetail.getVisibility() == View.VISIBLE) {
			reDetail.setAnimation(animation);
			reDetail.setVisibility(View.GONE);
		}
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		String name = null;
		String areaId = null;
		String stationId = null;
		int length = videoList.size();
		for (int i = 0; i < length; i++) {
			if (TextUtils.equals(marker.getTitle(), videoList.get(i).tag)) {
				areaId = videoList.get(i).areaId;
				stationId = videoList.get(i).stationId;
				name = videoList.get(i).name;
				break;
			}
		}
		
		tvName.setText(name + " " + stationId);
		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 1f, 
				Animation.RELATIVE_TO_SELF, 0);
		animation.setDuration(500);
		if (reDetail.getVisibility() == View.GONE) {
			reDetail.setAnimation(animation);
			reDetail.setVisibility(View.VISIBLE);
			tvDetail.setText("");
		}
		
		//异步请求数据
		HttpAsyncTask2 task = new HttpAsyncTask2();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(getSecretUrl2(stationId, areaId));
		return false;
	}
	
	@Override
	public View getInfoContents(Marker arg0) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.empty_marker_view, null);
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask2 extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask2() {
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@SuppressLint("SimpleDateFormat")
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					JSONObject obj = new JSONObject(result.toString());
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
					String startTime = null;//开始时间
					String endTime = null;//结束时间
					String no_rain_lx = null;//连续没雨天数
					String mai_lx = null;//连续霾天数
					String highTemp = null;//高温
					String lowTemp = null;//低温
					String highWind = null;//最大风速
					String highRain = null;//最大降水量
					if (!obj.isNull("starttime")) {
						try {
							startTime = sdf2.format(sdf.parse(obj.getString("starttime")));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					if (!obj.isNull("endtime")) {
						try {
							endTime = sdf2.format(sdf.parse(obj.getString("endtime")));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					if (!obj.isNull("no_rain_lx")) {
						no_rain_lx = obj.getString("no_rain_lx");
					}
					if (!obj.isNull("mai_lx")) {
						mai_lx = obj.getString("mai_lx");
					}
					
					if (!obj.isNull("count")) {
						JSONArray array = new JSONArray(obj.getString("count"));
						JSONObject itemObj0 = array.getJSONObject(0);//温度
						JSONObject itemObj1 = array.getJSONObject(1);//降水
						JSONObject itemObj5 = array.getJSONObject(5);//风速
						
						if (!itemObj0.isNull("max") && !itemObj0.isNull("min")) {
							highTemp = itemObj0.getString("max");
							lowTemp = itemObj0.getString("min");
						}
						if (!itemObj1.isNull("max")) {
							highRain = itemObj1.getString("max");
						}
						if (!itemObj5.isNull("max")) {
							highWind = itemObj5.getString("max");
						}
					}
					
					if (startTime != null && endTime != null && highTemp != null && lowTemp != null 
							&& highWind != null && highRain != null && no_rain_lx != null && mai_lx != null) {
						StringBuffer buffer = new StringBuffer();
						buffer.append(getString(R.string.from)).append(startTime);
						buffer.append(getString(R.string.to)).append(endTime);
						buffer.append("：\n");
						buffer.append(getString(R.string.highest_temp)).append(highTemp).append("℃").append("，");
						buffer.append(getString(R.string.lowest_temp)).append(lowTemp).append("℃").append("，");
						buffer.append(getString(R.string.max_speed)).append(highWind).append("m/s").append("，");
						buffer.append(getString(R.string.max_fall)).append(highRain).append("mm").append("，");
						if (no_rain_lx.equals("-1")) {
							buffer.append(getString(R.string.lx_no_fall)).append(getString(R.string.no_statics)).append("，");
						}else {
							buffer.append(getString(R.string.lx_no_fall)).append(no_rain_lx).append(getString(R.string.day)).append("，");
						}
						if (mai_lx.equals("-1")) {
							buffer.append(getString(R.string.lx_no_mai)).append(getString(R.string.no_statics)).append("。");
						}else {
							buffer.append(getString(R.string.lx_no_mai)).append(mai_lx).append(getString(R.string.day)).append("。");
						}
						
						SpannableStringBuilder builder = new SpannableStringBuilder(buffer.toString());
						ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
						ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
						ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
						ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
						ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
						ForegroundColorSpan builderSpan6 = new ForegroundColorSpan(getResources().getColor(R.color.builder));

						builder.setSpan(builderSpan1, 29, 29+highTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan2, 29+highTemp.length()+7, 29+highTemp.length()+7+lowTemp.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						builder.setSpan(builderSpan3, 29+highTemp.length()+7+lowTemp.length()+7, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan4, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						if (no_rain_lx.equals("-1")) {
							builder.setSpan(builderSpan5, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10+no_rain_lx.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}else {
							builder.setSpan(builderSpan5, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10+no_rain_lx.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						if (mai_lx.equals("-1")) {
							builder.setSpan(builderSpan6, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10+no_rain_lx.length()+7, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10+no_rain_lx.length()+7+mai_lx.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}else {
							builder.setSpan(builderSpan6, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10+no_rain_lx.length()+7, 29+highTemp.length()+7+lowTemp.length()+7+highWind.length()+10+highRain.length()+10+no_rain_lx.length()+7+mai_lx.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						tvDetail.setText(builder);
						
						try {
							long start = sdf2.parse(startTime).getTime();
							long end = sdf2.parse(endTime).getTime();
							long value = end - start;
							float day = (float) (value / (1000*60*60*24));
							if (!obj.isNull("tqxxcount")) {
								JSONArray array = new JSONArray(obj.getString("tqxxcount"));
								JSONObject itemObj0 = array.getJSONObject(0);
								JSONObject itemObj1 = array.getJSONObject(1);
								JSONObject itemObj2 = array.getJSONObject(2);
								JSONObject itemObj3 = array.getJSONObject(3);
								JSONObject itemObj4 = array.getJSONObject(4);
								
								if (!itemObj0.isNull("name") && !itemObj0.isNull("value")) {
									if (itemObj0.getInt("value") == -1) {
										tvBar1.setText(itemObj0.getString("name") + "\n" + "--");
									}else {
										tvBar1.setText(itemObj0.getString("name") + "\n" + itemObj0.getInt("value") + getString(R.string.day));
									}
								}
								if (!itemObj1.isNull("name") && !itemObj1.isNull("value")) {
									if (itemObj1.getInt("value") == -1) {
										tvBar2.setText(itemObj1.getString("name") + "\n" + "--");
									}else {
										tvBar2.setText(itemObj1.getString("name") + "\n" + itemObj1.getInt("value") + getString(R.string.day));
									}
								}
								if (!itemObj2.isNull("name") && !itemObj2.isNull("value")) {
									if (itemObj2.getInt("value") == -1) {
										tvBar3.setText(itemObj2.getString("name") + "\n" + "--");
									}else {
										tvBar3.setText(itemObj2.getString("name") + "\n" + itemObj2.getInt("value") + getString(R.string.day));
									}
								}
								if (!itemObj3.isNull("name") && !itemObj3.isNull("value")) {
									if (itemObj3.getInt("value") == -1) {
										tvBar4.setText(itemObj3.getString("name") + "\n" + "--");
									}else {
										tvBar4.setText(itemObj3.getString("name") + "\n" + itemObj3.getInt("value") + getString(R.string.day));
									}
								}
								if (!itemObj4.isNull("name") && !itemObj4.isNull("value")) {
									if (itemObj4.getInt("value") == -1) {
										tvBar5.setText(itemObj4.getString("name") + "\n" + "--");
									}else {
										tvBar5.setText(itemObj4.getString("name") + "\n" + itemObj4.getInt("value") + getString(R.string.day));
									}
								}
								
								animate(mCircularProgressBar1, null, -itemObj0.getInt("value")/day, 1000);
								mCircularProgressBar1.setProgress(-itemObj0.getInt("value")/day);
								animate(mCircularProgressBar2, null, -itemObj1.getInt("value")/day, 1000);
								mCircularProgressBar2.setProgress(-itemObj1.getInt("value")/day);
								animate(mCircularProgressBar3, null, -itemObj2.getInt("value")/day, 1000);
								mCircularProgressBar3.setProgress(-itemObj2.getInt("value")/day);
								animate(mCircularProgressBar4, null, -itemObj3.getInt("value")/day, 1000);
								mCircularProgressBar4.setProgress(-itemObj3.getInt("value")/day);
								animate(mCircularProgressBar5, null, -itemObj4.getInt("value")/day, 1000);
								mCircularProgressBar5.setProgress(-itemObj4.getInt("value")/day);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 进度条动画
	 * @param progressBar
	 * @param listener
	 * @param progress
	 * @param duration
	 */
	private void animate(final CircularProgressBar progressBar, final AnimatorListener listener,final float progress, final int duration) {
		ObjectAnimator mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
		mProgressBarAnimator.setDuration(duration);
		mProgressBarAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(final Animator animation) {
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				progressBar.setProgress(progress);
			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
			}

			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
		if (listener != null) {
			mProgressBarAnimator.addListener(listener);
		}
		mProgressBarAnimator.reverse();
		mProgressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				progressBar.setProgress((Float) animation.getAnimatedValue());
			}
		});
//		progressBar.setMarkerProgress(0f);
		mProgressBarAnimator.start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Animation animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0f, 
					Animation.RELATIVE_TO_SELF, 1f);
			animation.setDuration(500);
			if (reDetail.getVisibility() == View.VISIBLE) {
				reDetail.setAnimation(animation);
				reDetail.setVisibility(View.GONE);
				return false;
			} else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			Animation animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0f, 
					Animation.RELATIVE_TO_SELF, 1f);
			animation.setDuration(500);
			if (reDetail.getVisibility() == View.VISIBLE) {
				reDetail.setAnimation(animation);
				reDetail.setVisibility(View.GONE);
			} else {
				finish();
			}
		}
	}

}
