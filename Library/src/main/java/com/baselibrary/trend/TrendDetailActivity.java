package com.baselibrary.trend;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
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
import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.WeatherUtil;
import com.baselibrary.view.MyDialog;

public class TrendDetailActivity extends BaseActivity implements OnClickListener, AMapLocationListener{

	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private TextView tvTime = null;//更新时间
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
	private TextView tvHumidity = null;//相对湿度
	private TextView tvFall = null;//降水量
	private TextView tvWindForce = null;//风速
	private TextView tvWindDirection = null;//风向
	private TextView tvPressure = null;//气压
	private MyDialog mDialog = null;
	private LinearLayout llContainer1 = null;
	private LinearLayout llContainer2 = null;
	private LinearLayout llContainer3 = null;
	private LinearLayout llContainer4 = null;
	private int width = 0;
	private float density = 0;
	private LinearLayout llMain = null;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trend_detail);
		mContext = this;
		showDialog();
		initWidget();
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
		tvTime = (TextView) findViewById(R.id.tvTime);
		String time = sdf.format(new Date());
		if (time != null) {
			tvTime.setText(time + getString(R.string.update));
		}
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvFall = (TextView) findViewById(R.id.tvFall);
		tvWindForce = (TextView) findViewById(R.id.tvWindForce);
		tvWindDirection = (TextView) findViewById(R.id.tvWindDirection);
		tvPressure = (TextView) findViewById(R.id.tvPressure);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		llContainer2 = (LinearLayout) findViewById(R.id.llContainer2);
		llContainer3 = (LinearLayout) findViewById(R.id.llContainer3);
		llContainer4 = (LinearLayout) findViewById(R.id.llContainer4);
		llMain = (LinearLayout) findViewById(R.id.llMain);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;
		
		if (getIntent().hasExtra(CONST.ACTIVITY_NAME)) {
			tvTitle.setText(getIntent().getStringExtra(CONST.ACTIVITY_NAME));
		}
		
		startLocation();
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
        	getWeatherInfo(amapLocation.getLongitude(), amapLocation.getLatitude());
        }
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(double lng, double lat) {
		WeatherAPI.getGeo(mContext,String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							String cityId = geoObj.getString("id").substring(0, 9);
							if (cityId != null) {
								WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
									@Override
									public void onComplete(Weather content) {
										super.onComplete(content);
										if (content != null) {
											//实况信息
											JSONObject object = content.getWeatherFactInfo();
											try {
												int time = 0;//发布时间
												if (!object.isNull("l7")) {
													String[] temp = object.getString("l7").split(":");
													time = Integer.valueOf(temp[0]);
												}
												
												if (!object.isNull("l3")) {
													String windForce = WeatherUtil.lastValue(object.getString("l3"));
													tvWindForce.setText(getString(WeatherUtil.getWindForce(Integer.valueOf(windForce))));
												}
												if (!object.isNull("l4")) {
													String windDir = WeatherUtil.lastValue(object.getString("l4"));
													tvWindDirection.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))));
												}
												
												TrendDto dto = new TrendDto();
												if (!object.isNull("l2")) {
													String[] humiditys = object.getString("l2").split("\\|");
													for (int i = 0; i < humiditys.length; i++) {
														TrendDto h = new TrendDto();
														h.humidity = Integer.valueOf(humiditys[i]);
														dto.humidityList.add(h);
													}
													
													HumidityView humidityView = new HumidityView(mContext);
													humidityView.setData(dto.humidityList, time);
//													llContainer1.addView(humidityView, (int)(CommonUtil.dip2px(mContext, width/2)), (int)(CommonUtil.dip2px(mContext, 160)));
													llContainer1.addView(humidityView, (int)(CommonUtil.dip2px(mContext, width/density)), (int)(CommonUtil.dip2px(mContext, 160)));
													
													String humidity = WeatherUtil.lastValue(object.getString("l2"));
													tvHumidity.setText(humidity + getString(R.string.unit_percent));
												}
												if (!object.isNull("l6")) {
													String[] rainFalls = object.getString("l6").split("\\|");
													for (int i = 0; i < rainFalls.length; i++) {
														TrendDto r = new TrendDto();
														r.rainFall = Float.valueOf(rainFalls[i]);
														dto.rainFallList.add(r);
													}
													
													RainFallView rainFallView = new RainFallView(mContext);
													rainFallView.setData(dto.rainFallList, time);
//													llContainer2.addView(rainFallView, (int)(CommonUtil.dip2px(mContext, width/2)), (int)(CommonUtil.dip2px(mContext, 160)));
													llContainer2.addView(rainFallView, (int)(CommonUtil.dip2px(mContext, width/density)), (int)(CommonUtil.dip2px(mContext, 160)));
													
													String rainFall = WeatherUtil.lastValue(object.getString("l6"));
													tvFall.setText(rainFall + getString(R.string.unit_mm));
												}
												if (!object.isNull("l11")) {
													String[] windSpeeds = object.getString("l11").split("\\|");
													for (int i = 0; i < windSpeeds.length; i++) {
														TrendDto w = new TrendDto();
														w.windSpeed = Float.valueOf(windSpeeds[i]);
														dto.windSpeedList.add(w);
													}
													
													WindSpeedView windSpeedView = new WindSpeedView(mContext);
													windSpeedView.setData(dto.windSpeedList, time);
//													llContainer3.addView(windSpeedView, (int)(CommonUtil.dip2px(mContext, width/2)), (int)(CommonUtil.dip2px(mContext, 160)));
													llContainer3.addView(windSpeedView, (int)(CommonUtil.dip2px(mContext, width/density)), (int)(CommonUtil.dip2px(mContext, 160)));
												}
												if (!object.isNull("l10")) {
													String[] pressures = object.getString("l10").split("\\|");
													for (int i = 0; i < pressures.length; i++) {
														TrendDto p = new TrendDto();
														p.pressure = Integer.valueOf(pressures[i]);
														dto.pressureList.add(p);
													}
													
													PressureView pressureView = new PressureView(mContext);
													pressureView.setData(dto.pressureList, time);
//													llContainer4.addView(pressureView, (int)(CommonUtil.dip2px(mContext, width/2)), (int)(CommonUtil.dip2px(mContext, 160)));
													llContainer4.addView(pressureView, (int)(CommonUtil.dip2px(mContext, width/density)), (int)(CommonUtil.dip2px(mContext, 160)));
													
													String pressure = WeatherUtil.lastValue(object.getString("l10"));
													tvPressure.setText(pressure + getString(R.string.unit_hPa));
												}
												
											} catch (JSONException e) {
												e.printStackTrace();
											}
											
											cancelDialog();
											llMain.setVisibility(View.VISIBLE);
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
				Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}
}
