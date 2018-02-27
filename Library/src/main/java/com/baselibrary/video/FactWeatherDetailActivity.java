package com.baselibrary.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.VideoView;

import com.baselibrary.R;

public class FactWeatherDetailActivity extends Activity implements SurfaceHolder.Callback, OnPreparedListener, OnCompletionListener{
	
	private WebView webView = null;
	
	private SurfaceView surfaceView = null;
	private SurfaceHolder surfaceHolder = null;
	private MediaPlayer mPlayer = null;
	
	private VideoView videoView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fact_weather_detail);
		initWebView();
		setWebclient();
		initSurfaceView();
		initVideoView();
	}
	
	/**
	 * 初始化webview
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		webView = (WebView) findViewById(R.id.webview);
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(false);
		settings.setUseWideViewPort(true);
		settings.setSupportZoom(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		if (getIntent() != null) {
//			if (getIntent().hasExtra("cityName")) {
//				String cityName = getIntent().getStringExtra("cityName");
//				if (cityName != null) {
//					tvTitle.setText(cityName);
//				}
//			}
			if (getIntent().hasExtra("webUrl")) {
				String webUrl = getIntent().getStringExtra("webUrl");
				if (webUrl != null) {
					webView.loadUrl(webUrl);
				}
			}
		}
	}
	
	private void setWebclient() {
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
			
		});

		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
			}

			@Override
			public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
				callback.invoke(origin, true, false);
				super.onGeolocationPermissionsShowPrompt(origin, callback);
			}
		});
	}
	
	/**
	 * 初始化surfaceView
	 */
	@SuppressWarnings("deprecation")
	private void initSurfaceView() {
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setDisplay(holder);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);
        //设置显示视频显示在SurfaceView上
        try {
        	if (getIntent().getStringExtra("webUrl") != null) {
            	mPlayer.setDataSource(getIntent().getStringExtra("webUrl"));
            	mPlayer.prepareAsync();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		surfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = holder;
        releaseMediaPlayer();
	}
	
	@Override
	public void onPrepared(MediaPlayer player) {
    	startPlayVideo();
	}
	
	/**
	 * 开始播放视频
	 */
	private void startPlayVideo() {
		if (mPlayer != null) {
			mPlayer.start();
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * 释放MediaPlayer资源
	 */
	private void releaseMediaPlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	/**
	 * 初始化videoview
	 */
	private void initVideoView() {
		videoView = (VideoView) findViewById(R.id.videoView);
		if (getIntent().getStringExtra("webUrl") != null) {
			MediaController mc = new MediaController(this);
	        mc.setAnchorView(videoView);
	        videoView.setMediaController(mc);
	        videoView.setVideoPath(getIntent().getStringExtra("webUrl"));
	        videoView.requestFocus();
	        videoView.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
			        videoView.start();
				}
			});
		}
	}

}
