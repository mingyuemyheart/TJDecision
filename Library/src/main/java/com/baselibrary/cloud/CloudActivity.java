package com.baselibrary.cloud;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.district.DistrictSearchQuery;
import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.cloud.CloudManager.Classify;
import com.baselibrary.cloud.CloudManager.RadarListener;
import com.baselibrary.common.CONST;
import com.baselibrary.manager.RainManager;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;

public class CloudActivity extends BaseActivity implements OnClickListener, OnCameraChangeListener, RadarListener, OnSeekBarChangeListener, OnDistrictSearchListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private static final int HANDLER_SHOW_RADAR = 0;
	private static final int HANDLER_PROGRESS = 1;
	private static final int HANDLER_ZOOM = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
//	private float mLevel = CONST.zoom;
	private CloudManager mRadarManager = null;
	private RadarThread mRadarThread = null;
	private GroundOverlay mOverlay = null;
	private LinearLayout llSeekBar = null;
	private ImageView ivPlay = null;//播放暂停按钮
	private SeekBar seekBar = null;//进度条
	private TextView tvTime = null;//时间
	private MyDialog mDialog = null;
	private List<CloudDto> cloudList = new ArrayList<CloudDto>();
	long time = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud);
		mContext = this;
		initWidget();
		initAmap(savedInstanceState);
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
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);
		tvTime = (TextView) findViewById(R.id.tvTime);
		llSeekBar = (LinearLayout) findViewById(R.id.llSeekBar);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		time = System.currentTimeMillis();
		Log.d("time1", time+"");
		getRadarData();//加载第一张图片
	}
	
	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		
		LatLng latLng = new LatLng(getIntent().getDoubleExtra(CONST.LATITUDE, 0), getIntent().getDoubleExtra(CONST.LONGITUDE, 0));
		aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CONST.zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnCameraChangeListener(this);
		
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
					
					polylineOption.width(10).color(0xffd29cf4);	 
					aMap.addPolyline(polylineOption);
				}
			}
 		}.start();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mapView != null) {
			mapView.onSaveInstanceState(outState);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mapView != null) {
			mapView.onResume();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mapView != null) {
			mapView.onPause();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
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
	public void onCameraChange(CameraPosition arg0) {
	}
	@Override
	public void onCameraChangeFinish(CameraPosition camera) {
//		mLat = camera.target.latitude;
//		mLng = camera.target.longitude;
//		mHandler.removeMessages(HANDLER_ZOOM);
//		Message msg = mHandler.obtainMessage();
//		msg.what = HANDLER_ZOOM;
//		msg.arg1 = (int) camera.zoom;
//		mHandler.sendMessageDelayed(msg, 2000);
	}
	
	/**
	 * 获取雷达图片集信息
	 */
	private void getRadarData() {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(CloudManager.RADAR_CLOUD_URL);
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
				try {
					JSONObject obj = new JSONObject(result.toString());
					if (!obj.isNull("l")) {
						JSONArray array = obj.getJSONArray("l");
						cloudList.clear();
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							CloudDto radar = new CloudDto();
							radar.time = itemObj.getString("l1");
							radar.url = itemObj.getString("l2");
							cloudList.add(0, radar);
							
							if (i == array.length()-1) {
								if (!TextUtils.isEmpty(radar.url)) {
									String date = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmm");
									downloadPortrait(CloudManager.getRadarUrl(radar.url, date));
								}
							}
						}
						
						time = System.currentTimeMillis()-time;
						Log.d("time2", time+"");
						llSeekBar.setVisibility(View.VISIBLE);
						Message msg = new Message();
						msg.what = HANDLER_ZOOM;
						msg.arg1 = (int) CONST.zoom;
						mHandler.sendMessage(msg);
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
	 * 下载头像保存在本地
	 */
	private void downloadPortrait(String imgUrl) {
		AsynLoadTask task = new AsynLoadTask(new AsynLoadCompleteListener() {
			@Override
			public void loadComplete(Bitmap bitmap) {
				if (bitmap != null) {
					showRadar(bitmap);
				}
			}
		}, imgUrl);  
        task.execute();
	}
	
	private interface AsynLoadCompleteListener {
		public void loadComplete(Bitmap bitmap);
	}
    
	private class AsynLoadTask extends AsyncTask<Void, Bitmap, Bitmap> {
		
		private String imgUrl;
		private AsynLoadCompleteListener completeListener;

		private AsynLoadTask(AsynLoadCompleteListener completeListener, String imgUrl) {
			this.imgUrl = imgUrl;
			this.completeListener = completeListener;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onProgressUpdate(Bitmap... values) {
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = CommonUtil.getHttpBitmap(imgUrl);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (completeListener != null) {
				completeListener.loadComplete(bitmap);
            }
		}
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR: {
				if (msg.obj != null) {
					Bitmap bmp = (Bitmap) msg.obj;
					Bundle bundle = msg.getData();
					String time = bundle.getString("time");
					if (bmp != null) {
						showRadar(bmp);
					}
					changeProgress(time, msg.arg2, msg.arg1);
				}
			}
				break;
			case HANDLER_PROGRESS: {
				int progress = (Integer)msg.obj;
				if (mDialog != null) {
					mDialog.setPercent(progress);
				}
			}
				break;
			case HANDLER_LOAD_FINISHED: {
				cancelDialog();
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_pause);
				}
			}
				break;
			case HANDLER_ZOOM: 
				loadImages(msg.arg1, Classify.CLOUD);
				break;
			case HANDLER_PAUSE:
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_play);
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	/**
	 * 下载图片
	 * @param level
	 * @param classify
	 */
	private void loadImages(int level, Classify classify) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		
		if (mRadarManager == null) {
			mRadarManager = new CloudManager(mContext);
		}
 		mRadarManager.loadImagesAsyn(cloudList, Classify.CLOUD, CONST.zoom, this);
	}
	
	@Override
	public void onResult(int result, List<CloudDto> images, Bundle data) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarListener.RESULT_SUCCESSED) {
			if (mRadarThread != null) {
				mRadarThread.cancel();
				mRadarThread = null;
			}
			mRadarThread = new RadarThread(images, data);
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
	
	private void showRadar(Bitmap bitmap) {
//		DisplayMetrics dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
//		Point leftPoint = new Point(0, dm.heightPixels);
//		Point rightPoint = new Point(dm.widthPixels, 0);
//		LatLng leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
//		LatLng rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
		.include(new LatLng(-31.828567,65.866752))
		.include(new LatLng(68.953803,144.952197))
		.build();
		
		if (mOverlay == null) {
			mOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
				.anchor(0.5f, 0.5f)
				.positionFromBounds(bounds)
				.image(fromView)
				.transparency(0.2f));
		} else {
			mOverlay.setImage(null);
			mOverlay.setPositionFromBounds(bounds);
			mOverlay.setImage(fromView);
		}
		aMap.runOnDrawFrame();
	}
	
	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		tvTime.setText(time);
	}
	
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	}
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		if (mRadarThread != null) {
			mRadarThread.startTracking();
		}
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (mRadarThread != null) {
			mRadarThread.setCurrent(seekBar.getProgress());
			mRadarThread.stopTracking();
		}
	}
	
	private class RadarThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<CloudDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;
		public RadarThread(List<CloudDto> images, Bundle data) {
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
//				if (mRadarThread != null) {
//					mRadarThread.pause();
//					
//					Message message = mHandler.obtainMessage();
//					message.what = HANDLER_PAUSE;
//					mHandler.sendMessage(message);
//				}
			}
			
			CloudDto radar = images.get(index);
			Bitmap bit = BitmapFactory.decodeFile(radar.path);
			Message message = mHandler.obtainMessage();
			message.what = HANDLER_SHOW_RADAR;
			message.obj = bit;
			message.arg1 = count - 1;
			message.arg2 = index ++;
			Bundle bun = new Bundle();
			bun.putString("time", radar.getTime());
			message.setData(bun);
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
			} else if (mRadarThread == null) {
				initDialog();
//				mHandler.removeMessages(HANDLER_ZOOM);
//				Message msg = mHandler.obtainMessage();
//				Message msg = new Message();
//				msg.what = HANDLER_ZOOM;
//				msg.arg1 = (int) CONST.zoom;
//				mHandler.sendMessage(msg);
			}
		}
	}

}
