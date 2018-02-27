package com.tj.fragment;

/**
 * 特色服务
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baselibrary.cloud.CloudActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.common.ColumnData;
import com.baselibrary.index.IndexActivity;
import com.baselibrary.minute.MinuteFallActivity;
import com.baselibrary.radar.RadarActivity;
import com.baselibrary.report.ReportWeatherActivity;
import com.baselibrary.routeweather.RouteWeatherActivity;
import com.baselibrary.typhoon.TyphoonRouteActivity;
import com.baselibrary.video.FactWeatherActivity;
import com.tj.CityActivity;
import cxwl.shawn.tj.decision.R;
import com.tj.UniqueActivity;
import com.tj.WaitWindActivity;
import com.tj.view.CircleMenuLayout2;
import com.tj.view.CircleMenuLayout2.OnMenuItemClickListener2;

public class UniqueFragment extends Fragment{
	
	private TextView tvDes = null;
	private MyBroadCastReceiver mReceiver = null;
	private CircleMenuLayout2 mCircleMenuLayout = null;
	private List<ColumnData> showList = new ArrayList<ColumnData>();
	private ColumnData data = null;
	private ImageView ivCloud = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.unique_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initBroadCast();
	}
	
	private void initWidget(View view) {
		tvDes = (TextView) view.findViewById(R.id.tvDes);
		ivCloud = (ImageView) view.findViewById(R.id.ivCloud);
		
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,1f,
				Animation.RELATIVE_TO_SELF,-1f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f);
		animation.setDuration(30000);
		animation.setRepeatCount(Animation.INFINITE);
		ivCloud.startAnimation(animation);
		
		data = getArguments().getParcelable("data");
		if (data != null) {
			initCircleLayout(view);
		}
	}
	
	private void initCircleLayout(View view) {
		final int size = data.child.size();
		showList.clear();
		if (size > 6) {
			for (int i = 0; i < 5; i++) {
				showList.add(data.child.get(i));
			}
			ColumnData dto = new ColumnData();
			dto.name = getString(R.string.more);
			dto.desc = getString(R.string.look_more_column);
			showList.add(dto);
		}else {
			for (int i = 0; i < size; i++) {
				showList.add(data.child.get(i));
			}
		}
		
		mCircleMenuLayout = (CircleMenuLayout2) view.findViewById(R.id.id_menulayout);
		mCircleMenuLayout.setMenuItemIconsAndTexts(showList);
		mCircleMenuLayout.setOnMenuItemClickListener(new OnMenuItemClickListener2() {
				@Override
				public void itemClick(View view, int pos) {
					String id = showList.get(pos).id;
					Intent intent = null;
					if (id == null) {
						if (pos == 5) {
							intent = new Intent(getActivity(), UniqueActivity.class);
						}
					}else {
						if (id.equals("21")) {
							intent = new Intent(getActivity(), MinuteFallActivity.class);
						}else if (id.equals("22")) {
							intent = new Intent(getActivity(), IndexActivity.class);
						}else if (id.equals("23")) {
							intent = new Intent(getActivity(), WaitWindActivity.class);
							intent.putExtra(CONST.WEB_URL, com.tj.common.CONST.WAIT_WIND);
						}else if (id.equals("24")) {
							intent = new Intent(getActivity(), FactWeatherActivity.class);
						}else if (id.equals("25")) {
							intent = new Intent(getActivity(), ReportWeatherActivity.class);
						}else if (id.equals("26")) {
							intent = new Intent(getActivity(), TyphoonRouteActivity.class);
						}else if (id.equals("27")) {
							intent = new Intent(getActivity(), RouteWeatherActivity.class);
						}else if (id.equals("28")) {
							intent = new Intent(getActivity(), RadarActivity.class);
						}else if (id.equals("29")) {
							intent = new Intent(getActivity(), CloudActivity.class);
						}else if (id.equals("11")) {
							intent = new Intent(getActivity(), CityActivity.class);
						}
					}
					
					if (intent != null) {
						intent.putParcelableArrayListExtra("dataList", (ArrayList<? extends Parcelable>) data.child);
						intent.putExtra(CONST.ACTIVITY_NAME, showList.get(pos).name);
						intent.putExtra(CONST.LATITUDE, com.tj.common.CONST.tianJin_LATITUDE);
						intent.putExtra(CONST.LONGITUDE, com.tj.common.CONST.tianJin_LONGITUDE);
						String[] values = getResources().getStringArray(R.array.tianjin_radars);
						intent.putExtra(CONST.RADAR_NAME_ARRAY, values);
						startActivity(intent);
					}
					
				}

				@Override
				public void itemCenterClick(View view) {
					
				}
			});
		
		//设置左边布局宽高
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int displayWidth = dm.widthPixels;
		LinearLayout llLeft = (LinearLayout) view.findViewById(R.id.llLeft);
		int w = LinearLayout.MeasureSpec.makeMeasureSpec(0, LinearLayout.MeasureSpec.UNSPECIFIED);
		int h = LinearLayout.MeasureSpec.makeMeasureSpec(0, LinearLayout.MeasureSpec.UNSPECIFIED);
		llLeft.measure(w, h);
		int width = llLeft.getMeasuredWidth();
		int height = llLeft.getMeasuredHeight();
		llLeft.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int)(height*mCircleMenuLayout.percent)));
		llLeft.setMinimumWidth((int)(width*1.5));
	}
	
	private void initBroadCast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(com.tj.common.CONST.BROADCAST_UNIQUE);
		getActivity().registerReceiver(mReceiver, intentFilter);
	}
	
	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(com.tj.common.CONST.BROADCAST_UNIQUE)) {
				tvDes.setText(intent.getExtras().getString(com.tj.common.CONST.DESCRIBE));
			}
		}
		
	}
	
}
