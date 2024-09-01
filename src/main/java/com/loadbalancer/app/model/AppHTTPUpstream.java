package com.loadbalancer.app.model;

import java.net.MalformedURLException;
import java.net.URL;


public class AppHTTPUpstream {
	
	private String ip; 
	private String port;
	private String protocol; 
	private URL address;
	
	public AppHTTPUpstream(String address) throws MalformedURLException {
		this.address= new URL(address);
		this.ip = this.address.getHost(); 
		try {
			this.port = new String(this.address.getPort()+""); 
		}catch(Exception e) {}
		this.protocol = this.address.getProtocol(); 
	}
	
	public String getIp() {
		return ip;
	}


	public URL getAddress() {
		return address;
	}

	public void setAddress(URL address) {
		this.address = address;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	@Override
	public String toString() {
		return this.address.toString();
	}
}
