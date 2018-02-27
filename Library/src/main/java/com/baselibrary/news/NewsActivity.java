package com.baselibrary.news;

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
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.smartwebview.PDFActivity;
import com.baselibrary.smartwebview.WebViewActivity;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;
import com.baselibrary.view.RefreshLayout;
import com.baselibrary.view.RefreshLayout.OnLoadListener;
import com.baselibrary.view.RefreshLayout.OnRefreshListener;

public class NewsActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnLoadListener{
	
	private Context mContext = null;
	private RelativeLayout reTitle = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private NewsFragmentAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<NewsDto>();
	private int countpage = 0;//总页数
	private int page = 1;
	private int pageSize = 20;
	private MyDialog mDialog = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String appid = null;
	private String url = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news);
		mContext = this;
		showDialog();
		initRefreshLayout();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化dialog
	 */
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
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.baselibrary.common.CONST.color1, com.baselibrary.common.CONST.color2, com.baselibrary.common.CONST.color3, com.baselibrary.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.BOTH);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(this);
		refreshLayout.setOnLoadListener(this);
	}
	
	@Override
	public void onRefresh() {
		page = 1;
		pageSize = 20;
		mList.clear();
		operate();
	}
	
	@Override
	public void onLoad() {
		if (page >= countpage) {
			refreshLayout.setLoading(false);
			return;
		}else {
			if (!TextUtils.isEmpty(url)) {
				page += 1;
				
				String url2 = null;
				if (url.contains("pagesize")) {
					if (TextUtils.equals(appid, "15")) {
						url2 = CONST.GUIZHOU_BASE+"/Work/getnewslist/p/"+page+"/pagesize/"+pageSize+"/type/";
					}else if (TextUtils.equals(appid, "14")) {
						url2 = CONST.TIANJIN_BASE+"/Work/getnewslist/p/"+page+"/pagesize/"+pageSize+"/type/";
					}else if (TextUtils.equals(appid, "13")) {
						url2 = CONST.XIZANG_BASE+"/Work/getnewslist/p/"+page+"/pagesize/"+pageSize+"/type/";
					}
					
					String[] urls = url.split("/");
					asyncQuery(url2 + urls[urls.length-1]);
				}
			}
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
		
		appid = getIntent().getStringExtra(CONST.INTENT_APPID);
		
		operate();
	}
	
	private void operate() {
		url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			asyncQuery(url);
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new NewsFragmentAdapter(mContext, mList, getIntent().getStringExtra(CONST.INTENT_APPID));
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NewsDto dto = mList.get(arg2);
				postCount("http://decision-admin.tianqi.cn/Home/Work/shownews?id="+dto.id);
				Intent intent = null;
				if (TextUtils.equals(dto.showType, CONST.URL)) {//url
					intent = new Intent(mContext, WebViewActivity.class);
				}else if (TextUtils.equals(dto.showType, CONST.PDF)) {//pdf
					intent = new Intent(mContext, PDFActivity.class);
				}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {//news
					intent = new Intent(mContext, NewsActivity.class);
				}else if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {//product
					intent = new Intent(mContext, ProductActivity.class);
				}
				if (intent != null) {
					intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
					intent.putExtra(CONST.WEB_URL, dto.detailUrl);
					intent.putExtra(CONST.INTENT_APPID, appid);
					intent.putExtra(CONST.INTENT_IMGURL, dto.imgUrl);
					startActivity(intent);
				}
			}
		});
	}
	
	/**
	 * post阅读量
	 * @param url
	 */
	private void postCount(String url) {
		HttpAsyncTaskPost task = new HttpAsyncTaskPost();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskPost extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskPost() {
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

	private void asyncQuery(String url) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
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
			refreshLayout.setRefreshing(false);
			refreshLayout.setLoading(false);
			if (requestResult != null) {
				try {
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("count")) {
						String num = obj.getString("countpage");
						if (!TextUtils.isEmpty(num)) {
							countpage = Integer.valueOf(obj.getString("countpage"));
						}
					}
					if (!obj.isNull("info")) {
						JSONArray array = new JSONArray(obj.getString("info"));
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							NewsDto dto = new NewsDto();
							dto.id = itemObj.getString("id");
							dto.imgUrl = itemObj.getString("icon");
							dto.title = itemObj.getString("name");
							dto.time = itemObj.getString("addtime");
							dto.detailUrl = itemObj.getString("urladdress");
							dto.showType = itemObj.getString("showtype");
							mList.add(dto);
						}
						
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
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
