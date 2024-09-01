package com.loadbalancer.app.model.server;

public class AddUpstreamBody {
	String address;
	public AddUpstreamBody() {}
	public AddUpstreamBody(String addr) { this.address = addr; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; } 
}
