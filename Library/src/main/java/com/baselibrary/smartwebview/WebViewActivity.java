package com.baselibrary.smartwebview;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baselibrary.BaseActivity;
import com.baselibrary.R;
import com.baselibrary.common.CONST;
import com.baselibrary.utils.CustomHttpClient;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

public class WebViewActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private RelativeLayout reTitle = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvControl = null;
	private WebView webView = null;
	private WebSettings webSettings = null;
	private String url = null;
	private String imgUrl = null;
	private int drawable = -1;
	private ImageView ivBig = null;
	private ImageView ivMiddle = null;
	private ImageView ivSmall = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
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
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		ivBig = (ImageView) findViewById(R.id.ivBig);
		ivBig.setOnClickListener(this);
		ivMiddle = (ImageView) findViewById(R.id.ivMiddle);
		ivMiddle.setOnClickListener(this);
		ivSmall = (ImageView) findViewById(R.id.ivSmall);
		ivSmall.setOnClickListener(this);
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		
		if (TextUtils.equals(CONST.STYLE, CONST.STYLE_ONE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title1);
		}else if (TextUtils.equals(CONST.STYLE, CONST.STYLE_TWO)) {
			reTitle.setBackgroundResource(R.drawable.bg_title2);
		}else if (TextUtils.equals(CONST.STYLE, CONST.STYLE_THREE)) {
			reTitle.setBackgroundResource(R.drawable.bg_title3);
		}
		
		imgUrl = getIntent().getStringExtra(CONST.INTENT_IMGURL);
		String appid = getIntent().getStringExtra(CONST.INTENT_APPID);
		if (TextUtils.equals(appid, "15")) {
			tvControl.setVisibility(View.VISIBLE);
			PlatformConfig.setWeixin(CONST.GUIZHOU_WXAPPID, CONST.GUIZHOU_WXSIGN);
			drawable = R.drawable.iv_guizhou;
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
//		webView.loadUrl(url);
		
		//添加请求头
		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
		webView.loadUrl(url, extraHeaders);
		
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
//				webView.loadUrl(url);
				//添加请求头
				Map<String, String> extraHeaders = new HashMap<String, String>();
				extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
				webView.loadUrl(url, extraHeaders);
				return true;
			}
		};
		
		webView.setWebViewClient(wvc);
	}
	
	private ShareBoardlistener shareBoardlistener = new ShareBoardlistener() {
		@Override
		public void onclick(SnsPlatform arg0, SHARE_MEDIA arg1) {
			ShareAction sAction = new ShareAction(WebViewActivity.this);
			sAction.setPlatform(arg1);
			sAction.setCallback(umShareListener);
			if (imgUrl == null) {
	        	sAction.withMedia(new UMImage(mContext, drawable));
			}else {
				sAction.withMedia(new UMImage(mContext, imgUrl));
			}
	        sAction.withTitle(tvTitle.getText().toString());
	        sAction.withText(tvTitle.getText().toString());
	        if (!TextUtils.isEmpty(url)) {
	        	sAction.withTargetUrl(url);
			}
	        sAction.share();
		}
	};
	
	private UMShareListener umShareListener = new UMShareListener() {
		@Override
		public void onResult(SHARE_MEDIA arg0) {
			Toast.makeText(mContext, "成功", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onError(SHARE_MEDIA arg0, Throwable arg1) {
			Toast.makeText(mContext, "失败", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onCancel(SHARE_MEDIA arg0) {
			Toast.makeText(mContext, "取消", Toast.LENGTH_SHORT).show();
		}
	};
	
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

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.tvControl) {
	        ShareAction sAction = new ShareAction(WebViewActivity.this);
	        sAction.setDisplayList(SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.WEIXIN);
	        sAction.setShareboardclickCallback(shareBoardlistener);
	        sAction.open();
		}else if (v.getId() == R.id.ivBig) {
			webSettings.setTextSize(WebSettings.TextSize.LARGER);
			ivBig.setImageResource(R.drawable.iv_textsize_big_selected);
			ivMiddle.setImageResource(R.drawable.iv_textsize_middle);
			ivSmall.setImageResource(R.drawable.iv_textsize_small);
		}else if (v.getId() == R.id.ivMiddle) {
			webSettings.setTextSize(WebSettings.TextSize.NORMAL);
			ivBig.setImageResource(R.drawable.iv_textsize_big);
			ivMiddle.setImageResource(R.drawable.iv_textsize_middle_selected);
			ivSmall.setImageResource(R.drawable.iv_textsize_small);
		}else if (v.getId() == R.id.ivSmall) {
			webSettings.setTextSize(WebSettings.TextSize.SMALLER);
			ivBig.setImageResource(R.drawable.iv_textsize_big);
			ivMiddle.setImageResource(R.drawable.iv_textsize_middle);
			ivSmall.setImageResource(R.drawable.iv_textsize_small_selected);
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
