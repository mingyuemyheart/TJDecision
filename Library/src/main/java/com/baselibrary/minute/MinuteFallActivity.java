package com.baselibrary.minute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
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
import com.baselibrary.minute.CaiyunManager.RadarListener;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;

public class MinuteFallActivity extends BaseActivity implements OnClickListener, RadarListener, AMapLocationListener, OnDistrictSearchListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private MyDialog mDialog = null;
	private List<MinuteFallDto> mList = new ArrayList<MinuteFallDto>();
	private String url = "http://api.tianqi.cn:8070/v1/img.py";//彩云接口数据
	private GroundOverlay mOverlay = null;

	private CaiyunManager mRadarManager;
	private RadarThread mRadarThread;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private LinearLayout llSeekBar = null;
	private ImageView ivPlay = null;
	private SeekBar seekBar = null;
	private TextView tvTime = null;
	private TextView tvRain = null;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.minute_fall);
		mContext = this;
		initMap(savedInstanceState);
		initWidget();
		getData();
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
	
	private void initWidget() {
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvTime = (TextView) findViewById(R.id.tvTime);
		llSeekBar = (LinearLayout) findViewById(R.id.llSeekBar);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvRain = (TextView) findViewById(R.id.tvRain);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		startLocation();
		mRadarManager = new CaiyunManager(getApplicationContext());
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
        	if (amapLocation.getLongitude() != 0 && amapLocation.getLatitude() != 0) {
        		query(amapLocation.getLongitude(), amapLocation.getLatitude());
        		
        		MarkerOptions options = new MarkerOptions();
        		options.position(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
        		options.anchor(0.5f, 0.5f);
        		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_location_marker));
        		aMap.addMarker(options);
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
		HttpAsyncRain task = new HttpAsyncRain();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncRain extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncRain() {
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
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};
	
	private void initMap(Bundle bundle) {
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		LatLng centerLatLng = new LatLng(getIntent().getDoubleExtra(CONST.LATITUDE, 0), getIntent().getDoubleExtra(CONST.LONGITUDE, 0));
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, CONST.zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		
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
	
	private void getData() {
		showDialog();
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
	}
	
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
				try {
					JSONObject obj = new JSONObject(result.toString());
					if (!obj.isNull("status")) {
						if (obj.getString("status").equals("ok")) {//鎴愬姛
							if (!obj.isNull("radar_img")) {
								JSONArray array = new JSONArray(obj.getString("radar_img"));
								for (int i = 0; i < array.length(); i++) {
									JSONArray array0 = array.getJSONArray(i);
									MinuteFallDto dto = new MinuteFallDto();
									dto.setImgUrl(array0.optString(0));
									dto.setTime(array0.optLong(1));
									JSONArray itemArray = array0.getJSONArray(2);
									dto.setP1(itemArray.optDouble(0));
									dto.setP2(itemArray.optDouble(1));
									dto.setP3(itemArray.optDouble(2));
									dto.setP4(itemArray.optDouble(3));
									mList.add(dto);
								}
							}
							startDownLoadImgs(mList);
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
		 * 鍙栨秷褰撳墠task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	private void startDownLoadImgs(List<MinuteFallDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
 		mRadarManager.loadImagesAsyn(list, this);
	}
	
	@Override
	public void onResult(int result, List<MinuteFallDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarListener.RESULT_SUCCESSED) {
			if (mRadarThread != null) {
				mRadarThread.cancel();
				mRadarThread = null;
			}
			mRadarThread = new RadarThread(images);
			mRadarThread.start();
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}
	
	private void showRadar(Bitmap bitmap, double p1, double p2, double p3, double p4) {
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
		.include(new LatLng(p3, p2))
		.include(new LatLng(p1, p4))
		.build();
		
		if (mOverlay == null) {
			mOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
				.anchor(0.5f, 0.5f)
				.positionFromBounds(bounds)
				.image(fromView)
				.transparency(0.0f));
		} else {
			mOverlay.setImage(null);
			mOverlay.setPositionFromBounds(bounds);
			mOverlay.setImage(fromView);
		}
		aMap.runOnDrawFrame();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR: 
				if (msg.obj != null) {
					MinuteFallDto dto = (MinuteFallDto) msg.obj;
					if (dto.getPath() != null) {
						Bitmap bitmap = BitmapFactory.decodeFile(dto.getPath());
						if (bitmap != null) {
							showRadar(bitmap, dto.getP1(), dto.getP2(), dto.getP3(), dto.getP4());
						}
					}
					changeProgress(dto.getTime(), msg.arg2, msg.arg1);
				}
				break;
			case HANDLER_PROGRESS: 
				if (mDialog != null) {
					if (msg.obj != null) {
						int progress = (Integer) msg.obj;
						mDialog.setPercent(progress);
					}
				}
				break;
			case HANDLER_LOAD_FINISHED: 
				cancelDialog();
				llSeekBar.setVisibility(View.VISIBLE);
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_pause);
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	private class RadarThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<MinuteFallDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;
		
		public RadarThread(List<MinuteFallDto> images) {
			this.images = images;
			this.count = images.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}
		
		public int getCurrentState() {
			return state;
		}
		
		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				sendRadar();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;
			}
			
			MinuteFallDto radar = images.get(index);
			Message message = mHandler.obtainMessage();
			message.what = HANDLER_SHOW_RADAR;
			message.obj = radar;
			message.arg1 = count - 1;
			message.arg2 = index ++;
			mHandler.sendMessage(message);
		}
		
		public void cancel() {
			this.state = STATE_CANCEL;
		}
		public void pause() {
			this.state = STATE_PAUSE;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}
		
		public void setCurrent(int index) {
			this.index = index;
		}
		
		public void startTracking() {
			isTracking = true;
		}
		
		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				sendRadar();
			}
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private void changeProgress(long time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String value = time + "000";
		Date date = new Date(Long.valueOf(value));
		tvTime.setText(sdf.format(date));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.ivPlay) {
			if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
				mRadarThread.pause();
				ivPlay.setImageResource(R.drawable.iv_play);
			} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
				mRadarThread.play();
				ivPlay.setImageResource(R.drawable.iv_pause);
			}
		}
	}

}
