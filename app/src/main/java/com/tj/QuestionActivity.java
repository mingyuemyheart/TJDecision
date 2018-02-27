package com.tj;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import cxwl.shawn.tj.decision.R;

public class QuestionActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private RelativeLayout reTitle = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;
	private WebSettings webSettings = null;
	private String url = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
		mContext = this;
		initWidget();
		initWebView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
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
	}

	/**
	 * 初始化webview
	 */
	private void initWebView() {
		url = getIntent().getStringExtra(CONST.WEB_URL);
		if (url == null) {
			return;
		}
		webView = (WebView) findViewById(R.id.webView);
		webSettings = webView.getSettings();
		webSettings.setTextSize(WebSettings.TextSize.NORMAL);
		
		//支持javascript
		webSettings.setJavaScriptEnabled(true); 
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true); 
		// 设置出现缩放工具 
		webSettings.setBuiltInZoomControls(true);
		//扩大比例的缩放
		webSettings.setUseWideViewPort(true);
		//自适应屏幕
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
		webView.loadUrl(url);
		
		WebChromeClient wcc = new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (title != null) {
					tvTitle.setText(title);
				}
			}
		};
		webView.setWebChromeClient(wcc);
		
		WebViewClient wvc = new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
				url = itemUrl;
				webView.loadUrl(url);
				return true;
			}
		};
		
		webView.setWebViewClient(wvc);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				return true;
			}else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}
	
}
