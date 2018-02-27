package com.tj;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;
import com.tj.dto.FactCheckDto;
import com.tj.view.HumidityView;
import com.tj.view.PressureView;
import com.tj.view.RainFallView;
import com.tj.view.TemperatureView;
import com.tj.view.WindSpeedView;
import cxwl.shawn.tj.decision.R;

public class FactCheckDetailActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private MyDialog mDialog = null;
	private LinearLayout llContainer1 = null;
	private LinearLayout llContainer2 = null;
	private LinearLayout llContainer3 = null;
	private LinearLayout llContainer4 = null;
	private LinearLayout llContainer5 = null;
	private int width = 0;
	private float density = 0;
	private LinearLayout llMain = null;
	private FactCheckDto data = null;
	private List<FactCheckDto> mList = new ArrayList<FactCheckDto>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHH");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	private TextView tvException1 = null;
	private TextView tvException2 = null;
	private TextView tvException3 = null;
	private TextView tvException4 = null;
	private TextView tvException5 = null;
	private HorizontalScrollView hScroll1 = null;
	private HorizontalScrollView hScroll2 = null;
	private HorizontalScrollView hScroll3 = null;
	private HorizontalScrollView hScroll4 = null;
	private HorizontalScrollView hScroll5 = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fact_check_detail);
		mContext = this;
		showDialog();
		initWidget();
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
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		llContainer2 = (LinearLayout) findViewById(R.id.llContainer2);
		llContainer3 = (LinearLayout) findViewById(R.id.llContainer3);
		llContainer4 = (LinearLayout) findViewById(R.id.llContainer4);
		llContainer5 = (LinearLayout) findViewById(R.id.llContainer5);
		llMain = (LinearLayout) findViewById(R.id.llMain);
		tvException1 = (TextView) findViewById(R.id.tvException1);
		tvException2 = (TextView) findViewById(R.id.tvException2);
		tvException3 = (TextView) findViewById(R.id.tvException3);
		tvException4 = (TextView) findViewById(R.id.tvException4);
		tvException5 = (TextView) findViewById(R.id.tvException5);
		hScroll1 = (HorizontalScrollView) findViewById(R.id.hScroll1);
		hScroll2 = (HorizontalScrollView) findViewById(R.id.hScroll2);
		hScroll3 = (HorizontalScrollView) findViewById(R.id.hScroll3);
		hScroll4 = (HorizontalScrollView) findViewById(R.id.hScroll4);
		hScroll5 = (HorizontalScrollView) findViewById(R.id.hScroll5);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				if (!TextUtils.isEmpty(data.stationName)) {
					tvTitle.setText(data.stationName + "(" + data.stationId + ")");
				}
				if (!TextUtils.isEmpty(data.stationId)) {
					aysnTask(data.stationId);
				}
			}
		}
		
	}
	
	private void aysnTask(String stationId) {
		String url = "http://decision-admin.tianqi.cn/Home/Work/jnInfomes?sid="+stationId;
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
						JSONArray itemArray = array.getJSONArray(i);
						FactCheckDto dto = new FactCheckDto();
						try {
							dto.time = sdf2.format(sdf1.parse(itemArray.getString(1)));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						dto.stationId = itemArray.getString(2);
						dto.stationName = itemArray.getString(3);
						if (!TextUtils.isEmpty(itemArray.getString(4))) {
							dto.temp = Float.valueOf(itemArray.getString(4));
						}
						if (!TextUtils.isEmpty(itemArray.getString(5))) {
							dto.windDir = Integer.valueOf(itemArray.getString(5));
						}
						if (!TextUtils.isEmpty(itemArray.getString(7))) {
							dto.windSpeed = Float.valueOf(itemArray.getString(7));
						}
						if (!TextUtils.isEmpty(itemArray.getString(8))) {
							dto.rainFall = Float.valueOf(itemArray.getString(8));
						}
						if (!TextUtils.isEmpty(itemArray.getString(9))) {
							dto.humidity = Integer.valueOf(itemArray.getString(9));
						}
						if (!TextUtils.isEmpty(itemArray.getString(10))) {
							dto.pressure = Float.valueOf(itemArray.getString(10));
						}
//						if (!TextUtils.equals(dto.stationId, "999999") && !TextUtils.equals(dto.stationName, "999999")) {
							mList.add(dto);
//						}
					}
					
//					for (int i = 0; i < mList.size(); i++) {
//						FactCheckDto data = mList.get(i);
//						if (TextUtils.equals(data.stationId, "999999") && data.temp >= 999999) {
//							tvException1.setVisibility(View.VISIBLE);
//							hScroll1.setVisibility(View.GONE);
//						}
//						if (TextUtils.equals(data.stationId, "999999") && data.windSpeed >= 999999) {
//							tvException2.setVisibility(View.VISIBLE);
//							hScroll2.setVisibility(View.GONE);
//						}
//						if (TextUtils.equals(data.stationId, "999999") && data.rainFall >= 999999) {
//							tvException3.setVisibility(View.VISIBLE);
//							hScroll3.setVisibility(View.GONE);
//						}
//						if (TextUtils.equals(data.stationId, "999999") && data.humidity >= 999999) {
//							tvException4.setVisibility(View.VISIBLE);
//							hScroll4.setVisibility(View.GONE);
//						}
//						if (TextUtils.equals(data.stationId, "999999") && data.pressure >= 999999) {
//							tvException5.setVisibility(View.VISIBLE);
//							hScroll5.setVisibility(View.GONE);
//						}
//					}
					
					TemperatureView temperatureView = new TemperatureView(mContext);
					temperatureView.setData(mList);
					llContainer1.addView(temperatureView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							hScroll1.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
						}
					});
					
					WindSpeedView windSpeedView = new WindSpeedView(mContext);
					windSpeedView.setData(mList);
					llContainer2.addView(windSpeedView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							hScroll2.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
						}
					});
					
					RainFallView rainFallView = new RainFallView(mContext);
					rainFallView.setData(mList);
					llContainer3.addView(rainFallView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							hScroll3.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
						}
					});
						
					HumidityView humidityView = new HumidityView(mContext);
					humidityView.setData(mList);
					llContainer4.addView(humidityView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							hScroll4.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
						}
					});
					
					PressureView pressureView = new PressureView(mContext);
					pressureView.setData(mList);
					llContainer5.addView(pressureView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							hScroll5.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
						}
					});
					
					llMain.setVisibility(View.VISIBLE);
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
