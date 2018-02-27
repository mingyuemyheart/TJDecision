package com.tj;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baselibrary.BaseActivity;
import com.baselibrary.common.CONST;
import com.baselibrary.common.ColumnData;
import com.baselibrary.utils.PgyUtil;
import com.baselibrary.warning.WarningFragment;
import com.tj.common.MyApplication;
import com.tj.fragment.ServiceFragment;
import com.tj.fragment.UniqueFragment;
import com.tj.fragment.WeatherDetailFragment;
import com.tj.view.MainViewPager;
import cxwl.shawn.tj.decision.R;

public class MainActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private ImageView ivSetting = null;
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<Fragment>();
	private LinearLayout llTab1 = null, llTab2 = null, llTab3 = null, llTab4 = null;
	private ImageView ivTab1 = null, ivTab2 = null, ivTab3 = null, ivTab4 = null;
	private TextView tvTab1 = null, tvTab2 = null, tvTab3 = null, tvTab4 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = this;
        MyApplication.addDestoryActivity(MainActivity.this, com.tj.common.CONST.MainActivity);
		initWidget();
		initViewPager();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		PgyUtil.PgyUpdate(MainActivity.this, true);
		
		ivSetting = (ImageView) findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		llTab1 = (LinearLayout) findViewById(R.id.llTab1);
		llTab1.setOnClickListener(new MyOnClickListener(0));
		llTab2 = (LinearLayout) findViewById(R.id.llTab2);
		llTab2.setOnClickListener(new MyOnClickListener(1));
		llTab3 = (LinearLayout) findViewById(R.id.llTab3);
		llTab3.setOnClickListener(new MyOnClickListener(2));
		llTab4 = (LinearLayout) findViewById(R.id.llTab4);
		llTab4.setOnClickListener(new MyOnClickListener(3));
		ivTab1 = (ImageView) findViewById(R.id.ivTab1);
		ivTab2 = (ImageView) findViewById(R.id.ivTab2);
		ivTab3 = (ImageView) findViewById(R.id.ivTab3);
		ivTab4 = (ImageView) findViewById(R.id.ivTab4);
		tvTab1 = (TextView) findViewById(R.id.tvTab1);
		tvTab2 = (TextView) findViewById(R.id.tvTab2);
		tvTab3 = (TextView) findViewById(R.id.tvTab3);
		tvTab4 = (TextView) findViewById(R.id.tvTab4);
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		Fragment fragment = null;
		int size = CONST.dataList.size();
		for (int i = 0; i < size; i++) {
			ColumnData dto = CONST.dataList.get(i);
			if (dto.id.equals("1")) {
				tvTab1.setText(dto.name);
				fragment = new ServiceFragment();
			}else if (dto.id.equals("4")) {
				tvTab2.setText(dto.name);
				fragment = new WeatherDetailFragment();
			}else if (dto.id.equals("2")) {
				tvTab3.setText(dto.name);
				fragment = new UniqueFragment();
			}else if (dto.id.equals("3")) {
				tvTab4.setText(dto.name);
				fragment = new WarningFragment();
			}
			Bundle bundle = new Bundle();
			bundle.putString(CONST.INTENT_APPID, com.tj.common.CONST.APPID);
			bundle.putString(CONST.PROVINCE_NAME, getString(R.string.tianjin_city));
			bundle.putParcelable("data", dto);
			bundle.putString(CONST.CITY_ID, com.tj.common.CONST.TIANJIN_CITYID);
			bundle.putString(CONST.WARNING_ID, com.tj.common.CONST.TIANJIN_WARNINGID);
			bundle.putDouble(CONST.LATITUDE, com.tj.common.CONST.tianJin_LATITUDE);
			bundle.putDouble(CONST.LONGITUDE, com.tj.common.CONST.tianJin_LONGITUDE);
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}
		
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		viewPager.setSlipping(false);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				ivTab1.setImageResource(R.drawable.iv_tab1_press);
				ivTab2.setImageResource(R.drawable.iv_tab2);
				ivTab3.setImageResource(R.drawable.iv_tab3);
				ivTab4.setImageResource(R.drawable.iv_tab4);
				tvTab1.setTextColor(getResources().getColor(R.color.blue));
				tvTab2.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab3.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab4.setTextColor(getResources().getColor(R.color.text_color4));
				break;
			case 1:
				ivTab1.setImageResource(R.drawable.iv_tab1);
				ivTab2.setImageResource(R.drawable.iv_tab2_press);
				ivTab3.setImageResource(R.drawable.iv_tab3);
				ivTab4.setImageResource(R.drawable.iv_tab4);
				tvTab1.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab2.setTextColor(getResources().getColor(R.color.blue));
				tvTab3.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab4.setTextColor(getResources().getColor(R.color.text_color4));
				break;
			case 2:
				ivTab1.setImageResource(R.drawable.iv_tab1);
				ivTab2.setImageResource(R.drawable.iv_tab2);
				ivTab3.setImageResource(R.drawable.iv_tab3_press);
				ivTab4.setImageResource(R.drawable.iv_tab4);
				tvTab1.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab2.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab3.setTextColor(getResources().getColor(R.color.blue));
				tvTab4.setTextColor(getResources().getColor(R.color.text_color4));
				break;
			case 3:
				ivTab1.setImageResource(R.drawable.iv_tab1);
				ivTab2.setImageResource(R.drawable.iv_tab2);
				ivTab3.setImageResource(R.drawable.iv_tab3);
				ivTab4.setImageResource(R.drawable.iv_tab4_press);
				tvTab1.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab2.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab3.setTextColor(getResources().getColor(R.color.text_color4));
				tvTab4.setTextColor(getResources().getColor(R.color.blue));
				break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * @ClassName: MyOnClickListener
	 * @Description: TODO头标点击监听
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:46:08
	 *
	 */
	private class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			viewPager.setCurrentItem(index);
		}
	};

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if ((System.currentTimeMillis() - mExitTime) > 2000) {
    			Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    			mExitTime = System.currentTimeMillis();
    		} else {
    			finish();
    		}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivSetting:
			startActivity(new Intent(mContext, SettingActivity.class));
			break;

		default:
			break;
		}
	}

}
