package com.tj;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.common.ColumnData;
import com.baselibrary.utils.CustomHttpClient;
import com.baselibrary.view.MyDialog;
import cxwl.shawn.tj.decision.R;

public class LoginActivity extends BaseActivity implements OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private EditText etUserName = null;//用户名
	private EditText etPwd = null;//密码
	private LinearLayout llLogin = null;//登陆
	private TextView tvForgetPwd = null;//忘记密码
	private MyDialog mDialog = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private String lat = "0";
	private String lon = "0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		mContext = this;
		startLocation();
		initWidget();
	}
	
	/**
	 * 初始化dialog
	 */
	private void showDialog() {
		mDialog = new MyDialog(mContext);
		if (mDialog != null) {
			mDialog.show();
		}
	}
	private void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
        	lat = String.valueOf(amapLocation.getLatitude());
        	lon = String.valueOf(amapLocation.getLongitude());
        }
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etUserName = (EditText) findViewById(R.id.etUserName);
		etPwd = (EditText) findViewById(R.id.etPwd);
		llLogin = (LinearLayout) findViewById(R.id.llLogin);
		llLogin.setOnClickListener(this);
		tvForgetPwd = (TextView) findViewById(R.id.tvForgetPwd);
		tvForgetPwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
		String uid = sharedPreferences.getString(CONST.UserInfo.uId, null);
		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
		String pwd = sharedPreferences.getString(CONST.UserInfo.passWord, null);
		
		CONST.UID = uid;
		CONST.USERNAME = userName;
		CONST.PASSWORD = pwd;
		
		etUserName.setText(userName);
		etPwd.setText(pwd);
		
		if (!TextUtils.isEmpty(etUserName.getText().toString()) && !TextUtils.isEmpty(etPwd.getText().toString())) {
			doLogin();
		}
	}
	
	private void doLogin() {
		if (checkInfo()) {
			showDialog();
			asyncQuery(CONST.TIANJIN_LOGIN);
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_username), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_password), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	private String getVersion() {
	    try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
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
			NameValuePair pair1 = new BasicNameValuePair("username", etUserName.getText().toString());
	        NameValuePair pair2 = new BasicNameValuePair("password", etPwd.getText().toString());
	        NameValuePair pair3 = new BasicNameValuePair("appid", com.tj.common.CONST.APPID);
	        NameValuePair pair4 = new BasicNameValuePair("device_id", "");
	        NameValuePair pair5 = new BasicNameValuePair("platform", "android");
	        NameValuePair pair6 = new BasicNameValuePair("os_version", android.os.Build.VERSION.RELEASE);
	        NameValuePair pair7 = new BasicNameValuePair("software_version", getVersion());
	        NameValuePair pair8 = new BasicNameValuePair("mobile_type", android.os.Build.MODEL);
	        NameValuePair pair9 = new BasicNameValuePair("address", "");
	        NameValuePair pair10 = new BasicNameValuePair("lat", lat);
	        NameValuePair pair11 = new BasicNameValuePair("lon", lon);
	        
	        nvpList.add(pair1);
			nvpList.add(pair2);
			nvpList.add(pair3);
			nvpList.add(pair4);
			nvpList.add(pair5);
			nvpList.add(pair6);
			nvpList.add(pair7);
			nvpList.add(pair8);
			nvpList.add(pair9);
			nvpList.add(pair10);
			nvpList.add(pair11);
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
								
								JSONArray array = new JSONArray(object.getString("column"));
								for (int i = 0; i < array.length(); i++) {
									JSONObject obj = array.getJSONObject(i);
									ColumnData data = new ColumnData();
									if (!obj.isNull("localviewid")) {
										data.id = obj.getString("localviewid");
									}
									if (!obj.isNull("name")) {
										data.name = obj.getString("name");
									}
									if (!obj.isNull("icon")) {
										data.icon = obj.getString("icon");
									}
									if (!obj.isNull("icon2")) {
										data.icon2 = obj.getString("icon2");
									}
									if (!obj.isNull("desc")) {
										data.desc = obj.getString("desc");
									}
									if (!obj.isNull("dataurl")) {
										data.dataUrl = obj.getString("dataurl");
									}
									if (!obj.isNull("showtype")) {
										data.showType = obj.getString("showtype");
									}
									if (!obj.isNull("child")) {
										JSONArray childArray = new JSONArray(obj.getString("child"));
										for (int j = 0; j < childArray.length(); j++) {
											JSONObject childObj = childArray.getJSONObject(j);
											ColumnData dto = new ColumnData();
											if (!childObj.isNull("localviewid")) {
												dto.id = childObj.getString("localviewid");
											}
											if (!childObj.isNull("name")) {
												dto.name = childObj.getString("name");
											}
											if (!childObj.isNull("icon")) {
												dto.icon = childObj.getString("icon");
											}
											if (!childObj.isNull("icon2")) {
												dto.icon2 = childObj.getString("icon2");
											}
											if (!childObj.isNull("desc")) {
												dto.desc = childObj.getString("desc");
											}
											if (!childObj.isNull("dataurl")) {
												dto.dataUrl = childObj.getString("dataurl");
											}
											if (!childObj.isNull("showtype")) {
												dto.showType = childObj.getString("showtype");
											}
											if (!childObj.isNull("child")) {
												JSONArray childItemArray = new JSONArray(childObj.getString("child"));
												for (int m = 0; m < childItemArray.length(); m++) {
													JSONObject childItemObj = childItemArray.getJSONObject(m);
													ColumnData itemDto = new ColumnData();
													if (!childItemObj.isNull("localviewid")) {
														itemDto.id = childItemObj.getString("localviewid");
													}
													if (!childItemObj.isNull("name")) {
														itemDto.name = childItemObj.getString("name");
													}
													if (!childItemObj.isNull("icon")) {
														itemDto.icon = childItemObj.getString("icon");
													}
													if (!childItemObj.isNull("icon2")) {
														itemDto.icon2 = childItemObj.getString("icon2");
													}
													if (!childItemObj.isNull("desc")) {
														itemDto.desc = childItemObj.getString("desc");
													}
													if (!childItemObj.isNull("dataurl")) {
														itemDto.dataUrl = childItemObj.getString("dataurl");
													}
													if (!childItemObj.isNull("showtype")) {
														itemDto.showType = childItemObj.getString("showtype");
													}
													if (!childItemObj.isNull("newsType")) {
														itemDto.newsType = childItemObj.getString("newsType");
													}
													if (!childItemObj.isNull("newscount")) {
														itemDto.newsCount = childItemObj.getString("newscount");
													}
													dto.child.add(itemDto);
												}
											}
											
//											if (!TextUtils.equals(dto.id, "23")) {//过滤掉等风来
												data.child.add(dto);
//											}
										}
									}
									
									CONST.dataList.add(data);
									
								}
								
								if (!object.isNull("info")) {
									JSONObject obj = new JSONObject(object.getString("info"));
									if (!obj.isNull("id")) {
										String uid = obj.getString("id");
										if (uid != null) {
											//把用户信息保存在sharedPreferance里
											SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
											Editor editor = sharedPreferences.edit();
											editor.putString(CONST.UserInfo.uId, uid);
											editor.putString(CONST.UserInfo.userName, etUserName.getText().toString());
											editor.putString(CONST.UserInfo.passWord, etPwd.getText().toString());
											editor.commit();
											
											CONST.UID = uid;
											CONST.USERNAME = etUserName.getText().toString();
											CONST.PASSWORD = etPwd.getText().toString();
											
											startActivity(new Intent(mContext, MainActivity.class));
											finish();
										}
									}
								}
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
		switch (v.getId()) {
		case R.id.llLogin:
			doLogin();
			break;

		default:
			break;
		}
	}
}
