package com.tj;

/**
 * 热点新闻
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;
import com.tj.adapter.FactCheckAdapter;
import com.tj.dto.FactCheckDto;
import cxwl.shawn.tj.decision.R;

public class FactCheckActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private RelativeLayout reTitle = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private FactCheckAdapter mAdapter = null;
	private List<FactCheckDto> mList = new ArrayList<FactCheckDto>();
	private MyDialog mDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fact_check);
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
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getIntent().getStringExtra(CONST.ACTIVITY_NAME));
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		
		if (TextUtils.equals(CONST.STYLE, CONST.STYLE_ONE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title1);
		}else if (TextUtils.equals(CONST.STYLE, CONST.STYLE_TWO)) {
			reTitle.setBackgroundResource(R.drawable.bg_title2);
		}else if (TextUtils.equals(CONST.STYLE, CONST.STYLE_THREE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title3);
		}
		
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			asyncQuery(url);
		}
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
			cancelDialog();
			if (requestResult != null) {
				try {
					mList.clear();
					JSONArray array = new JSONArray(requestResult);
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						FactCheckDto dto = new FactCheckDto();
						if (!obj.isNull("name")) {
							dto.stationName = obj.getString("name");
						}
						if (!obj.isNull("id")) {
							dto.stationId = obj.getString("id");
						}
						mList.add(dto);
					}
					
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
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
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new FactCheckAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FactCheckDto dto = mList.get(arg2);
				Intent intent = new Intent(mContext, FactCheckDetailActivity.class);
				intent.putExtra("data", dto);
				startActivity(intent);
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
