package com.baselibrary.smartwebview;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baselibrary.R;
import com.baselibrary.common.ColumnData;
import com.baselibrary.utils.CustomHttpClient;

public class WebViewFragment extends Fragment {
	
	private WebView webView = null;
	private String url = null;
	private ColumnData channel = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.webview_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWebView(view);
	}
	
	/**
	 * 初始化webview
	 */
	private void initWebView(View view) {
		channel = getArguments().getParcelable("data");
		if (channel != null) {
			url = channel.dataUrl;
			if (url == null) {
				return;
			}
			webView = (WebView) view.findViewById(R.id.webView);
//			webView.loadUrl(url);
			
			//添加请求头
			Map<String, String> extraHeaders = new HashMap<String, String>();
			extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
			webView.loadUrl(url, extraHeaders);
			
			WebChromeClient wcc = new WebChromeClient() {
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
				}
			};
			webView.setWebChromeClient(wcc);
			
			WebViewClient wvc = new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
					url = itemUrl;
//					webView.loadUrl(url);
					//添加请求头
					Map<String, String> extraHeaders = new HashMap<String, String>();
					extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
					webView.loadUrl(url, extraHeaders);
					return true;
				}
			};
			
			webView.setWebViewClient(wvc);
		}
	}
	
}
