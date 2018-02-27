package com.baselibrary.warning;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.manager.DBManager;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;

public class WarningDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private RelativeLayout reTitle = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView imageView = null;//预警图标
	private TextView tvName = null;//预警名称
	private TextView tvTime = null;//预警时间
	private TextView tvIntro = null;//预警介绍
	private TextView tvGuide = null;//防御指南
	private String url = "http://decision.tianqi.cn/alarm12379/content2/";//详情页面url
	private WarningDto data = null;
	private MyDialog mDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warning_detail);
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
		imageView = (ImageView) findViewById(R.id.imageView);
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvIntro = (TextView) findViewById(R.id.tvIntro);
		tvGuide = (TextView) findViewById(R.id.tvGuide);
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		
		if (TextUtils.equals(com.baselibrary.common.CONST.STYLE, com.baselibrary.common.CONST.STYLE_ONE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title1);
		}else if (TextUtils.equals(com.baselibrary.common.CONST.STYLE, com.baselibrary.common.CONST.STYLE_TWO)) {
			reTitle.setBackgroundResource(R.drawable.bg_title2);
		}else if (TextUtils.equals(com.baselibrary.common.CONST.STYLE, com.baselibrary.common.CONST.STYLE_THREE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title3);
		}
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			try {
				asyncQuery(url+data.html);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 初始化数据库
	 */
	private void initDBManager() {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from WarningInfo where WarningId = " + "\"" + data.type+data.color + "\"",null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			tvGuide.setText(getString(R.string.warning_guide)+cursor.getString(cursor.getColumnIndex("WarningGuide")));
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
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						tvTitle.setText(getString(R.string.warning_detail));
						if (!object.isNull("sendTime")) {
							tvTime.setText(object.getString("sendTime"));
						}
						if (!object.isNull("description")) {
							tvIntro.setText(object.getString("description"));
						}
						String name = object.getString("headline");
						if (!TextUtils.isEmpty(name)) {
							tvName.setText(name.replace(getString(R.string.publish), getString(R.string.publish)+"\n"));
						}
						
						Bitmap bitmap = null;
						if (object.getString("severityCode").equals(CONST.blue[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(mContext,object.getString("eventType")+CONST.blue[1]+CONST.imageSuffix);
							if (bitmap != null) {
								imageView.setImageBitmap(bitmap);
							}else {
								imageView.setImageResource(R.drawable.default_blue);
							}
						}else if (object.getString("severityCode").equals(CONST.yellow[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(mContext,object.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix);
							if (bitmap != null) {
								imageView.setImageBitmap(bitmap);
							}else {
								imageView.setImageResource(R.drawable.default_yellow);
							}
						}else if (object.getString("severityCode").equals(CONST.orange[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(mContext,object.getString("eventType")+CONST.orange[1]+CONST.imageSuffix);
							if (bitmap != null) {
								imageView.setImageBitmap(bitmap);
							}else {
								imageView.setImageResource(R.drawable.default_orange);
							}
						}else if (object.getString("severityCode").equals(CONST.red[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(mContext,object.getString("eventType")+CONST.red[1]+CONST.imageSuffix);
							if (bitmap != null) {
								imageView.setImageBitmap(bitmap);
							}else {
								imageView.setImageResource(R.drawable.default_red);
							}
						}
						
						initDBManager();
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
		}
	}

}
