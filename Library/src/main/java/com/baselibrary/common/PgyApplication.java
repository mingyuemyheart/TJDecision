package com.baselibrary.common;

import com.pgyersdk.crash.PgyCrashManager;

import android.app.Application;

public class PgyApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		PgyCrashManager.register(this);
	}
	
}
