package com.tj.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baselibrary.common.CONST;
import com.baselibrary.manager.DBManager;
import com.baselibrary.utils.CommonUtil;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.warning.WarningDto;
import cxwl.shawn.tj.decision.R;

public class WarningDetailFragment extends Fragment{
	
	private ImageView imageView = null;//预警图标
	private TextView tvName = null;//预警名称
	private TextView tvTime = null;//预警时间
	private TextView tvIntro = null;//预警介绍
	private TextView tvGuide = null;//防御指南
	private String url = "http://decision.tianqi.cn/alarm12379/content2/";//详情页面url
	private WarningDto data = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.warning_detail_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		imageView = (ImageView) view.findViewById(R.id.imageView);
		tvName = (TextView) view.findViewById(R.id.tvName);
		tvTime = (TextView) view.findViewById(R.id.tvTime);
		tvIntro = (TextView) view.findViewById(R.id.tvIntro);
		tvGuide = (TextView) view.findViewById(R.id.tvGuide);
		
		data = getArguments().getParcelable("data");
		try {
			asyncQuery(url+data.html);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化数据库
	 */
	private void initDBManager() {
		String[] array = data.html.split("-");
		String item2 = array[2];
		String type = item2.substring(0, 5);
		String color = item2.substring(5, 7);
		
		DBManager dbManager = new DBManager(getActivity());
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + type+color + "\"",null);
		String content = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			content = cursor.getString(cursor.getColumnIndex("WarningGuide"));
		}
		if (content != null) {
			tvGuide.setText(getString(R.string.warning_guide)+content);
		}else {
			tvGuide.setVisibility(View.GONE);
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
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("sendTime")) {
							tvTime.setText(object.getString("sendTime"));
						}
						if (!object.isNull("description")) {
							tvIntro.setText(object.getString("description"));
						}
						tvName.setText(object.getString("headline"));
						
						Bitmap bitmap = null;
						if (object.getString("severityCode").equals(CONST.blue[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.blue[1]+CONST.imageSuffix);
							if (bitmap == null) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
							}
						}else if (object.getString("severityCode").equals(CONST.yellow[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix);
							if (bitmap == null) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
							}
						}else if (object.getString("severityCode").equals(CONST.orange[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.orange[1]+CONST.imageSuffix);
							if (bitmap == null) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
							}
						}else if (object.getString("severityCode").equals(CONST.red[0])) {
							bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.red[1]+CONST.imageSuffix);
							if (bitmap == null) {
								bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
							}
						}
						imageView.setImageBitmap(bitmap);
						
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

}
