package com.baselibrary.warning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
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
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.CustomHttpClient;

public class WarningFragment extends Fragment implements OnClickListener, OnMapClickListener, OnMarkerClickListener, InfoWindowAdapter, OnCameraChangeListener, OnDistrictSearchListener{
	
	private TextView tvPrompt = null;//没有数据时提示
	private TextView tvTJPrompt = null;
	private ImageView ivLocation = null;
	private boolean isExtend = false;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private Marker selectMarker = null;
	private String url = "http://decision.tianqi.cn/alarm12379/grepalarm2.php?areaid=";
	private ListView cityListView = null;
	private WarningAdapter cityAdapter = null;
	private List<WarningDto> cityList = new ArrayList<WarningDto>();
	private RelativeLayout reList = null;
	private LinearLayout llSwitch = null;
	private ImageView ivSwitch = null;
	private TextView tvSwitch = null;
	private LinearLayout llRefresh = null;
	private ImageView ivRefresh = null;
	private ImageView ivPlus = null;
	private ImageView ivMinuse = null;
	private LatLng centerLatLng = null;//中心点
	private ArrayList<BitmapDescriptor> blueIcons = new ArrayList<BitmapDescriptor>();//蓝色预警帧图片
	private ArrayList<BitmapDescriptor> yellowIcons = new ArrayList<BitmapDescriptor>();//黄色预警帧图片
	private ArrayList<BitmapDescriptor> orangeIcons = new ArrayList<BitmapDescriptor>();//橙色预警帧图片
	private ArrayList<BitmapDescriptor> redIcons = new ArrayList<BitmapDescriptor>();//红色预警帧图片
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.warning_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initAmap(savedInstanceState, view);
		initListView(view);
		
		String warningId = getArguments().getString(CONST.WARNING_ID);
		if (warningId != null) {
			url = url + warningId;
			asyncQuery(url);
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		ivLocation = (ImageView) view.findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		tvTJPrompt = (TextView) view.findViewById(R.id.tvTJPrompt);
		reList = (RelativeLayout) view.findViewById(R.id.reList);
		llSwitch = (LinearLayout) view.findViewById(R.id.llSwitch);
		llSwitch.setOnClickListener(this);
		ivPlus = (ImageView) view.findViewById(R.id.ivPlus);
		ivPlus.setOnClickListener(this);
		ivMinuse = (ImageView) view.findViewById(R.id.ivMinuse);
		ivMinuse.setOnClickListener(this);
		llRefresh = (LinearLayout) view.findViewById(R.id.llRefresh);
		llRefresh.setOnClickListener(this);
		ivRefresh = (ImageView) view.findViewById(R.id.ivRefresh);
		ivSwitch = (ImageView) view.findViewById(R.id.ivSwitch);
		tvSwitch = (TextView) view.findViewById(R.id.tvSwitch);
		
		blueIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_blue_marker1));
		blueIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_blue_marker2));
		blueIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_blue_marker3));
		blueIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_blue_marker4));
		blueIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_blue_marker5));
		
		yellowIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_yellow_marker1));
		yellowIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_yellow_marker2));
		yellowIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_yellow_marker3));
		yellowIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_yellow_marker4));
		yellowIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_yellow_marker5));
		
		orangeIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_orange_marker1));
		orangeIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_orange_marker2));
		orangeIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_orange_marker3));
		orangeIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_orange_marker4));
		orangeIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_orange_marker5));
		
		redIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_red_marker1));
		redIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_red_marker2));
		redIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_red_marker3));
		redIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_red_marker4));
		redIcons.add(BitmapDescriptorFactory.fromResource(R.drawable.iv_red_marker5));
		
    }
	
	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle, View view) {
		mapView = (MapView) view.findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		
		centerLatLng = new LatLng(getArguments().getDouble(CONST.LATITUDE, 0), getArguments().getDouble(CONST.LONGITUDE, 0));
		float zoom = 6.0f;
		String appid = getArguments().getString(CONST.INTENT_APPID);
		if (TextUtils.equals(appid, "13")) {//西藏
			zoom = 6.0f;
			drawDistrict();
		}else if (TextUtils.equals(appid, "14")) {//天津
			zoom = 9.0f;
			String name = getArguments().getString(CONST.PROVINCE_NAME);
			if (name != null) {
				drawDistrict(name);
			}
		}else if (TextUtils.equals(appid, "15")) {//贵州
			zoom = 7.5f;
			String name = getArguments().getString(CONST.PROVINCE_NAME);
			if (name != null) {
				drawDistrict(name);
			}
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);
	}
	
	/**
	 * 获取西藏边界数据
	 * @return
	 */
	private String getDistrictData() {
		AssetManager assetManager = getActivity().getAssets();
		try {
			InputStream is = assetManager.open("xizang_district.json");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer stringBuffer = new StringBuffer();
			String str = null;
			while((str = br.readLine())!=null){
				stringBuffer.append(str);
			}
			return stringBuffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 回执西藏边界，这里取得是本地数据
	 */
	private void drawDistrict() {
		new Thread() {
			public void run() {
				if (!TextUtils.isEmpty(getDistrictData())) {
					try {
						JSONObject obj = new JSONObject(getDistrictData());
						if (!obj.isNull("geometries")) {
							JSONArray array = obj.getJSONArray("geometries");
							for (int i = 0; i < array.length(); i++) {
								JSONObject itemObj = array.getJSONObject(i);
								Log.d("itemObj", itemObj.toString());
								if (!itemObj.isNull("coordinates")) {
									JSONArray itemArray = itemObj.getJSONArray("coordinates");
									Log.d("itemArray", itemArray.toString());
									for (int j = 0; j < itemArray.length(); j++) {
										JSONArray itemArray2 = itemArray.getJSONArray(j);
										Log.d("itemArray2", itemArray2.toString());
										
										PolylineOptions polylineOption = new PolylineOptions();
										for (int k = 0; k < itemArray2.length(); k++) {
											JSONArray itemArray3 = itemArray2.getJSONArray(k);
											Log.d("itemArray3", itemArray3.toString());
											double lat = itemArray3.getDouble(1);
											Log.d("lat", lat+"");
											double lng = itemArray3.getDouble(0);
											Log.d("lng", lng+"");
											LatLng latLng = new LatLng(lat, lng);
											polylineOption.add(latLng);
										}
										polylineOption.width(10).color(0xffd29cf4);	 
										aMap.addPolyline(polylineOption);
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
 		}.start();
	}
	
	/**
	 * 绘制区域
	 */
	private void drawDistrict(String keywords) {
		DistrictSearch search = new DistrictSearch(getActivity());
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
	public void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		cityListView = (ListView) view.findViewById(R.id.cityListView);
		cityAdapter = new WarningAdapter(getActivity(), cityList);
		cityListView.setAdapter(cityAdapter);
		cityListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(cityList.get(arg2));
			}
		});
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
						if (!object.isNull("count")) {
							String count = object.getString("count");
							if (count != null) {
								String appid = getArguments().getString(CONST.INTENT_APPID);
								if (count.equals("0")) {
									if (TextUtils.equals(appid, "13")) {//西藏
										tvPrompt.setText(getString(R.string.xizang_province) + getString(R.string.no_warning));
										tvPrompt.setVisibility(View.VISIBLE);
									}else if (TextUtils.equals(appid, "14")) {//天津
										tvPrompt.setText(getString(R.string.tianjin_city) + getString(R.string.no_warning));
										tvTJPrompt.setText(getString(R.string.tianjin_city) + getString(R.string.no_warning));
										tvPrompt.setVisibility(View.GONE);
										tvTJPrompt.setVisibility(View.VISIBLE);
									}else if (TextUtils.equals(appid, "15")) {//贵州
										tvPrompt.setText(getString(R.string.guizhou_province) + getString(R.string.no_warning));
										tvPrompt.setVisibility(View.VISIBLE);
									}
									llSwitch.setVisibility(View.GONE);
									ivRefresh.clearAnimation();
									return;
								}else {
									if (TextUtils.equals(appid, "13")) {//西藏
										tvPrompt.setText(getString(R.string.xizang_province) + getString(R.string.publish) + count + getString(R.string.tiao) + getString(R.string.warning));
										tvPrompt.setVisibility(View.VISIBLE);
									}else if (TextUtils.equals(appid, "14")) {//天津
										tvPrompt.setText(getString(R.string.tianjin_city) + getString(R.string.publish) + count + getString(R.string.tiao) + getString(R.string.warning));
										tvTJPrompt.setText(getString(R.string.tianjin_city) + getString(R.string.publish) + count + getString(R.string.tiao) + getString(R.string.warning));
										tvPrompt.setVisibility(View.GONE);
										tvTJPrompt.setVisibility(View.VISIBLE);
									}else if (TextUtils.equals(appid, "15")) {//贵州
										tvPrompt.setText(getString(R.string.guizhou_province) + getString(R.string.publish) + count + getString(R.string.tiao) + getString(R.string.warning));
										tvPrompt.setVisibility(View.VISIBLE);
									}
									llSwitch.setVisibility(View.VISIBLE);
								}
							}
						}
						
						if (!object.isNull("data")) {
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
								dto.type = item2.substring(0, 5);
								dto.color = item2.substring(5, 7);
								dto.time = item1;
								dto.lng = tempArray.optString(2);
								dto.lat = tempArray.optString(3);
								dto.name = tempArray.optString(0);
								
								dto.tag = "tag"+i;
								
								cityList.add(dto);
							}
							
							ivRefresh.clearAnimation();
							aMap.clear();
							addMarkersToMap(cityList);
							
							if (cityAdapter != null) {
								cityAdapter.notifyDataSetChanged();
							}
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
	 * 在地图上添加marker
	 */
	private void addMarkersToMap(List<WarningDto> list) {
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			MarkerOptions optionsTemp = new MarkerOptions();
			optionsTemp.title("tag"+i);
			if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
				optionsTemp.position(new LatLng(Double.valueOf(dto.lat), Double.valueOf(dto.lng)));
			}
			if (dto.color.equals("01")) {
				optionsTemp.icons(blueIcons);
			}else if (dto.color.equals("02")) {
				optionsTemp.icons(yellowIcons);
			}else if (dto.color.equals("03")) {
				optionsTemp.icons(orangeIcons);
			}else if (dto.color.equals("04")) {
				optionsTemp.icons(redIcons);
			}else {
				optionsTemp.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_warning_defult));
			}
	        optionsTemp.period(10);
			aMap.addMarker(optionsTemp);
		}
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		selectMarker.hideInfoWindow();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		selectMarker = marker;
		marker.showInfoWindow();
		aMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
		return false;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_info, null);
		ListView mListView = null;
		WarningAdapter mAdapter = null;
		final List<WarningDto> infoList = new ArrayList<WarningDto>();
		
		addInfoList(cityList, marker, infoList);
		
		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new WarningAdapter(getActivity(), infoList);
		mListView.setAdapter(mAdapter);
		LayoutParams params = mListView.getLayoutParams();
		if (infoList.size() <= 1) {
			params.height = (int) CommonUtil.dip2px(getActivity(), 60);
		}else if (infoList.size() > 1 && infoList.size() <= 3) {
			params.height = (int) CommonUtil.dip2px(getActivity(), 120);
		}else {
			params.height = (int) CommonUtil.dip2px(getActivity(), 180);
		}
		mListView.setLayoutParams(params);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(infoList.get(arg2));
			}
		});
		return view;
	}
	
	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(getActivity(), WarningDetailActivity.class);
		intentDetail.putExtra("data", data);
		startActivity(intentDetail);
	}
	
	private void addInfoList(List<WarningDto> list, Marker marker, List<WarningDto> infoList) {
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			if (TextUtils.equals(marker.getTitle(), dto.tag)) {
				infoList.add(dto);
			}
		}
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(boolean flag, final RelativeLayout reList, LinearLayout llSwitch, LinearLayout llRefresh) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		reList.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reList.clearAnimation();
			}
		});
		
		//地图、列表切换按钮动画
		if (flag == false) {
			AnimationSet animationSet1 = new AnimationSet(true);
			TranslateAnimation animation1 = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f);
			animation1.setDuration(200);
			TranslateAnimation animation2 = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f);
			animation2.setDuration(200);
			animation2.setStartOffset(200);
			animationSet1.addAnimation(animation1);
			animationSet1.addAnimation(animation2);
			animationSet1.setFillAfter(true);
			llSwitch.startAnimation(animationSet1);
			animationSet1.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {
				}
				@Override
				public void onAnimationRepeat(Animation arg0) {
				}
				@Override
				public void onAnimationEnd(Animation arg0) {
					ivSwitch.setImageResource(R.drawable.iv_map);
					tvSwitch.setText(getString(R.string.map));
				}
			});
		}else {
			AnimationSet animationSet2 = new AnimationSet(true);
			TranslateAnimation animation1 = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f);
			animation1.setDuration(200);
			TranslateAnimation animation2 = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,1.0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f);
			animation2.setDuration(200);
			animation2.setStartOffset(200);
			animationSet2.addAnimation(animation1);
			animationSet2.addAnimation(animation2);
			animationSet2.setFillAfter(true);
			llSwitch.startAnimation(animationSet2);
			animationSet2.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {
				}
				@Override
				public void onAnimationRepeat(Animation arg0) {
				}
				@Override
				public void onAnimationEnd(Animation arg0) {
					ivSwitch.setImageResource(R.drawable.iv_table);
					tvSwitch.setText(getString(R.string.table));
				}
			});
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ivLocation) {
			if (isExtend) {
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, CONST.zoom));
				ivLocation.setImageResource(R.drawable.iv_map_collose);
				isExtend = false;
			}else {
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 4.0f));
				ivLocation.setImageResource(R.drawable.iv_map_expand);
				isExtend = true;
			}
		}else if (v.getId() == R.id.llSwitch) {
			if (reList.getVisibility() == View.GONE) {
				startAnimation(false, reList, llSwitch, llRefresh);
				reList.setVisibility(View.VISIBLE);
			}else {
				startAnimation(true, reList, llSwitch, llRefresh);
				reList.setVisibility(View.GONE);
			}
		}else if (v.getId() == R.id.llRefresh) {
			Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.round_animation);
			ivRefresh.startAnimation(animation);
			cityList.clear();
			asyncQuery(url);
		}else if (v.getId() == R.id.ivPlus) {
			aMap.moveCamera(CameraUpdateFactory.zoomIn());
		}else if (v.getId() == R.id.ivMinuse) {
			aMap.moveCamera(CameraUpdateFactory.zoomOut());
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		//计算地区跨度
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		Projection myProjection = aMap.getProjection();
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		LatLng leftlatlng = myProjection.fromScreenLocation(leftPoint);
		LatLng rightLatlng = myProjection.fromScreenLocation(rightPoint);
		
		double a = (leftlatlng.latitude+rightLatlng.latitude)/2;
		double b = (leftlatlng.longitude+rightLatlng.longitude)/2;
	}
	
}
