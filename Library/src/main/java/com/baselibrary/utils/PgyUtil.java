package com.baselibrary.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baselibrary.R;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

public class PgyUtil {

	/**
	 * 蒲公英自动更新
	 * @param activity
	 * @param flag true为主界面检测，false为设置界面里检测
	 */
	public static void PgyUpdate(final Activity activity, final boolean flag) {
		PgyUpdateManager.register(activity, new UpdateManagerListener() {
			@Override
			public void onUpdateAvailable(String result) {
				final AppBean appBean = getAppBeanFromString(result);
				LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.pgy_update, null);
				TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
				TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
				LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
				LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
				
				final Dialog dialog = new Dialog(activity, R.style.CustomProgressDialog);
				dialog.setContentView(view);
				dialog.show();
				
				tvTitle.setText(activity.getString(R.string.auto_update));
				tvContent.setText(activity.getString(R.string.new_version)+appBean.getVersionName()+"\n\n"
						+activity.getString(R.string.update_log)+"\n"+appBean.getReleaseNote());
				llNegative.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});
				
				llPositive.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
						startDownloadTask(activity, appBean.getDownloadURL());
					}
				});
			}
			
			@Override
			public void onNoUpdateAvailable() {
				if (!flag) {
					Toast.makeText(activity, activity.getString(R.string.already_new_version), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
}
