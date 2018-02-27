package com.tj;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

import com.baselibrary.BaseActivity;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.utils.WeatherUtil;
import com.baselibrary.view.MyDialog;
import com.tj.dto.CityDto;
import com.tj.dto.WeatherDto;
import com.tj.view.WeeklyView;
import cxwl.shawn.tj.decision.R;

public class WeatherDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private TextView tvTime = null;//更新时间
	private TextView tvRain = null;
	private TextView tvTemp = null;//实况温度
	private TextView tvTempUnit = null;
	private ImageView ivPhenomenon = null;//天气显现对应的图标
	private TextView tvPhenomenon = null;//天气现象
	private TextView tvHumidity = null;//相对湿度
	private TextView tvWind = null;//风速
	private TextView tvQuality = null;//空气质量
	private CityDto data = null;
	private LinearLayout container = null;//一周预报容器
	private List<WeatherDto> weeklyList = new ArrayList<WeatherDto>();
	private RelativeLayout reMain = null;
	private MyDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_detail);
		mContext = this;
		showDialog();
		initWidget();
	}
	
	/**
	 * 初始化dialog
	 */
	private void showDialog() {
		mDialog = new MyDialog(mContext);
		if (mDialog != null) {
			mDialog.show();
		}
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
		tvRain = (TextView) findViewById(R.id.tvRain);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		tvTempUnit = (TextView) findViewById(R.id.tvTempUnit);
		ivPhenomenon = (ImageView) findViewById(R.id.ivPhenomenon);
		tvPhenomenon = (TextView) findViewById(R.id.tvPhenomenon);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvWind = (TextView) findViewById(R.id.tvWind);
		tvQuality = (TextView) findViewById(R.id.tvQuality);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		container = (LinearLayout) findViewById(R.id.container);
		reMain = (RelativeLayout) findViewById(R.id.reMain);
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				tvTitle.setText(data.cityName);
				query(data.lng, data.lat);
				getWeatherInfo(data.cityId);
			}
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
	private void getWeatherInfo(String cityId) {
		if (cityId != null) {
			WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
				@Override
				public void onComplete(Weather content) {
					super.onComplete(content);
					cancelDialog();
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
						
						//一周预报信息
						try {
							//这里只去一周预报，默认为15天，所以遍历7次
							for (int i = 1; i <= 7; i++) {
								WeatherDto dto = new WeatherDto();
								
								JSONArray weeklyArray = content.getWeatherForecastInfo(i);
								JSONObject weeklyObj = weeklyArray.getJSONObject(0);
								
								//晚上
								dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
								dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
								dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
								
								//白天数据缺失时，就使用晚上数据
								if (TextUtils.isEmpty(weeklyObj.getString("fa"))) {
//									dto.highPheCode = Integer.valueOf(weeklyObj.getString("fb"));//天气现象编号
//									dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));//天气现象
//									dto.highTemp = dto.lowTemp+5;//白天气温
									
									JSONObject secondObj = content.getWeatherForecastInfo(2).getJSONObject(0);
									dto.highPheCode = Integer.valueOf(secondObj.getString("fa"));
									dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(secondObj.getString("fa"))));
									dto.highTemp = Integer.valueOf(secondObj.getString("fc"));
								}else {
									//白天
									dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
									dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
									dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
								}
								
								JSONArray timeArray =  content.getTimeInfo(i);
								JSONObject timeObj = timeArray.getJSONObject(0);
								dto.week = timeObj.getString("t4");//星期几
								dto.date = timeObj.getString("t1");//日期
								
								weeklyList.add(dto);
							}
							
							WeeklyView weeklyView = new WeeklyView(mContext);
							weeklyView.setData(weeklyList);
							container.addView(weeklyView);
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
						
			    		reMain.setVisibility(View.VISIBLE);
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
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}
}
