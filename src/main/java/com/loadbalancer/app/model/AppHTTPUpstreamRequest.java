package com.loadbalancer.app.model;

public class AppHTTPUpstreamRequest {
	private AppHTTPUpstream upstream;  
	private AppHTTPRequest request; 
	public AppHTTPUpstreamRequest(AppHTTPRequest request, AppHTTPUpstream upstream) {
		this.upstream = upstream; 
		this.request = request; 
	}
	public AppHTTPUpstream getUpstream() {
		return upstream;
	}
	public AppHTTPRequest getRequest() {
		return request;
	}
}
