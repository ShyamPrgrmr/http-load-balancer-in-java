package com.loadbalancer.app.model;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.loadbalancer.app.enums.AppHTTPMethod;
import com.sun.net.httpserver.HttpExchange;

public class AppHTTPRequest {

	private URI url;
	private AppHTTPMethod method;
	private AppHTTPHeaders header; 
	private AppHTTPBody body;
	private String requestID;
	private String senderIP; 
	private HttpExchange exchange; 
	private String protocol; 
	
	
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public HttpExchange getExchange() {
		return exchange;
	}
	public void setExchange(HttpExchange exchange) {
		this.exchange = exchange;
	}
	public String getRequestID() {
		return requestID;
	}
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	public String getSenderIP() {
		return senderIP;
	}
	public void setSenderIP(String senderIP) {
		this.senderIP = senderIP;
	}
	
	public URI getUrl() {
		return url;
	}
	public void setUrl(URI uri) {
		this.url = uri;
	}
	public AppHTTPMethod getMethod() {
		return method;
	}
	public void setMethod(AppHTTPMethod method) {
		this.method = method;
	}
	public AppHTTPHeaders getHeader() {
		return header;
	}
	public void setHeader(AppHTTPHeaders header) {
		this.header = header;
	}
	public AppHTTPBody getBody() {
		return body;
	}
	public void setBody(AppHTTPBody body) {
		this.body = body;
	} 
	
	
	@Override
	public String toString() {
		return "AppHTTPRequest [url=" + url + ", method=" + method + ", headers=" + header + ", body=" + body
				+ ", requestID=" + requestID + ", clientIP=" + senderIP + "]";
	}
}
