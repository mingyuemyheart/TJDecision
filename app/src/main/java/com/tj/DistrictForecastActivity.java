package com.tj;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.display.Displayer;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.news.NewsDto;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;
import com.tj.adapter.DistrictForecastAdapter;
import cxwl.shawn.tj.decision.R;

public class DistrictForecastActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private TextView tvTitle = null;
	private LinearLayout llBack = null;
	private ImageView imageView = null;
	private GridView gridView = null;
	private DistrictForecastAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<NewsDto>();
	private MyDialog mDialog = null;
	private ProgressBar progressBar = null;
	private TextView tvShuanggang = null;
	private TextView tvXinzhuang = null;
	private TextView tvXianshuigu = null;
	private TextView tvShuangqiaohe = null;
	private TextView tvGegu = null;
	private TextView tvBeizhakou = null;
	private TextView tvXiaozhan = null;
	private TextView tvShuangzha = null;
	private TextView tvBalitai = null;
	private TextView tvNongyeyuan = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.district_forecast);
		mContext = this;
		showDialog();
		initWidget();
//		initGridView();
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
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		imageView = (ImageView) findViewById(R.id.imageView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		tvShuanggang = (TextView) findViewById(R.id.tvShuanggang);
		tvShuanggang.setOnClickListener(this);
		tvXinzhuang = (TextView) findViewById(R.id.tvXinzhuang);
		tvXinzhuang.setOnClickListener(this);
		tvXianshuigu = (TextView) findViewById(R.id.tvXianshuigu);
		tvXianshuigu.setOnClickListener(this);
		tvShuangqiaohe = (TextView) findViewById(R.id.tvShuangqiaohe);
		tvShuangqiaohe.setOnClickListener(this);
		tvGegu = (TextView) findViewById(R.id.tvGegu);
		tvGegu.setOnClickListener(this);
		tvBeizhakou = (TextView) findViewById(R.id.tvBeizhakou);
		tvBeizhakou.setOnClickListener(this);
		tvXiaozhan = (TextView) findViewById(R.id.tvXiaozhan);
		tvXiaozhan.setOnClickListener(this);
		tvShuangzha = (TextView) findViewById(R.id.tvShuangzha);
		tvShuangzha.setOnClickListener(this);
		tvBalitai = (TextView) findViewById(R.id.tvBalitai);
		tvBalitai.setOnClickListener(this);
		tvNongyeyuan = (TextView) findViewById(R.id.tvNongyeyuan);
		tvNongyeyuan.setOnClickListener(this);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			asyncQuery(url);
		}
	}
	
	private void initGridView() {
		gridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new DistrictForecastAdapter(mContext, mList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				progressBar.setVisibility(View.VISIBLE);
				for (int i = 0; i < mList.size(); i++) {
					if (i == arg2) {
						mList.get(i).isSelected = true;
					}else {
						mList.get(i).isSelected = false;
					}
				}
				
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				
				NewsDto dto = mList.get(arg2);
				if (!TextUtils.isEmpty(dto.imgUrl)) {
					FinalBitmap finalBitmap = FinalBitmap.create(mContext);
					finalBitmap.display(imageView, dto.imgUrl, null, 0);
					finalBitmap.configDisplayer(new Displayer() {
						@Override
						public void loadFailDisplay(View image, Bitmap bitmap) {
							progressBar.setVisibility(View.GONE);
						}
						
						@Override
						public void loadCompletedisplay(View image, Bitmap bitmap, BitmapDisplayConfig config) {
							progressBar.setVisibility(View.GONE);
							if (bitmap != null) {
								imageView.setImageBitmap(bitmap);
							}
						}
					});
				}
			}
		});
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
			if (requestResult != null) {
				try {
					mList.clear();
					JSONArray array = new JSONArray(requestResult);
					for (int i = 0; i < array.length(); i++) {
						JSONObject itemObj = array.getJSONObject(i);
						NewsDto dto = new NewsDto();
						dto.id = i+"";
						dto.imgUrl = itemObj.getString("url");
						dto.title = itemObj.getString("name");
						if (i == 0) {
							dto.isSelected = true;
						}else {
							dto.isSelected = false;
						}
						mList.add(dto);
					}
					
					if (mList.size() > 0) {
						if (!TextUtils.isEmpty(mList.get(0).imgUrl)) {
							if (progressBar.getVisibility() == View.VISIBLE) {
								progressBar.setVisibility(View.GONE);
							}
							progressBar.setVisibility(View.VISIBLE);
							FinalBitmap finalBitmap = FinalBitmap.create(mContext);
							finalBitmap.display(imageView, mList.get(0).imgUrl, null, 0);
							finalBitmap.configDisplayer(new Displayer() {
								@Override
								public void loadFailDisplay(View image, Bitmap bitmap) {
									progressBar.setVisibility(View.GONE);
								}
								
								@Override
								public void loadCompletedisplay(View image, Bitmap bitmap, BitmapDisplayConfig config) {
									progressBar.setVisibility(View.GONE);
									if (bitmap != null) {
										imageView.setImageBitmap(bitmap);
									}
								}
							});
						}
					}
					
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
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
	
	private void click(String id) {
		for (int i = 0; i < mList.size(); i++) {
			NewsDto dto = mList.get(i);
			if (TextUtils.equals(id, dto.id)) {
				if (!TextUtils.isEmpty(dto.imgUrl)) {
					if (progressBar.getVisibility() == View.VISIBLE) {
						progressBar.setVisibility(View.GONE);
					}
					progressBar.setVisibility(View.VISIBLE);
					FinalBitmap finalBitmap = FinalBitmap.create(mContext);
					finalBitmap.display(imageView, dto.imgUrl, null, 0);
					finalBitmap.configDisplayer(new Displayer() {
						@Override
						public void loadFailDisplay(View image, Bitmap bitmap) {
							progressBar.setVisibility(View.GONE);
						}
						
						@Override
						public void loadCompletedisplay(View image, Bitmap bitmap, BitmapDisplayConfig config) {
							progressBar.setVisibility(View.GONE);
							if (bitmap != null) {
								imageView.setImageBitmap(bitmap);
							}
						}
					});
				}
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvGegu:
			click("0");
			tvGegu.setBackgroundResource(R.drawable.iv_district_press);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvXianshuigu:
			click("1");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district_press);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvXinzhuang:
			click("2");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district_press);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvBalitai:
			click("3");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district_press);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvShuangqiaohe:
			click("4");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district_press);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvShuanggang:
			click("5");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district_press);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvXiaozhan:
			click("6");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district_press);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvNongyeyuan:
			click("7");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district_press);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvShuangzha:
			click("8");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district_press);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district);
			break;
		case R.id.tvBeizhakou:
			click("9");
			tvGegu.setBackgroundResource(R.drawable.iv_district);
			tvXianshuigu.setBackgroundResource(R.drawable.iv_district);
			tvXinzhuang.setBackgroundResource(R.drawable.iv_district);
			tvBalitai.setBackgroundResource(R.drawable.iv_district);
			tvShuangqiaohe.setBackgroundResource(R.drawable.iv_district);
			tvShuanggang.setBackgroundResource(R.drawable.iv_district);
			tvXiaozhan.setBackgroundResource(R.drawable.iv_district);
			tvNongyeyuan.setBackgroundResource(R.drawable.iv_district);
			tvShuangzha.setBackgroundResource(R.drawable.iv_district);
			tvBeizhakou.setBackgroundResource(R.drawable.iv_district_press);
			break;

		default:
			break;
		}
	}
	
}
