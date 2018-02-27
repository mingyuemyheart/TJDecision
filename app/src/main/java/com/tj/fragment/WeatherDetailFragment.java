package com.tj.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.utils.WeatherUtil;
import cxwl.shawn.tj.decision.R;
import com.tj.dto.WeatherDto;
import com.tj.view.WeeklyView;

public class WeatherDetailFragment extends Fragment {

	private TextView tvRain = null;
	private TextView tvTime = null;//更新时间
	private TextView tvTemp = null;//实况温度
	private TextView tvTempUnit = null;
	private ImageView ivPhenomenon = null;//天气显现对应的图标
	private TextView tvPhenomenon = null;//天气现象
	private TextView tvHumidity = null;//相对湿度
	private TextView tvWind = null;//风速
	private TextView tvQuality = null;//空气质量
	private LinearLayout container = null;//一周预报容器
	private List<WeatherDto> weeklyList = new ArrayList<WeatherDto>();
	private RelativeLayout reMain = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.weather_detail_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		tvRain = (TextView) view.findViewById(R.id.tvRain);
		tvTime = (TextView) view.findViewById(R.id.tvTime);
		tvTemp = (TextView) view.findViewById(R.id.tvTemp);
		tvTempUnit = (TextView) view.findViewById(R.id.tvTempUnit);
		ivPhenomenon = (ImageView) view.findViewById(R.id.ivPhenomenon);
		tvPhenomenon = (TextView) view.findViewById(R.id.tvPhenomenon);
		tvHumidity = (TextView) view.findViewById(R.id.tvHumidity);
		tvWind = (TextView) view.findViewById(R.id.tvWind);
		tvQuality = (TextView) view.findViewById(R.id.tvQuality);
		container = (LinearLayout) view.findViewById(R.id.container);
		reMain = (RelativeLayout) view.findViewById(R.id.reMain);
		
		double jinnanLng = 117.357396;
		double jinnanLat = 38.937857;
		query(jinnanLng, jinnanLat);
		
		String jinNanCityId = "101031000";
		getWeatherInfo(jinNanCityId);
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
			WeatherAPI.getWeather2(getActivity(), cityId, Language.ZH_CN, new AsyncResponseHandler() {
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
												num + " " + WeatherUtil.getAqi(getActivity(), Integer.valueOf(num)));
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
							
							WeeklyView weeklyView = new WeeklyView(getActivity());
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
				}
			});
		}
	}
	
}
