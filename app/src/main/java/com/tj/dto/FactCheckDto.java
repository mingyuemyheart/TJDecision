package com.tj.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class FactCheckDto implements Parcelable{

	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	public String time;//时间
	public String stationId;//站号
	public String stationName;//站名
	public float temp;//温度
	public int windDir;//风向
	public float windSpeed;//风速
	public float rainFall;//降雨量
	public int humidity;//湿度
	public float pressure;//气压

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(this.x);
		dest.writeFloat(this.y);
		dest.writeString(this.time);
		dest.writeString(this.stationId);
		dest.writeString(this.stationName);
		dest.writeFloat(this.temp);
		dest.writeInt(this.windDir);
		dest.writeFloat(this.windSpeed);
		dest.writeFloat(this.rainFall);
		dest.writeInt(this.humidity);
		dest.writeFloat(this.pressure);
	}

	public FactCheckDto() {
	}

	protected FactCheckDto(Parcel in) {
		this.x = in.readFloat();
		this.y = in.readFloat();
		this.time = in.readString();
		this.stationId = in.readString();
		this.stationName = in.readString();
		this.temp = in.readFloat();
		this.windDir = in.readInt();
		this.windSpeed = in.readFloat();
		this.rainFall = in.readFloat();
		this.humidity = in.readInt();
		this.pressure = in.readFloat();
	}

	public static final Creator<FactCheckDto> CREATOR = new Creator<FactCheckDto>() {
		@Override
		public FactCheckDto createFromParcel(Parcel source) {
			return new FactCheckDto(source);
		}

		@Override
		public FactCheckDto[] newArray(int size) {
			return new FactCheckDto[size];
		}
	};
}
