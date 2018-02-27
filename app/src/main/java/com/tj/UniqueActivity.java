package com.tj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.cloud.CloudActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.common.ColumnData;
import com.baselibrary.index.IndexActivity;
import com.baselibrary.minute.MinuteFallActivity;
import com.baselibrary.radar.RadarActivity;
import com.baselibrary.report.ReportWeatherActivity;
import com.baselibrary.routeweather.RouteWeatherActivity;
import com.baselibrary.typhoon.TyphoonRouteActivity;
import com.baselibrary.video.FactWeatherActivity;
import com.tj.adapter.UniqueAdapter;
import cxwl.shawn.tj.decision.R;

public class UniqueActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private GridView mGridView = null;
	private UniqueAdapter mAdapter = null;
	private List<ColumnData> mList = new ArrayList<ColumnData>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unique);
		mContext = this;
		initWidget();
		initGridView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getIntent().getStringExtra(CONST.ACTIVITY_NAME));
	}
	
	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		List<ColumnData> tempList = new ArrayList<ColumnData>();
		tempList.addAll(getIntent().getExtras().<ColumnData>getParcelableArrayList("dataList"));
		for (int i = 5; i < tempList.size(); i++) {
			mList.add(tempList.get(i));
		}
		mGridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new UniqueAdapter(mContext, mList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = mList.get(arg2);
				Intent intent = null;
				if (dto.id.equals("21")) {
					intent = new Intent(mContext, MinuteFallActivity.class);
				}else if (dto.id.equals("22")) {
					intent = new Intent(mContext, IndexActivity.class);
				}else if (dto.id.equals("23")) {
					intent = new Intent(mContext, QuestionActivity.class);
					intent.putExtra(CONST.WEB_URL, com.tj.common.CONST.WAIT_WIND);
				}else if (dto.id.equals("24")) {
					intent = new Intent(mContext, FactWeatherActivity.class);
				}else if (dto.id.equals("25")) {
					intent = new Intent(mContext, ReportWeatherActivity.class);
				}else if (dto.id.equals("26")) {
					intent = new Intent(mContext, TyphoonRouteActivity.class);
				}else if (dto.id.equals("27")) {
					intent = new Intent(mContext, RouteWeatherActivity.class);
				}else if (dto.id.equals("28")) {
					intent = new Intent(mContext, RadarActivity.class);
				}else if (dto.id.equals("29")) {
					intent = new Intent(mContext, CloudActivity.class);
				}
				
				if (intent != null) {
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.LATITUDE, com.tj.common.CONST.tianJin_LATITUDE);
					intent.putExtra(CONST.LONGITUDE, com.tj.common.CONST.tianJin_LONGITUDE);
					String[] values = getResources().getStringArray(R.array.tianjin_radars);
					intent.putExtra(CONST.RADAR_NAME_ARRAY, values);
					startActivity(intent);
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
