package com.baselibrary.cloud;

public class CloudDto {

	public String name;
	public String time;
	public String url;
	public String path;
	
	public String getTime() {
		String t = time.split(" ")[1];
		return (String) t.subSequence(0, t.lastIndexOf(':'));
	}
}
