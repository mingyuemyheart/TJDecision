package com.baselibrary.index;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.baselibrary.manager.DBManager;
import com.baselibrary.view.MyDialog;

/**
 * 生活指数
 * @author shawn_sun
 *
 */

public class IndexActivity extends BaseActivity implements OnClickListener, AMapLocationListener{

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private IndexAdapter mAdapter = null;
	private List<IndexDto> mList = new ArrayList<IndexDto>();
	private MyDialog mDialog = null;
	private String[] indexs = {"fs", "ac", "pp", "pl", "ct", "tr", "gm", "ls", "lk",
			"cl", "xq", "uv", "pj", "nl", "jt", "zs", "hc", "xc", "mf", "co", "gj",
			"dy", "yd", "ag", "ys", "pk", "yh" };//所有生活指数缩写
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		mContext = this;
		showDialog();
		initWidget();
		initListView();
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
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		if (getIntent().hasExtra(CONST.ACTIVITY_NAME)) {
			tvTitle.setText(getIntent().getStringExtra(CONST.ACTIVITY_NAME));
		}
		
		startLocation();
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new IndexAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				IndexDto dto = mList.get(arg2);
				if (dto.isExpand == false) {
					dto.isExpand = true;
				}else {
					dto.isExpand = false;
				}
				mAdapter.notifyDataSetChanged();
			}
		});
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
	 * 初始化数据库
	 */
	private void initDBManager(String abbr, String number, IndexDto dto) {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		
		Cursor cursor = database.rawQuery("select wi.abbr,wi.number,wil.level_zh ,win.name ,wii.intro_zh "
				+"from weather_index as wi ,weather_index_level as wil ,weather_index_name as win ,weather_index_introduction as wii "
				+"where wi.abbr = "+"'"+abbr+"'" + " and wi.number = "+"'"+number+"'"
				+" and wil.level=wi.level and win.abbr=wi.abbr and wii.intro = wi.intro", null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			dto.name = cursor.getString(cursor.getColumnIndex("name"))+"：";
			dto.abbr = cursor.getString(cursor.getColumnIndex("abbr"));
			dto.level_zh = cursor.getString(cursor.getColumnIndex("level_zh"));
			dto.intro_zh = cursor.getString(cursor.getColumnIndex("intro_zh"));
			dto.number = cursor.getString(cursor.getColumnIndex("number"));
			dto.isExpand = false;
			
			if (!TextUtils.isEmpty(dto.name)) {
				mList.add(dto);
			}
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
											//生活指数
											JSONArray array = content.getIndexInfo(mContext, indexs, Language.ZH_CN);
											for (int i = 0; i < array.length(); i++) {
												IndexDto dto = new IndexDto();
												try {
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("i1")) {
														dto.abbr = itemObj.getString("i1");
													}
													JSONArray itemArray = itemObj.getJSONArray("i4");
													JSONObject itemArrayObj = itemArray.getJSONObject(0);
													if (!itemArrayObj.isNull("ia")) {
														dto.number = itemArrayObj.getString("ia");
													}
													
													initDBManager(dto.abbr, dto.number, dto);
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}
											
											if (mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}
											cancelDialog();
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
				cancelDialog();
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
