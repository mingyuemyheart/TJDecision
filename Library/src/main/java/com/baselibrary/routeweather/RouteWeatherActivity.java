package com.baselibrary.routeweather;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.FromAndTo;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.WeatherUtil;
import com.baselibrary.view.MyDialog;

public class RouteWeatherActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnRouteSearchListener, OnMarkerClickListener, InfoWindowAdapter, OnMapClickListener {
	
	private Context mContext = null;
	private MapView mMapView;
	private AMap mAMap;
	private RouteSearch routeSearch = null;//路径搜索
	private LatLonPoint startPoint = null;//起点经纬度点
	private LatLonPoint endPoint = null;//终点经纬度点
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private LinearLayout llExchange = null;//互换按钮
	private TextView tvStart = null;
	private TextView tvEnd = null;
	private boolean isStart = true;
	private LinearLayout llSearch = null;
	private TextView line = null;
	private MyDialog mDialog = null;
	private LinearLayout llStart = null;
	private LinearLayout llEnd = null;
	private float MAX_DISTANCE = 100*1000;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	
	//详情
	private List<RouteWeatherDto> detailList = new ArrayList<RouteWeatherDto>();
	private TextView tvPosition = null;//定位地点
	private TextView tvTime = null;//更新时间
	private TextView tvVisible = null;//能见度
	private TextView tvTemp = null;//实况温度
	private TextView tvTempUnit = null;
	private ImageView ivPhenomenon = null;//天气显现对应的图标
	private TextView tvRainFall = null;//降水量
	private TextView tvPressure = null;//气压
	private TextView tvHumidity = null;//相对湿度
	private TextView tvWind = null;//风速
	private RelativeLayout reDetail = null;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.route_weather);
		mContext = this;
		initWidget();
		initAmap(arg0);
	}
	
	private void showDialog() {
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
		llExchange = (LinearLayout) findViewById(R.id.llExchange);
		llExchange.setOnClickListener(this);
		tvStart = (TextView) findViewById(R.id.tvStart);
		tvEnd = (TextView) findViewById(R.id.tvEnd);
		llSearch = (LinearLayout) findViewById(R.id.llSearch);
		line = (TextView) findViewById(R.id.line);
		llStart = (LinearLayout) findViewById(R.id.llStart);
		llStart.setOnClickListener(this);
		llEnd = (LinearLayout) findViewById(R.id.llEnd);
		llEnd.setOnClickListener(this);
		
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvVisible = (TextView) findViewById(R.id.tvVisible);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		tvTempUnit = (TextView) findViewById(R.id.tvTempUnit);
		ivPhenomenon = (ImageView) findViewById(R.id.ivPhenomenon);
		tvRainFall = (TextView) findViewById(R.id.tvRainFall);
		tvPressure = (TextView) findViewById(R.id.tvPressure);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvWind = (TextView) findViewById(R.id.tvWind);
		reDetail = (RelativeLayout) findViewById(R.id.reDetail);
		
		if (getIntent().hasExtra(CONST.ACTIVITY_NAME)) {
			String name = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
			if (name != null) {
				tvTitle.setText(name);
			}
		}
		
		startLocation();
		
		routeSearch = new RouteSearch(mContext);
		routeSearch.setRouteSearchListener(this);
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
        	startPoint = new LatLonPoint(amapLocation.getLatitude(), amapLocation.getLongitude());
        	tvStart.setText(getString(R.string.route_current_position));
        	llSearch.setVisibility(View.VISIBLE);
    		line.setVisibility(View.VISIBLE);
        }
	}
	
	@Override
	public void onBusRouteSearched(BusRouteResult arg0, int arg1) {
	}
	@Override
	public void onDriveRouteSearched(DriveRouteResult arg0, int arg1) {
		if (arg0 == null || arg0.getStartPos() == null || arg0.getTargetPos() == null) {
			cancelDialog();
			Toast.makeText(mContext, getString(R.string.no_result), Toast.LENGTH_SHORT).show();
			return;
		}
		
		final LatLonPoint startPoint = arg0.getStartPos();
		final LatLonPoint endPoint = arg0.getTargetPos();
		final LatLng startLatLng = new LatLng(startPoint.getLatitude(), startPoint.getLongitude());
		final LatLng endLatLng = new LatLng(endPoint.getLatitude(), endPoint.getLongitude());
		
		mAMap.clear();// 清理地图上的所有覆盖物
		detailList.clear();
		final DrivePath drivePath = arg0.getPaths().get(0);
		DrivingOverlay overlay = new DrivingOverlay(mContext, mAMap, drivePath, startPoint, endPoint);
		overlay.removeFromMap();
		overlay.addToMap();
		overlay.zoomToSpan();
		overlay.setNodeIconVisibility(false);//隐藏驾车路线经过的点
		overlay.setThroughPointIconVisibility(false);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < 2; i++) {
			if (i == 0) {
				View view = inflater.inflate(R.layout.marker_view, null);
				ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
				ivMarker.setBackgroundResource(R.drawable.iv_start_marker);
				LayoutParams params = ivMarker.getLayoutParams();
				params.width = (int) CommonUtil.dip2px(mContext, 20);
				params.height = (int) CommonUtil.dip2px(mContext, 20);
				ivMarker.setLayoutParams(params);
				MarkerOptions options = new MarkerOptions();
				options.anchor(0.5f, 0.5f);
				options.position(startLatLng);
				options.icon(BitmapDescriptorFactory.fromView(view));
				mAMap.addMarker(options);
			}else if (i == 1) {
				View view = inflater.inflate(R.layout.marker_view, null);
				ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
				ivMarker.setBackgroundResource(R.drawable.iv_end_marker);
				LayoutParams params = ivMarker.getLayoutParams();
				params.width = (int) CommonUtil.dip2px(mContext, 20);
				params.height = (int) CommonUtil.dip2px(mContext, 20);
				ivMarker.setLayoutParams(params);
				MarkerOptions options = new MarkerOptions();
				options.anchor(0.5f, 0.5f);
				options.position(endLatLng);
				options.icon(BitmapDescriptorFactory.fromView(view));
				mAMap.addMarker(options);
			}
		}
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				List<DriveStep> driveSteps = drivePath.getSteps();
//				getWeatherInfo(startPoint.getLongitude(), startPoint.getLatitude());
//				getWeatherInfo(endPoint.getLongitude(), endPoint.getLatitude());
				float distance = 0;
				for (int i = 0; i < driveSteps.size(); i++) {
					DriveStep step = driveSteps.get(i);
					
					if (i == 0 || i == driveSteps.size()-1) {
						LatLonPoint point = step.getPolyline().get(0);
						getWeatherInfo(point.getLongitude(), point.getLatitude());
					}
					
					distance += step.getDistance();
					if (distance >= MAX_DISTANCE) {
						distance = 0;
						LatLonPoint point = step.getPolyline().get(0);
						getWeatherInfo(point.getLongitude(), point.getLatitude());
					}
				}
			}
		});
		thread.start();
		
		mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 7.0f));
		
		cancelDialog();
		llSearch.setVisibility(View.GONE);
		line.setVisibility(View.GONE);
		mMapView.setVisibility(View.VISIBLE);
	}
	@Override
	public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {
	}
	
	/**
	 * 添加marker到地图上
	 */
	private void addMarkerToMap(LatLng latLng, String weatherCode, String cityId) {
		MarkerOptions options = new MarkerOptions();
		options.title(cityId);
		options.anchor(0.5f, 1.0f);
		options.position(latLng);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_view, null);
		ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
		Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_route);
		drawable.setLevel(Integer.valueOf(weatherCode));
		ivMarker.setBackground(drawable);
		options.icon(BitmapDescriptorFactory.fromView(view));
		
		mAMap.addMarker(options);
	}
	
	/**
	 * 初始化地图
	 */
	private void initAmap(Bundle bundle) {
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (mAMap == null) {
			mAMap = mMapView.getMap();
		}
		mAMap.getUiSettings().setZoomControlsEnabled(false);
		mAMap.setOnMapClickListener(this);
		mAMap.setOnMarkerClickListener(this);
		mAMap.setInfoWindowAdapter(this);
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
	public boolean onMarkerClick(Marker arg0) {
		for (int i = 0; i < detailList.size(); i++) {
			RouteWeatherDto dto = detailList.get(i);
			if (TextUtils.equals(arg0.getTitle(), dto.cityId)) {
				tvPosition.setText(dto.position);
				tvTime.setText(dto.time + getString(R.string.update));
				tvTemp.setText(dto.temp);
				tvTempUnit.setText(getString(R.string.unit_degree));
				tvHumidity.setText(dto.humidity + getString(R.string.unit_percent));
				Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
				drawable.setLevel(Integer.valueOf(dto.code));
				ivPhenomenon.setBackground(drawable);
				tvRainFall.setText(dto.rain + getString(R.string.unit_mm));
				float value = Float.valueOf(dto.visible)/1000.0f;
				tvVisible.setText(getString(R.string.visible)+String.valueOf(value)+getString(R.string.unit_km));
				tvPressure.setText(dto.pressure+getString(R.string.unit_hPa));
				tvWind.setText(dto.windSpeed+getString(R.string.unit_speed));
				break;
			}
		}
		
		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 1f, 
				Animation.RELATIVE_TO_SELF, 0);
		animation.setDuration(500);
		if (reDetail.getVisibility() == View.GONE) {
			reDetail.setAnimation(animation);
			reDetail.setVisibility(View.VISIBLE);
		}
		
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
	 * 获取天气数据
	 */
	private void getWeatherInfo(final double lng, final double lat) {
		WeatherAPI.getGeo(mContext,String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							final String cityId = geoObj.getString("id").substring(0, 9);
							final String city = geoObj.getString("city");
							if (cityId != null) {
								WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
									@Override
									public void onComplete(Weather content) {
										super.onComplete(content);
										if (content != null) {
											//实况信息
											JSONObject object = content.getWeatherFactInfo();
											try {
												RouteWeatherDto dto = new RouteWeatherDto();
												dto.cityId = cityId;
												dto.lat = lat;
												dto.lng = lng;
												dto.position = city;
												if (!object.isNull("l1")) {
													String factTemp = WeatherUtil.lastValue(object.getString("l1"));
													dto.temp = factTemp;
												}
												if (!object.isNull("l2")) {
													String humidity = WeatherUtil.lastValue(object.getString("l2"));
													dto.humidity = humidity;
												}
												if (!object.isNull("l3")) {
													String windForce = WeatherUtil.lastValue(object.getString("l3"));
												}
												if (!object.isNull("l4")) {
													String windDir = WeatherUtil.lastValue(object.getString("l4"));
												}
												if (!object.isNull("l5")) {
													String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
													dto.code = weatherCode;
												}
												if (!object.isNull("l6")) {
													String rainFall = WeatherUtil.lastValue(object.getString("l6"));
													dto.rain = rainFall;
												}
												if (!object.isNull("l7")) {
													String time = object.getString("l7");
													dto.time = time;
												}
												if (!object.isNull("l9")) {
													String visible = WeatherUtil.lastValue(object.getString("l9"));
													dto.visible = visible;
												}
												if (!object.isNull("l10")) {
													String pressure = WeatherUtil.lastValue(object.getString("l10"));
													dto.pressure = pressure;
												}
												if (!object.isNull("l11")) {
													String windSpeed = WeatherUtil.lastValue(object.getString("l11"));
													dto.windSpeed = windSpeed;
												}
												detailList.add(dto);
												addMarkerToMap(new LatLng(lat, lng), dto.code, cityId);
											} catch (JSONException e) {
												e.printStackTrace();
											}
										}
									}
									
									@Override
									public void onError(Throwable error, String content) {
										super.onError(error, content);
										cancelDialog();
										Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
									}
								});
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
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
		}else if (v.getId() == R.id.llExchange) {
			String start = tvStart.getText().toString().trim();
			String end = tvEnd.getText().toString().trim();
			LatLonPoint startTemp = startPoint;
			LatLonPoint endTemp = endPoint;
			if (isStart) {
				tvStart.setText("");
				tvEnd.setText(start);
				startPoint = null;
				endPoint = startTemp;
				isStart = false;
			}else {
				tvStart.setText(end);
				tvEnd.setText("");
				startPoint = endTemp;
				endPoint = null;
				isStart = true;
			}
		}else if (v.getId() == R.id.llStart) {
			Intent intentStart = new Intent(mContext, RouteSearchActivity.class);
			intentStart.putExtra("startOrEnd", "start");
			startActivityForResult(intentStart, 0);
		}else if (v.getId() == R.id.llEnd) {
			Intent intentEnd = new Intent(mContext, RouteSearchActivity.class);
			intentEnd.putExtra("startOrEnd", "end");
			startActivityForResult(intentEnd, 1);
		}
	}
	
	private LatLonPoint getResultData(Intent data, TextView textView) {
		LatLonPoint latLngPoint = null;
		if (data != null) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				textView.setText(bundle.getString("cityName"));;
				latLngPoint = new LatLonPoint(bundle.getDouble("lat"), bundle.getDouble("lng"));
			}
		}
		return latLngPoint;
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == 0) {
				startPoint = getResultData(data, tvStart);
			}else if (requestCode == 1) {
				endPoint = getResultData(data, tvEnd);
			}
			
			if (!TextUtils.isEmpty(tvStart.getText().toString()) && !TextUtils.isEmpty(tvEnd.getText().toString())) {
				showDialog();
				FromAndTo ft = new FromAndTo(startPoint, endPoint);
	    		DriveRouteQuery drive = new DriveRouteQuery(ft, RouteSearch.DrivingMultiStrategy, null, null, null);
	    		routeSearch.calculateDriveRouteAsyn(drive);
			}
		}
	}

}
