package com.baselibrary.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.weather.api.CrowdWeatherAPI;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.http.RequestParams;
import cn.com.weather.listener.AsyncResponseHandler;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.WeatherUtil;
import com.baselibrary.view.MyDialog;

public class ReportWeatherActivity extends BaseActivity implements OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private GridView mGridView = null;
	private ReportAdapter mAdapter = null;
	private List<ReportDto> mList = new ArrayList<ReportDto>();
	private MyDialog mDialog = null;
	private TextView tvPosition = null;
	private TextView tvPressure = null;//气压
	private TextView tvPhenomenon = null;
	private ImageView ivPhe = null;
	private int phenomenonCode = -1;
	private String cityId = null;
	private double lng = 0;
	private double lat = 0;
	private TextView tvControl = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private RelativeLayout reMain = null;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private ImageView ivCamera = null;//拍照按钮
	private ImageView ivThumb = null;//缩略图
	private ImageView ivDelete = null;//删除按钮
	public static final String programDir = Environment.getExternalStorageDirectory()+"/ChinaWeather/";
	public static final String correctionImageUrl = programDir + "correction/";//天气纠错图片路径
	public static final String correctionImageName = "correction.jpg";//天气纠错图片名称
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
		mContext = this;
		showDialog();
		initWidget();
		initSensors();
		getGridViewData();
		initGridView();
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
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvPhenomenon = (TextView) findViewById(R.id.tvPhenomenon);
		tvPressure = (TextView) findViewById(R.id.tvPressure);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setText(getString(R.string.submit));
		tvControl.setOnClickListener(this);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivPhe = (ImageView) findViewById(R.id.ivPhe);
		reMain = (RelativeLayout) findViewById(R.id.reMain);
		ivCamera = (ImageView) findViewById(R.id.ivCamera);
		ivCamera.setOnClickListener(this);
		ivThumb = (ImageView) findViewById(R.id.ivThumb);
		ivDelete = (ImageView) findViewById(R.id.ivDelete);
		ivDelete.setOnClickListener(this);
		
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
        	String city = amapLocation.getCity();
        	String district = amapLocation.getDistrict();
        	if (city != null && district != null) {
        		tvPosition.setText(city +" "+ district);
			}
        	lng = amapLocation.getLongitude();
        	lat = amapLocation.getLatitude();
        	getWeatherInfo(lng, lat);
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
							cityId = geoObj.getString("id").substring(0, 9);
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
												if (!object.isNull("l5")) {
													String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
													if (weatherCode != null) {
														Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
														drawable.setLevel(Integer.valueOf(weatherCode));
														ivPhe.setBackground(drawable);
														phenomenonCode = Integer.valueOf(weatherCode);
														tvPhenomenon.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
														reMain.setVisibility(View.VISIBLE);
														tvControl.setVisibility(View.VISIBLE);
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
	
	/**
	 * 初始化传感器
	 */
	private void initSensors() {
		SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorListener(sm, Sensor.TYPE_PRESSURE);//气压传感器
	}
	
	/**
	 * 传感器监听器
	 * @param manager  传感器管理器
	 * @param sensorType  传感器类型
	 * @param textView  对应的textview
	 */
	private void sensorListener(SensorManager manager, final int sensorType) {
		if (manager != null) {
			Sensor sensor = manager.getDefaultSensor(sensorType);
			if (sensor != null) {
				SensorEventListener listener = new SensorEventListener() {
					@Override
					public void onSensorChanged(SensorEvent event) {
						float value = event.values[0];
						BigDecimal bd = new BigDecimal(value);
						float newValue = bd.setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();//四舍五入保留0位小数
						if (sensorType == Sensor.TYPE_PRESSURE) {
							String pressure = String.valueOf(newValue);
							if (pressure != null) {
								tvPressure.setText(pressure + getResources().getString(R.string.unit_hPa));
							}
						}
					}
					
					@Override
					public void onAccuracyChanged(Sensor arg0, int arg1) {
					}
				};
				manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
			}
		}
	}
	
	/**
	 * 获取gridview数据
	 */
	private void getGridViewData() {
		//晴、多云、阴
		ReportDto dto = new ReportDto();
		dto.setPhenomenonCode(0);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(1);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(2);
		mList.add(dto);
		//空position=3
		dto = new ReportDto();
		dto.setPhenomenon(null);
		dto.setPhenomenonCode(-1);
		mList.add(dto);
		
		//雨
		dto = new ReportDto();
		dto.setPhenomenonCode(7);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(8);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(9);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(10);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(11);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(12);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(3);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(4);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(5);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(19);
		mList.add(dto);
		// 空position=14
		dto = new ReportDto();
		dto.setPhenomenon(null);
		dto.setPhenomenonCode(-1);
		mList.add(dto);
		// 空position=15
		dto = new ReportDto();
		dto.setPhenomenon(null);
		dto.setPhenomenonCode(-1);
		mList.add(dto);
		
		//雪、小到中雪、中到大雪、大到暴雪
		dto = new ReportDto();
		dto.setPhenomenonCode(33);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(14);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(15);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(16);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(17);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(6);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(13);
		mList.add(dto);
		// 空position=23
		dto = new ReportDto();
		dto.setPhenomenon(null);
		dto.setPhenomenonCode(-1);
		mList.add(dto);
		
		//浮尘、扬沙、沙尘暴、强沙尘暴
		dto = new ReportDto();
		dto.setPhenomenonCode(29);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(30);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(20);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(31);
		mList.add(dto);
		
		//雾、大雾、浓雾、强浓雾、特强浓雾
		dto = new ReportDto();
		dto.setPhenomenonCode(18);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(57);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(32);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(49);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(58);
		mList.add(dto);
		// 空position=33
		dto = new ReportDto();
		dto.setPhenomenon(null);
		dto.setPhenomenonCode(-1);
		mList.add(dto);
		// 空position=34
		dto = new ReportDto();
		dto.setPhenomenon(null);
		dto.setPhenomenonCode(-1);
		mList.add(dto);
		// 空position=35
		dto = new ReportDto();
		dto.setPhenomenon(null);
		dto.setPhenomenonCode(-1);
		mList.add(dto);
		
		//霾、中度霾、重度霾、严重霾
		dto = new ReportDto();
		dto.setPhenomenonCode(53);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(54);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(55);
		mList.add(dto);
		dto = new ReportDto();
		dto.setPhenomenonCode(56);
		mList.add(dto);
	}
	
	/**
	 * 初始化gridview数据
	 */
	private void initGridView() {
		mGridView = (GridView) findViewById(R.id.gridview);
		mAdapter = new ReportAdapter(mContext, mList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (mList.get(arg2).getPhenomenonCode() != -1) {
					phenomenonCode = mList.get(arg2).getPhenomenonCode();
					mAdapter.clickPosition = arg2;
					mAdapter.notifyDataSetChanged();
				}
			}
		});
	}
	
	/**
	 * 上传实景天气
	 */
	private void submit() {
		showDialog();
		try {
			JSONObject weather = new JSONObject();
			weather.put("code", phenomenonCode);
			weather.put("wd", 0);
			weather.put("windSpeed", 0);
			
			RequestParams params = new RequestParams();
			params.put("areaId", cityId);
			params.put("lng", String.valueOf(lng));
			params.put("lat", String.valueOf(lat));
			if (tvPosition.getText().toString() != null) {
				params.put("position", tvPosition.getText().toString());
			}
			params.put("weather", weather.toString());
			
			File imgFile = new File(programDir + correctionImageUrl + correctionImageName);
			if (imgFile.exists()) {
				params.put("imgFile", imgFile);
			}
			
			CrowdWeatherAPI.uploadMyWeather(mContext, params, new AsyncResponseHandler() {
				@Override
				public void onComplete(JSONObject content) {
					super.onComplete(content);
					cancelDialog();
					try {
						JSONObject obj = new JSONObject(content.toString());
						if (!obj.isNull("status")) {
							if (obj.getString("status").equals("SUCCESS")) {
								ivCamera.setVisibility(View.VISIBLE);
								ivThumb.setVisibility(View.GONE);
								ivDelete.setVisibility(View.GONE);
								removeStayFile();
								Toast.makeText(mContext, getResources().getString(R.string.upload_phenomenon), Toast.LENGTH_SHORT).show();
							}else {
								Toast.makeText(mContext, getResources().getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onError(Throwable error, String content) {
					super.onError(error, content);
					cancelDialog();
					Toast.makeText(mContext, getResources().getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
	}
	
	/**
	 * 删除遗留的图片文件
	 */
	private void removeStayFile() {
		File removeFile = new File(programDir + correctionImageUrl + correctionImageName);
		if (removeFile.exists()) {
			removeFile.delete();
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.tvControl) {
			submit();
		}else if (v.getId() == R.id.ivCamera) {
			File files = new File(programDir + correctionImageUrl);
			if (!files.exists()) {
				files.mkdirs();
			}
			
			File file = new File(programDir + correctionImageUrl + correctionImageName);
			Uri uri = Uri.fromFile(file);
			
			Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intentCamera.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
			intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(intentCamera, 0);
		}else if (v.getId() == R.id.ivDelete) {
			ivCamera.setVisibility(View.VISIBLE);
			ivThumb.setVisibility(View.GONE);
			ivDelete.setVisibility(View.GONE);
			removeStayFile();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				try {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 5;
					Bitmap bitmap = BitmapFactory.decodeFile(programDir + correctionImageUrl + correctionImageName, options);
					
					FileOutputStream b = null;
					try {
						b = new FileOutputStream(programDir + correctionImageUrl + correctionImageName);
						if (bitmap != null && b != null) {
							bitmap.compress(CompressFormat.JPEG, 30, b);
							ivThumb.setImageBitmap(bitmap);
							ivCamera.setVisibility(View.GONE);
							ivThumb.setVisibility(View.VISIBLE);
							ivDelete.setVisibility(View.VISIBLE);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}finally {
						try {
							b.flush();
							b.close();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (NullPointerException e2) {
							e2.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	}
	
}
