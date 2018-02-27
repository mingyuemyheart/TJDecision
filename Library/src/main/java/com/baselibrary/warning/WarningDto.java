package com.baselibrary.warning;

import android.os.Parcel;
import android.os.Parcelable;

public class WarningDto implements Parcelable{

	public String name;// 预警全名
	public String html;// 详情需要用到的html
	public String time;// 预警发布时间
	public String lat;// 纬度
	public String lng;// 经度
	public String id;// 预警id，这里是html的后四位
	public String type;//预警类型，id的前两位
	public String color;// 预警颜色,红橙黄蓝，id的后两位
	public String tag;//标示

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.html);
		dest.writeString(this.time);
		dest.writeString(this.lat);
		dest.writeString(this.lng);
		dest.writeString(this.id);
		dest.writeString(this.type);
		dest.writeString(this.color);
		dest.writeString(this.tag);
	}

	public WarningDto() {
	}

	protected WarningDto(Parcel in) {
		this.name = in.readString();
		this.html = in.readString();
		this.time = in.readString();
		this.lat = in.readString();
		this.lng = in.readString();
		this.id = in.readString();
		this.type = in.readString();
		this.color = in.readString();
		this.tag = in.readString();
	}

	public static final Creator<WarningDto> CREATOR = new Creator<WarningDto>() {
		@Override
		public WarningDto createFromParcel(Parcel source) {
			return new WarningDto(source);
		}

		@Override
		public WarningDto[] newArray(int size) {
			return new WarningDto[size];
		}
	};
}
