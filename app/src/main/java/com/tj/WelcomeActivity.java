package com.tj;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.baselibrary.BaseActivity;
import cxwl.shawn.tj.decision.R;

public class WelcomeActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		startIntentMain();
	}
	
	/**
	 * 启动线程进入主界面
	 */
	private void startIntentMain() {
		Handler handler = new Handler();
		handler.postDelayed(new MainRunnable(), 2000);
	}
	
	private class MainRunnable implements Runnable{
		@Override
		public void run() {
			startActivity(new Intent(getApplication(), LoginActivity.class));
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
}
