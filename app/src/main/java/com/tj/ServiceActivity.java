package com.tj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.common.ColumnData;
import com.baselibrary.index.IndexActivity;
import com.baselibrary.news.NewsActivity;
import com.baselibrary.news.ProductActivity;
import com.tj.adapter.UniqueAdapter;
import cxwl.shawn.tj.decision.R;

public class ServiceActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private GridView mGridView = null;
	private UniqueAdapter mAdapter = null;
	private List<ColumnData> mList = new ArrayList<ColumnData>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.service);
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
				String showType = dto.showType;
				String id = dto.id;
				Intent intent = null;
				if (TextUtils.equals(showType, CONST.LOCAL)) {
					if (TextUtils.equals(id, "12")) {
						intent = new Intent(mContext, DistrictForecastActivity.class);
					}else if (TextUtils.equals(id, "22")) {
						intent = new Intent(mContext, IndexActivity.class);
					}else if (TextUtils.equals(id, "13")) {
						intent = new Intent(mContext, QuickPaperActivity.class);
					}else if (TextUtils.equals(id, "65")) {
						intent = new Intent(mContext, FactCheckActivity.class);
					}
				}else if (TextUtils.equals(showType, CONST.NEWS)) {
					intent = new Intent(mContext, NewsActivity.class);
				}else if (TextUtils.equals(showType, CONST.PRODUCT)) {
					intent = new Intent(mContext, ProductActivity.class);
				}else if (TextUtils.equals(showType, CONST.URL)) {
					intent = new Intent(mContext, QuestionActivity.class);
				}
				
				if (intent != null) {
					intent.putExtra("data", dto);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.INTENT_APPID, com.tj.common.CONST.APPID);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
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
