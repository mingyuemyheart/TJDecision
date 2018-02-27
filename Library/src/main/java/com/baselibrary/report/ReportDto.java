package com.baselibrary.report;

import android.graphics.drawable.Drawable;

public class ReportDto {

	int color;// 天气现象对应的颜色
	Drawable drawable;// 天气现象对应的drawable
	String phenomenon;// 天气现象
	int phenomenonCode;// 天气现象代码
	int subCount;// 每种天气现象数量
	float angel;// 饼图每种天气现象的角度

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

	public float getAngel() {
		return angel;
	}

	public void setAngel(float angel) {
		this.angel = angel;
	}

	public int getSubCount() {
		return subCount;
	}

	public void setSubCount(int subCount) {
		this.subCount = subCount;
	}

	public String getPhenomenon() {
		return phenomenon;
	}

	public void setPhenomenon(String phenomenon) {
		this.phenomenon = phenomenon;
	}

	public int getPhenomenonCode() {
		return phenomenonCode;
	}

	public void setPhenomenonCode(int phenomenonCode) {
		this.phenomenonCode = phenomenonCode;
	}

}
