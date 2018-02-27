package com.tj;

/**
 * 决策快报
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.common.ColumnData;
import com.baselibrary.news.NewsActivity;
import com.tj.adapter.QuickPaperAdapter;
import cxwl.shawn.tj.decision.R;

public class QuickPaperActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private QuickPaperAdapter mAdapter = null;
	private List<ColumnData> mList = new ArrayList<ColumnData>();
	private ColumnData data = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_paper);
		mContext = this;
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		if (getIntent().hasExtra(CONST.ACTIVITY_NAME)) {
			tvTitle.setText(getIntent().getStringExtra(CONST.ACTIVITY_NAME));
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			mList.addAll(data.child);
		}
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new QuickPaperAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = mList.get(arg2);
				Intent intent = new Intent(mContext, NewsActivity.class);
				intent.putExtra("data", dto);
				intent.putExtra(CONST.WEB_URL, dto.dataUrl);
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
				intent.putExtra(CONST.INTENT_APPID, com.tj.common.CONST.APPID);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
}
