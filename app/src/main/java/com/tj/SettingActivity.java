package com.tj;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.feedback.FeedbackActivity;
import com.baselibrary.manager.DataCleanManager;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.utils.PgyUtil;
import com.baselibrary.utils.WeatherUtil;
import com.tj.common.MyApplication;
import cxwl.shawn.tj.decision.R;

public class SettingActivity extends BaseActivity implements OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private TextView tvPosition = null;//定位地点
	private TextView tvTime = null;//更新时间
	private TextView tvRain = null;
	private TextView tvTemp = null;//实况温度
	private TextView tvTempUnit = null;
	private ImageView ivPhenomenon = null;//天气显现对应的图标
	private TextView tvPhenomenon = null;//天气现象
	private TextView tvHumidity = null;//相对湿度
	private TextView tvWind = null;//风速
	private TextView tvQuality = null;//空气质量
	private TextView tvVersion = null;//版本号
	private LinearLayout llFeedBack = null;//意见反馈
	private LinearLayout llVersion = null;//版本检测
	private LinearLayout llClearCache = null;//清除缓存
	private TextView tvCache = null;
	private TextView tvLogout = null;//退出登录
	private ProgressBar progressBar = null;
	private RelativeLayout reLocation = null;
	private TextView tvUserName = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvPosition.setFocusable(true);
		tvPosition.setFocusableInTouchMode(true);
		tvPosition.requestFocus();
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		tvRain = (TextView) findViewById(R.id.tvRain);
		tvTempUnit = (TextView) findViewById(R.id.tvTempUnit);
		ivPhenomenon = (ImageView) findViewById(R.id.ivPhenomenon);
		tvPhenomenon = (TextView) findViewById(R.id.tvPhenomenon);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvWind = (TextView) findViewById(R.id.tvWind);
		tvQuality = (TextView) findViewById(R.id.tvQuality);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvVersion.setText(getVersion());
		tvLogout = (TextView) findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		llFeedBack = (LinearLayout) findViewById(R.id.llFeedBack);
		llFeedBack.setOnClickListener(this);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
		llClearCache = (LinearLayout) findViewById(R.id.llClearCache);
		llClearCache.setOnClickListener(this);
		tvCache = (TextView) findViewById(R.id.tvCache);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.setting));
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		reLocation = (RelativeLayout) findViewById(R.id.reLocation);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
		if (userName != null) {
			tvUserName.setText(userName);
		}
		
		try {
			String cache = DataCleanManager.getCacheSize(mContext);
			tvCache.setText(cache);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		startLocation();
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	private String getVersion() {
	    try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	        return "V"+info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
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
        	String district = amapLocation.getDistrict();
        	String street = amapLocation.getStreet();
        	if (district != null && street != null) {
        		tvPosition.setText(district +" "+ street);
			}
        	query(amapLocation.getLongitude(), amapLocation.getLatitude());
        	getWeatherInfo(amapLocation.getLongitude(), amapLocation.getLatitude());
        }
	}
	
	/**
	 * 异步加载一小时内降雨、或降雪信息
	 * @param lng
	 * @param lat
	 */
	private void query(double lng, double lat) {
		String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
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
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("result")) {
							JSONObject objResult = object.getJSONObject("result");
							if (!objResult.isNull("minutely")) {
								JSONObject objMin = objResult.getJSONObject("minutely");
								if (!objMin.isNull("description")) {
									String rain = objMin.getString("description");
									if (!TextUtils.isEmpty(rain)) {
										tvRain.setText(rain);
										tvRain.setVisibility(View.VISIBLE);
									}else {
										tvRain.setVisibility(View.GONE);
									}
								}
							}
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
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
												if (!object.isNull("l7")) {
													String time = object.getString("l7");
													if (time != null) {
										    			tvTime.setText(time + getString(R.string.update));
										    		}
												}
												if (!object.isNull("l5")) {
													String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
													if (weatherCode != null) {
														Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
														drawable.setLevel(Integer.valueOf(weatherCode));
														ivPhenomenon.setBackground(drawable);
														tvPhenomenon.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
													}
												}
												if (!object.isNull("l1")) {
													String factTemp = WeatherUtil.lastValue(object.getString("l1"));
													if (factTemp != null) {
														tvTemp.setText(factTemp);
														tvTempUnit.setText(getString(R.string.unit_degree));
													}
												}
												if (!object.isNull("l2")) {
													String humidity = WeatherUtil.lastValue(object.getString("l2"));
													if (humidity != null) {
														tvHumidity.setText(getString(R.string.humidity) + humidity + getString(R.string.unit_percent));
													}
												}
												
												String windForce = "";
												if (!object.isNull("l3")) {
													windForce = WeatherUtil.lastValue(object.getString("l3"));
												}
												String windDir = "";
												if (!object.isNull("l4")) {
													windDir = WeatherUtil.lastValue(object.getString("l4"));
												}
												if (windForce != null && windDir != null) {
													tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) 
															+ getString(WeatherUtil.getWindForce(Integer.valueOf(windForce))));
												}
											} catch (JSONException e) {
												e.printStackTrace();
											}
											
											//空气质量
											try {
												JSONObject obj = content.getAirQualityInfo();
												if (obj != null) {
													if (!obj.isNull("k3")) {
														String num = WeatherUtil.lastValue(obj.getString("k3"));
														if (!TextUtils.isEmpty(num)) {
															tvQuality.setText(getString(R.string.weather_quality) + 
																	num + " " + WeatherUtil.getAqi(mContext, Integer.valueOf(num)));
														}
													}
												}
											} catch (JSONException e) {
												e.printStackTrace();
											}
											
											progressBar.setVisibility(View.GONE);
											reLocation.setVisibility(View.VISIBLE);
										}
									}
									
									@Override
									public void onError(Throwable error, String content) {
										super.onError(error, content);
										progressBar.setVisibility(View.GONE);
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
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void deleteDialog(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					DataCleanManager.clearCache(mContext);
					try {
						String cache = DataCleanManager.getCacheSize(mContext);
						if (cache != null) {
							tvCache.setText(cache);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				dialog.dismiss();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.llFeedBack:
			Intent intent = new Intent(mContext, FeedbackActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_feedback));
			intent.putExtra(CONST.INTENT_APPID, com.tj.common.CONST.APPID);
			startActivity(intent);
			break;
		case R.id.llVersion:
			PgyUtil.PgyUpdate(SettingActivity.this, false);
//			UmengUpdateAgent.forceUpdate(mContext);
//			UmengUpdateAgent.setUpdateOnlyWifi(false);
//			UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
//				@Override
//				public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
//					switch (updateStatus) {
//			        case UpdateStatus.Yes: // has update
//			            UmengUpdateAgent.showUpdateDialog(mContext, updateInfo);
//			            break;
//			        case UpdateStatus.No: // has no update
//			            Toast.makeText(mContext, getString(R.string.already_new), Toast.LENGTH_SHORT).show();
//			            break;
//			        case UpdateStatus.NoneWifi: // none wifi
////			            Toast.makeText(mContext, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
//			            break;
//			        case UpdateStatus.Timeout: // time out
//			            Toast.makeText(mContext, getString(R.string.connect_timeout), Toast.LENGTH_SHORT).show();
//			            break;
//			        }
//				}
//			});
			break;
		case R.id.llClearCache:
			deleteDialog(getString(R.string.delete_cache), getString(R.string.sure_delete_cache));
			break;
		case R.id.tvLogout:
			SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.clear();
			editor.commit();
			startActivity(new Intent(mContext, LoginActivity.class));
			finish();
			MyApplication.destoryActivity(com.tj.common.CONST.MainActivity);
			break;

		default:
			break;
		}
	}
}
