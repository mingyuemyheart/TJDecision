package com.baselibrary.video;

/**
 * 实景天气
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;

public class FactWeatherActivity extends BaseActivity implements OnMarkerClickListener, OnClickListener, InfoWindowAdapter{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private String videoUrl = "http://decision.tianqi.cn//data/video/videoweather.html";//天气视频地址
	private List<FactWeatherDto> videoList = new ArrayList<FactWeatherDto>();
	private MyDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fact_weather);
		mContext = this;
		showDialog();
		initWidget();
		initMap(savedInstanceState);
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
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		if (getIntent().hasExtra(CONST.ACTIVITY_NAME)) {
			String name = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
			if (name != null) {
				tvTitle.setText(name);
			}
		}
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
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.moveCamera(CameraUpdateFactory.zoomTo(4.0f));
		aMap.setInfoWindowAdapter(this);
	}
	
	/**
	 * 获取天气网眼数据
	 */
	private void getData() {
		//异步请求数据
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(videoUrl);
	}
	
	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap() {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.fact_weather_item, null);
		if (view == null) {
			return null;
		}
		return view;
	}
	
	/**
	 * 添加marker
	 */
	private void addMarker(List<FactWeatherDto> list) {
		if (list.isEmpty()) {
			return;
		}
		
		int length = list.size();
		for (int i = 0; i < length; i++) {
			MarkerOptions options = new MarkerOptions();
			options.snippet(list.get(i).tag);
			options.anchor(0.5f, 0.5f);
			options.position(new LatLng(Double.valueOf(list.get(i).latitude), Double.valueOf(list.get(i).longitude)));
			options.icon(BitmapDescriptorFactory.fromView(getTextBitmap()));
			aMap.addMarker(options);
		}
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		String cityName = null;
		String webUrl = null;
		int length = videoList.size();
		for (int i = 0; i < length; i++) {
			if (TextUtils.equals(marker.getSnippet(), videoList.get(i).tag)) {
				cityName = videoList.get(i).name;
				webUrl = videoList.get(i).url;
				break;
			}
		}
		
		Intent intent = new Intent(mContext, FactWeatherDetailActivity.class);
		intent.putExtra("cityName", cityName);
		intent.putExtra("webUrl", webUrl);
		startActivity(intent);
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
			cancelDialog();
			if (result != null) {
				videoList.clear();
				try {
					JSONArray array = new JSONArray(result.toString());
					for (int i = 0; i < array.length(); i++) {
						FactWeatherDto dto = new FactWeatherDto();
						JSONObject obj = array.getJSONObject(i);
						if (!obj.isNull("name")) {
							dto.name = obj.getString("name");
						}
						if (!obj.isNull("url")) {
							dto.url = obj.getString("url");
						}
						if (!obj.isNull("lat")) {
							dto.latitude = obj.getString("lat");
						}
						if (!obj.isNull("lon")) {
							dto.longitude = obj.getString("lon");
						}
						dto.tag = "tag"+i;
						
						videoList.add(dto);
					}
					addMarker(videoList);
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

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}

}
