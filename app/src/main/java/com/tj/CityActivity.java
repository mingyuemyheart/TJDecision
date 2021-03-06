package com.tj;

/**
 * 城市选择
 */

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.com.weather.api.WeatherAPI;
import cn.com.weather.listener.AsyncResponseHandler;

import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.tj.adapter.CityAdapter;
import com.tj.adapter.CityFragmentAdapter;
import com.tj.dto.CityDto;
import cxwl.shawn.tj.decision.R;

public class CityActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private EditText etSearch = null;
	private TextView tvProvince = null;//省内热门
	private TextView tvNational = null;//全国热门
	private LinearLayout llGroup = null;
	private LinearLayout llGridView = null;
	private ProgressBar progressBar = null;
	
	//搜索城市后的结果列表
	private ListView mListView = null;
	private CityAdapter cityAdapter = null;
	private List<CityDto> cityList = new ArrayList<CityDto>();

	//省内热门
	private GridView pGridView = null;
	private CityFragmentAdapter pAdapter = null;
	private List<CityDto> pList = new ArrayList<CityDto>();
	
	//全国热门
	private GridView nGridView = null;
	private CityFragmentAdapter nAdapter = null;
	private List<CityDto> nList = new ArrayList<CityDto>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.city);
		mContext = this;
		initWidget();
		initListView();
		initPGridView();
		initNGridView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getIntent().getStringExtra(CONST.ACTIVITY_NAME));
		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		tvProvince = (TextView) findViewById(R.id.tvProvince);
		tvProvince.setOnClickListener(this);
		tvNational = (TextView) findViewById(R.id.tvNational);
		tvNational.setOnClickListener(this);
		llGroup = (LinearLayout) findViewById(R.id.llGroup);
		llGridView = (LinearLayout) findViewById(R.id.llGridView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
	}
	
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (arg0.toString() == null) {
				return;
			}

			progressBar.setVisibility(View.VISIBLE);
			cityList.clear();
			if (arg0.toString().trim().equals("")) {
				mListView.setVisibility(View.GONE);
				llGroup.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			}else {
				mListView.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.GONE);
				llGroup.setVisibility(View.GONE);
				getCityInfo(arg0.toString().trim());
			}

		}
	};
	
	/**
	 * 迁移到天气详情界面
	 */
	private void intentWeatherDetail(CityDto data) {
		Intent intent = new Intent(mContext, WeatherDetailActivity.class);
		intent.putExtra("data", data);
		startActivity(intent);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		cityAdapter = new CityAdapter(mContext, cityList);
		mListView.setAdapter(cityAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(cityList.get(arg2));
			}
		});
	}
	
	/**
	 * 获取天津热门城市
	 * @param context
	 * @return
	 */
	private List<CityDto> getTianJinHotCity(Context context) {
		final List<CityDto> pList = new ArrayList<CityDto>();
		String[] array = context.getResources().getStringArray(R.array.tianJin_hotCity);
		for (int i = 0; i < array.length; i++) {
			String[] data = array[i].split(",");
			CityDto dto = new CityDto();
			dto.lng = Double.valueOf(data[0]);
			dto.lat = Double.valueOf(data[1]);
			dto.cityId = data[2];
			dto.cityName = data[3];
			dto.spellName = data[4];
			pList.add(dto);
		}
		return pList;
	}
	
	/**
	 * 初始化省内热门gridview
	 */
	private void initPGridView() {
		pList.clear();
		pList.addAll(getTianJinHotCity(mContext));
		pGridView = (GridView) findViewById(R.id.pGridView);
		pAdapter = new CityFragmentAdapter(mContext, pList);
		pGridView.setAdapter(pAdapter);
		pGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(pList.get(arg2));
			}
		});
	}
	
	/**
	 * 获取全国热门城市
	 * @param context
	 * @return
	 */
	private List<CityDto> getNationHotCity(Context context) {
		final List<CityDto> nList = new ArrayList<CityDto>();
		String[] array = context.getResources().getStringArray(R.array.nation_hotCity);
		for (int i = 0; i < array.length; i++) {
			String[] data = array[i].split(",");
			CityDto dto = new CityDto();
			dto.lng = Double.valueOf(data[0]);
			dto.lat = Double.valueOf(data[1]);
			dto.cityId = data[2];
			dto.cityName = data[3];
			dto.spellName = data[4];
			nList.add(dto);
		}
		return nList;
	}
	
	/**
	 * 初始化全国热门
	 */
	private void initNGridView() {
		nList.clear();
		nList.addAll(getNationHotCity(mContext));
		nGridView = (GridView) findViewById(R.id.nGridView);
		nAdapter = new CityFragmentAdapter(mContext, nList);
		nGridView.setAdapter(nAdapter);
		nGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(nList.get(arg2));
			}
		});
	}
	
	/**
	 * 获取城市信息
	 */
	private void getCityInfo(String keyword) {
		WeatherAPI.searchCity(mContext, "", "", "", "", "", keyword, 0, 100, new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				progressBar.setVisibility(View.GONE);
				if (content != null) {
					try {
						JSONObject obj = new JSONObject(content.toString());
						if (!obj.isNull("data")) {
							JSONObject dataObj = obj.getJSONObject("data");
							if (!dataObj.isNull("records")) {
								JSONArray array = dataObj.getJSONArray("records");
								for (int i = 0; i < array.length(); i++) {
									JSONObject itemObj = array.getJSONObject(i);
									CityDto dto = new CityDto();
									if (!itemObj.isNull("areaId")) {
										dto.cityId = itemObj.getString("areaId");
									}
									if (!itemObj.isNull("nameZh")) {
										dto.cityName = itemObj.getString("nameZh");
									}
									if (!itemObj.isNull("nameEn")) {
										dto.spellName = itemObj.getString("nameEn");
									}
									if (!itemObj.isNull("provZh")) {
										dto.provinceName = itemObj.getString("provZh");
									}
									if (!itemObj.isNull("longitude")) {
										dto.lng = itemObj.getDouble("longitude");
									}
									if (!itemObj.isNull("latitude")) {
										dto.lat = itemObj.getDouble("latitude");
									}
									cityList.add(dto);
								}
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
			
			@Override
			public void onError(Throwable error, String content) {
				// TODO Auto-generated method stub
				super.onError(error, content);
				progressBar.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.tvProvince) {
			tvProvince.setTextColor(getResources().getColor(R.color.white));
			tvNational.setTextColor(getResources().getColor(R.color.title_bg));
			tvProvince.setBackgroundResource(R.drawable.corner_left_blue);
			tvNational.setBackgroundResource(R.drawable.corner_right_white);
			pGridView.setVisibility(View.VISIBLE);
			nGridView.setVisibility(View.GONE);
		}else if (v.getId() == R.id.tvNational) {
			tvProvince.setTextColor(getResources().getColor(R.color.title_bg));
			tvNational.setTextColor(getResources().getColor(R.color.white));
			tvProvince.setBackgroundResource(R.drawable.corner_left_white);
			tvNational.setBackgroundResource(R.drawable.corner_right_blue);
			pGridView.setVisibility(View.GONE);
			nGridView.setVisibility(View.VISIBLE);
		}
	}
}
