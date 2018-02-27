package com.baselibrary.cloud;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.baselibrary.manager.RainManager;
import com.scene.net.Net;

public class CloudManager {
	public enum Classify {
		RAIN, 
		THUMDER, 
		CLOUD
	}
	
	private final static String APPID = "6f688d62594549a2";//机密需要用到的AppId
	private final static String CHINAWEATHER_DATA = "chinaweather_data";//加密秘钥名称
//	private static final String RADAR_RAIN_URL = "http://scapi.weather.com.cn/product/list/precipitation_mector_20.html";
	private static final String RADAR_RAIN_URL = "http://scapi.weather.com.cn/product/list/radar_mector_r_20.html";
	private static final String RADAR_THUMDER_URL = "http://scapi.weather.com.cn/product/list/leidian_mector20.html";
//	public static final String RADAR_CLOUD_URL = "http://scapi.weather.com.cn/product/list/cloud_mector20.html";
	public static final String RADAR_CLOUD_URL = "http://scapi.weather.com.cn/product/list/cloudnew_20.html";
	private Map<Classify,  List<CloudDto>> mRadars = new HashMap<Classify, List<CloudDto>>();
	private List<Classify> mSuccesseds = new ArrayList<Classify>();
	private Context mContext;
//	private BaseHandler mHandler = new BaseHandler() {
//		public void handleMessage(android.os.Message msg) {
//			
//		};
//	};
	private LoadThread mLoadThread;
	public interface RadarListener {
		public static final int RESULT_SUCCESSED = 1;
		public static final int RESULT_FAILED = 2;
		void onResult(int result, List<CloudDto> images, Bundle data);
		void onProgress(String url, int progress);
	}
	
	public CloudManager(Context context) {
		mContext = context.getApplicationContext();
	}
	private String getUrl(Classify classify) {
		switch (classify) {
		case RAIN:
			return RADAR_RAIN_URL;
		case THUMDER:
			return RADAR_THUMDER_URL;
		case CLOUD:
			return RADAR_CLOUD_URL;
		default:
			break;
		}
		return null;
	}
	
	public void loadImagesAsyn(List<CloudDto> cloudList, final Classify classify, final float level, final RadarListener listener) {
		if (mLoadThread != null) {
			mLoadThread.cancel();
			mLoadThread = null;
		}
		mLoadThread = new LoadThread(cloudList, classify, level, listener);
		mLoadThread.start();
	}
	
	/**
	 * loncenter=105.0
	&  latcenter=33.2
	&  lonspan=64.0
	&  latspan=42.0
	&  width=1000
	&  proj=webmector
	&  date=201409051112
	&  appid=f63d32
	&  key=6YpancjAbsqc8ud%2FYMIHqP5%2B8vU%3D
	 */
	public static String getRadarUrl(String url, String date) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(url);
		buffer.append("?");
		buffer.append("date=").append(date);
		buffer.append("&");
		buffer.append("appid=").append(APPID);
		
		String key = getKey(CHINAWEATHER_DATA, buffer.toString());
		buffer.delete(buffer.lastIndexOf("&"), buffer.length());
		
		buffer.append("&");
		buffer.append("appid=").append(APPID.subSequence(0, 6));
		buffer.append("&");
		buffer.append("key=").append(key.subSequence(0, key.length() - 3));
		String result = buffer.toString();
		return result;
	}
	
	private static String getKey(String key, String src) {
		try{
			byte[] rawHmac = null;
			byte[] keyBytes = key.getBytes("UTF-8");
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			rawHmac = mac.doFinal(src.getBytes("UTF-8"));
			String encodeStr = Base64.encodeToString(rawHmac, Base64.DEFAULT);
			String keySrc = URLEncoder.encode(encodeStr, "UTF-8");
			return keySrc;
		}catch(Exception e){
			Log.e("SceneException", e.getMessage(), e);
		}
		return null;
	}
	
	public void onDestory() {
		mRadars.clear();
		mSuccesseds.clear();
		File file = getDir();
		if (file != null && file.exists()) {
			File[] files = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".png");
				}
			});
			for (File f : files) {
				f.delete();
			}
		}
		
	}
	
	private File getDir() {
		return mContext.getCacheDir();
	}
	
	private class LoadThread extends Thread {
		private List<CloudDto> radars;
		private RadarListener listener;
		private Classify classify;
		private float level;
		private int count;
		public LoadThread(List<CloudDto> cloudList, Classify classify, float level, RadarListener listener) {
			this.radars = cloudList;
			this.classify = classify;
			this.level = level;
			this.listener = listener;
		}
		
		@Override
		public void run() {
			super.run();
			Bundle data = new Bundle();
			data.putString(Keys.NAME, String.valueOf(classify));
			if (radars != null) {
				final String date = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmm");
				int len = count = radars.size();
				for (int i = 0; i < len ; i++) {
					CloudDto radar = radars.get(i);
					String url = getRadarUrl(radar.url, date);
					loadImage(i, url, date, radars, data, listener, String.valueOf(classify));
				}
			}
		}
		
		private void loadImage(final int index, final String url, final String date, final List<CloudDto> results, final Bundle data, final RadarListener listener, final String name) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String path = decodeFromUrl(url, name);
					if (!TextUtils.isEmpty(path)) {
						Log.e("path = ", path);
						results.get(index).path = path;
					}
					finished(url, results, data);
				}
			}).start();
			
//			ExecutorService service = Executors.newFixedThreadPool(20);  
//			service.submit(new Runnable(){  
//	            @Override  
//	            public void run() {  
//	            	String path = decodeFromUrl(url, name);
//					if (!TextUtils.isEmpty(path)) {
//						Log.e("path = ", path);
//						results.get(index).path = path;
//					}
//					finished(url, results, data);
//	            }  
//	        });  
		}
		
		private synchronized void finished(final String url, final List<CloudDto> results, Bundle data) {
			int max = results.size();
			count -- ;
			int progress = (int) (((max - count) * 1.0 / max) * 100);
			if (listener != null) {
				listener.onProgress(url, progress);
				if (count <= 0) {
					listener.onResult(results == null ? RadarListener.RESULT_FAILED : RadarListener.RESULT_SUCCESSED, results, data);
					mSuccesseds.add(classify);
				}
			}
		}
		
		private String getName(String url) {
			if (url.contains("?")) {
				String temp = url.split("?")[0];
				return temp.substring(temp.lastIndexOf('\\'));
			} else {
				return url.substring(url.lastIndexOf('\\'));
			}
		}
		
		/**
		 * 
		 * @param url
		 * @return path of the image
		 */
		private String decodeFromUrl(String url, String name){
		    try {
		    	URLConnection connection = new URL(url).openConnection();
		    	connection.setConnectTimeout(5000);
		    	connection.connect();
		    	
		    	File file = FileUtils.getFile(getDir(), name+System.currentTimeMillis() + ".png");
		    	BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		    	
		    	InputStream is = connection.getInputStream();
		    	byte[] buffer = new byte[8 * 1024];
		    	int read = -1;
		    	while ((read = is.read(buffer)) != -1) {
					os.write(buffer, 0, read);
				}
		    	os.flush();
		    	os.close();
		    	System.gc();
		    	return file.getAbsolutePath();
		    } catch (Exception e) {
		    	Log.e("SceneException", e.getMessage(), e);
		    }

		    return null;
		}
		
		void cancel() {
			listener = null;
		}
	}
	
	private List<CloudDto> parseRadar(String result) {
		if (result == null) {
			return null;
		}
		try {
			JSONObject object = new JSONObject(result);
			JSONArray radars = object.getJSONArray("l");
			int len = radars.length();
			List<CloudDto> list = new ArrayList<CloudDto>();
			for (int i = 0; i < len; i ++) {
				JSONObject item = radars.getJSONObject(i);
				String time = item.getString("l1");
				String url = item.getString("l2").split("\\?")[0]
								;
				CloudDto radar = new CloudDto();
				radar.time = time;
				radar.url = url;
				list.add(0, radar);
			}
			return list;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private class Keys {
		public static final String ADDRESS = "address";
		public static final String CITY = "city";
		public static final String CITY_ID = "city_id";
		public static final String DISTRICT = "district";
		public static final String STREET = "street";
		public static final String LAT = "lat";
		public static final String LNG = "lng";
		public static final String LAT_SPAN = "lat_span";
		public static final String LNG_SPAN = "lng_span";
		public static final String CONTENT = "content";
		public static final String NAME = "name";
	}

}
