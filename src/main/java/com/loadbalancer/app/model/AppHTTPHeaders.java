package com.loadbalancer.app.model;

import java.util.HashMap;

public class AppHTTPHeaders {
	private HashMap<String,String> header; 
	public AppHTTPHeaders(HashMap<String,String> map) {
		this.header = map; 
	}
	public HashMap<String, String> getMap(){return this.header; }
	
	@Override
	public String toString() {
		return "AppHTTPHeaders [header=" + header + "]";
	}
	
}
