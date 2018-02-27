package com.baselibrary.routeweather;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.utils.CommonUtil;

public class RouteSearchActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView imageView = null;
	private TextView textView = null;
	private EditText editText = null;
	private ListView mListView = null;
	private RouteSearchAdapter mAdapter = null;
	private List<PoiItem> poiList = new ArrayList<PoiItem>();//搜索地点的列表
	private int count = 0;//计数器，计算获取地点列表次数，这里只获取2次，当count==2时，就执行搜索
	private ProgressBar progressBar = null;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.route_search);
		mContext = this;
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		imageView = (ImageView) findViewById(R.id.imageView);
		textView = (TextView) findViewById(R.id.textView);
		editText = (EditText) findViewById(R.id.editText);
		editText.addTextChangedListener(wathcer);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		if (getIntent().hasExtra("startOrEnd")) {
			String startOrEnd = getIntent().getStringExtra("startOrEnd");
			if (startOrEnd.equals("start")) {
				tvTitle.setText(getString(R.string.select_start_point));
				imageView.setImageResource(R.drawable.route_start);
				textView.setText(getString(R.string.route_start_point));
				editText.setHint(getString(R.string.select_start_point));
			}else if (startOrEnd.equals("end")) {
				tvTitle.setText(getString(R.string.select_end_point));
				imageView.setImageResource(R.drawable.route_end);
				textView.setText(getString(R.string.route_end_point));
				editText.setHint(getString(R.string.select_end_point));
			}
		}
		
	}
	
	private TextWatcher wathcer = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (!TextUtils.isEmpty(arg0.toString().trim())) {
				progressBar.setVisibility(View.VISIBLE);
				searchPoints(arg0.toString().trim());
			}else {
				poiList.clear();
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	
	/**
	 * 根据关键字查找相关列表数据
	 * @param keyWord
	 */
	private void searchPoints(String keyWord) {
		// 第一个参数表示查询关键字，第二参数表示poi搜索类型，第三个参数表示城市区号或者城市名
		PoiSearch.Query startSearchQuery = new PoiSearch.Query(keyWord, "", ""); 
		startSearchQuery.setPageNum(0);// 设置查询第几页，第一页从0开始
		startSearchQuery.setPageSize(20);// 设置每页返回多少条数据
		PoiSearch poiSearch = new PoiSearch(mContext, startSearchQuery);
		poiSearch.searchPOIAsyn();// 异步poi查询
		poiSearch.setOnPoiSearchListener(new OnPoiSearchListener() {
			@Override
			public void onPoiSearched(PoiResult result, int rCode) {
				progressBar.setVisibility(View.GONE);
				if (rCode == 0) {// 返回成功
					if (result != null && result.getQuery() != null && result.getPois() != null && result.getPois().size() > 0) {// 搜索poi的结果
						count++;
						poiList.clear();
						poiList.addAll(result.getPois());
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				}else {
					Toast.makeText(mContext, getString(R.string.no_result), Toast.LENGTH_SHORT).show();
				}
			}
			@Override
			public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
			}
		});
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new RouteSearchAdapter(mContext, poiList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PoiItem dto = poiList.get(arg2);
				if (dto.getTitle() != null) {
					editText.setText(dto.getTitle());
					editText.setSelection(dto.getTitle().length());
					if (count >= 2) {
						count = 0;
						CommonUtil.hideInputSoft(editText, mContext);
						
						Intent intent = new Intent();
						intent.putExtra("cityName", dto.getTitle());
						intent.putExtra("lat", dto.getLatLonPoint().getLatitude());
						intent.putExtra("lng", dto.getLatLonPoint().getLongitude());
						setResult(RESULT_OK, intent);
						finish();
					}else {
						searchPoints(dto.getTitle());
					}
				}
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
