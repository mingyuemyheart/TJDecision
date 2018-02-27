package com.tj.fragment;

/**
 * 天气服务
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.baselibrary.common.CONST;
import com.baselibrary.common.ColumnData;
import com.baselibrary.index.IndexActivity;
import com.baselibrary.news.NewsActivity;
import com.baselibrary.news.ProductActivity;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.utils.WeatherUtil;
import com.baselibrary.warning.WarningDto;
import com.tj.DistrictForecastActivity;
import com.tj.FactCheckActivity;
import com.tj.HeadWarningActivity;
import com.tj.QuestionActivity;
import com.tj.QuickPaperActivity;
import cxwl.shawn.tj.decision.R;
import com.tj.ServiceActivity;
import com.tj.view.CircleMenuLayout;
import com.tj.view.CircleMenuLayout.OnMenuItemClickListener;

public class ServiceFragment extends Fragment implements AMapLocationListener, OnClickListener{
	
	private TextView tvDes = null;
	private MyBroadCastReceiver mReceiver = null;
	private CircleMenuLayout mCircleMenuLayout = null;
	private List<ColumnData> showList = new ArrayList<ColumnData>();
	private ColumnData data = null;
	private TextView tvPosition = null;//定位地点
	private ImageView ivPhe = null;//天气现象
	private TextView tvTemp = null;//实况温度
	private TextView tvPhe = null;//天气现象
	private LinearLayout llFact = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private ImageView ivCloud = null;//北京云层动画
	private ImageView ivWarning = null;//预警图标
	private List<WarningDto> warningList = new ArrayList<WarningDto>();//预警列表

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.service_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		startLocation();
		initBroadCast();
	}
	
	private void initWidget(View view) {
		tvPosition = (TextView) view.findViewById(R.id.tvPosition);
		ivPhe = (ImageView) view.findViewById(R.id.ivPhe);
		tvTemp = (TextView) view.findViewById(R.id.tvTemp);
		tvPhe = (TextView) view.findViewById(R.id.tvPhe);
		tvDes = (TextView) view.findViewById(R.id.tvDes);
		llFact = (LinearLayout) view.findViewById(R.id.llFact);
		ivWarning = (ImageView) view.findViewById(R.id.ivWarning);
		ivWarning.setOnClickListener(this);
		ivCloud = (ImageView) view.findViewById(R.id.ivCloud);
		
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,1f,
				Animation.RELATIVE_TO_SELF,-1f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f);
		animation.setDuration(30000);
		animation.setRepeatCount(Animation.INFINITE);
		ivCloud.startAnimation(animation);
		
		data = getArguments().getParcelable("data");
		if (data != null) {
			initCircleLayout(view);
		}
		
		//获取预警信息
		String url = "http://decision.tianqi.cn/alarm12379/grepalarm2.php?areaid="+com.tj.common.CONST.TIANJIN_WARNINGID;
		asyncQuery(url);
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(getActivity());//初始化定位
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
        	if (district != null) {
        		tvPosition.setText(district);
			}
        	getWeatherInfo(amapLocation.getLongitude(), amapLocation.getLatitude());
        }
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(double lng, double lat) {
		WeatherAPI.getGeo(getActivity(), String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							try {
								String cityId = geoObj.getString("id").substring(0, 9);
								if (cityId != null) {
									WeatherAPI.getWeather2(getActivity(), cityId, Language.ZH_CN, new AsyncResponseHandler() {
										@Override
										public void onComplete(Weather content) {
											super.onComplete(content);
											if (content != null) {
												//实况信息
												JSONObject object = content.getWeatherFactInfo();
												try {
													if (!object.isNull("l5")) {
														String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
														if (weatherCode != null) {
															Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
															drawable.setLevel(Integer.valueOf(weatherCode));
															ivPhe.setBackground(drawable);
															tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
														}
													}
													if (!object.isNull("l1")) {
														String factTemp = WeatherUtil.lastValue(object.getString("l1"));
														if (factTemp != null) {
															tvTemp.setText(factTemp + getString(R.string.unit_degree));
														}
													}
													llFact.setVisibility(View.VISIBLE);
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
							} catch (IndexOutOfBoundsException e) {
								e.printStackTrace();
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
	
	private void initCircleLayout(View view) {
		final int size = data.child.size();
		showList.clear();
		if (size > 6) {
			for (int i = 0; i < 5; i++) {
				showList.add(data.child.get(i));
			}
			ColumnData dto = new ColumnData();
			dto.name = getString(R.string.more);
			dto.desc = getString(R.string.look_more_column);
			showList.add(dto);
		}else {
			showList.addAll(data.child);
		}
		
		mCircleMenuLayout = (CircleMenuLayout) view.findViewById(R.id.id_menulayout);
		mCircleMenuLayout.setMenuItemIconsAndTexts(showList);
		mCircleMenuLayout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void itemClick(View view, int pos) {
				String showType = data.child.get(pos).showType;
				String id = data.child.get(pos).id;
				Intent intent = null;
				if (pos == 5) {
					intent = new Intent(getActivity(), ServiceActivity.class);
				}else {
					if (TextUtils.equals(showType, CONST.LOCAL)) {
						if (TextUtils.equals(id, "12")) {
							intent = new Intent(getActivity(), DistrictForecastActivity.class);
						}else if (TextUtils.equals(id, "22")) {
							intent = new Intent(getActivity(), IndexActivity.class);
						}else if (TextUtils.equals(id, "13")) {
							intent = new Intent(getActivity(), QuickPaperActivity.class);
						}else if (TextUtils.equals(id, "65")) {
							intent = new Intent(getActivity(), FactCheckActivity.class);
						}
					}else if (TextUtils.equals(showType, CONST.NEWS)) {
						intent = new Intent(getActivity(), NewsActivity.class);
					}else if (TextUtils.equals(showType, CONST.PRODUCT)) {
						intent = new Intent(getActivity(), ProductActivity.class);
					}else if (TextUtils.equals(showType, CONST.URL)) {
						intent = new Intent(getActivity(), QuestionActivity.class);
					}
				}
				
				if (intent != null) {
					intent.putExtra("data", data.child.get(pos));
					intent.putParcelableArrayListExtra("dataList", (ArrayList<? extends Parcelable>) data.child);
					intent.putExtra(CONST.ACTIVITY_NAME, data.child.get(pos).name);
					intent.putExtra(CONST.INTENT_APPID, com.tj.common.CONST.APPID);
					intent.putExtra(CONST.WEB_URL, data.child.get(pos).dataUrl);
					startActivity(intent);
				}
			}
			
			@Override
			public void itemCenterClick(View view) {
				
			}
		});
		
		//设置左边布局宽高
		LinearLayout llLeft = (LinearLayout) view.findViewById(R.id.llLeft);
		int w = LinearLayout.MeasureSpec.makeMeasureSpec(0, LinearLayout.MeasureSpec.UNSPECIFIED);
		int h = LinearLayout.MeasureSpec.makeMeasureSpec(0, LinearLayout.MeasureSpec.UNSPECIFIED);
		llLeft.measure(w, h);
		int width = llLeft.getMeasuredWidth();
		int height = llLeft.getMeasuredHeight();
		llLeft.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int)(height*mCircleMenuLayout.percent)));
		llLeft.setMinimumWidth((int)(width*1.5));
	}
	
	private void initBroadCast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(com.tj.common.CONST.BROADCAST_SERVICE);
		getActivity().registerReceiver(mReceiver, intentFilter);
	}
	
	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(com.tj.common.CONST.BROADCAST_SERVICE)) {
				tvDes.setText(intent.getExtras().getString(com.tj.common.CONST.DESCRIBE));
			}
		}
		
	}
	
	/**
	 * 异步请求
	 */
	private void asyncQuery(String requestUrl) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
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
						if (!object.isNull("data")) {
							warningList.clear();
							JSONArray jsonArray = object.getJSONArray("data");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONArray tempArray = jsonArray.getJSONArray(i);
								WarningDto dto = new WarningDto();
								dto.html = tempArray.optString(1);
								String[] array = dto.html.split("-");
								String item0 = array[0];
								String item1 = array[1];
								String item2 = array[2];
								
								dto.id = item2.substring(0, 7);
								dto.type = item2.substring(3, 5);
								dto.color = item2.substring(5, 7);
								dto.time = item1;
								dto.lng = tempArray.optString(2);
								dto.lat = tempArray.optString(3);
								dto.name = tempArray.optString(0);
								warningList.add(dto);
							}
						}
						
						if (warningList.size() > 0) {
							Bitmap bitmap = null;
							if (warningList.get(0).color.equals(CONST.blue[0])) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+warningList.get(0).type+CONST.blue[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
								}
							}else if (warningList.get(0).color.equals(CONST.yellow[0])) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+warningList.get(0).type+CONST.yellow[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
								}
							}else if (warningList.get(0).color.equals(CONST.orange[0])) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+warningList.get(0).type+CONST.orange[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
								}
							}else if (warningList.get(0).color.equals(CONST.red[0])) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+warningList.get(0).type+CONST.red[1]+CONST.imageSuffix);
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
								}
							}
							ivWarning.setImageBitmap(bitmap);
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivWarning:
			Intent intent = new Intent(getActivity(), HeadWarningActivity.class);
			intent.putParcelableArrayListExtra("warnList", (ArrayList<? extends Parcelable>) warningList);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
	
}
