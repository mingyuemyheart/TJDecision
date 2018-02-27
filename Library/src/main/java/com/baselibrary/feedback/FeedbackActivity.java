package com.baselibrary.feedback;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;

public class FeedbackActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private RelativeLayout reTitle = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvControl = null;
	private EditText etContent = null;
	private MyDialog mDialog = null;
	private String appid = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		mContext = this;
		initWidget();
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
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setText(getString(R.string.submit));
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setOnClickListener(this);
		etContent = (EditText) findViewById(R.id.etContent);
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		
		if (TextUtils.equals(CONST.STYLE, CONST.STYLE_ONE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title1);
		}else if (TextUtils.equals(CONST.STYLE, CONST.STYLE_TWO)) {
			reTitle.setBackgroundResource(R.drawable.bg_title2);
		}else if (TextUtils.equals(CONST.STYLE, CONST.STYLE_THREE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title3);
		}
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		appid = getIntent().getStringExtra(CONST.INTENT_APPID);
	}
	
	/**
	 * 异步请求
	 */
	private void asyncQuery(String requestUrl) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("uid", CONST.UID);
	        NameValuePair pair2 = new BasicNameValuePair("content", etContent.getText().toString());
	        NameValuePair pair3 = new BasicNameValuePair("appid", appid);
			nvpList.add(pair1);
			nvpList.add(pair2);
			nvpList.add(pair3);
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
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("status")) {
							int status  = object.getInt("status");
							if (status == 1) {//成功
								Toast.makeText(mContext, getString(R.string.submit_success), Toast.LENGTH_SHORT).show();
								finish();
							}else {
								//失败
								if (!object.isNull("msg")) {
									String msg = object.getString("msg");
									if (msg != null) {
										Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
									}
								}
							}
						}
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

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.tvControl) {
			if (TextUtils.isEmpty(etContent.getText().toString())) {
				return;
			}
			showDialog();
			if (appid.equals("15")) {
				asyncQuery(CONST.GUIZHOU_FEEDBACK);
			}else if (appid.equals("14")) {
				asyncQuery(CONST.TIANJIN_FEEDBACK);
			}else if (appid.equals("13")) {
				asyncQuery(CONST.XIZANG_FEEDBACK);
			}
		}
	}
}
