package com.tj.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class CityDto implements Parcelable{

	public String cityName = null;//城市名称
	public String cityId = null;//城市id
	public String spellName = null;//全拼名称
	public String provinceName = null;//省份名称
	public double lng = 0;//经度
	public double lat = 0;//维度

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.cityName);
		dest.writeString(this.cityId);
		dest.writeString(this.spellName);
		dest.writeString(this.provinceName);
		dest.writeDouble(this.lng);
		dest.writeDouble(this.lat);
	}

	public CityDto() {
	}

	protected CityDto(Parcel in) {
		this.cityName = in.readString();
		this.cityId = in.readString();
		this.spellName = in.readString();
		this.provinceName = in.readString();
		this.lng = in.readDouble();
		this.lat = in.readDouble();
	}

	public static final Creator<CityDto> CREATOR = new Creator<CityDto>() {
		@Override
		public CityDto createFromParcel(Parcel source) {
			return new CityDto(source);
		}

		@Override
		public CityDto[] newArray(int size) {
			return new CityDto[size];
		}
	};
}
