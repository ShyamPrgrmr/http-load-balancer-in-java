package com.loadbalancer.app.model;

//import java.io.InputStream;

public class AppHTTPBody {

	private String body;
	//private InputStream bodyStream; 
	
	public AppHTTPBody(String body){
		this.body = body; 
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "AppHTTPBody [body=" + body + "]";
	} 
	
	
}
