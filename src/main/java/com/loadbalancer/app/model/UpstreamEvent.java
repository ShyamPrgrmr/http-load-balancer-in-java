package com.loadbalancer.app.model;

import com.loadbalancer.app.enums.UpstreamEventType;

public class UpstreamEvent {
	
	private String upstream; 
	private UpstreamEventType upstreamEventType; 
	
	public UpstreamEvent(String upstream, UpstreamEventType upstreamEventType) {
		this.upstream = upstream; 
		this.upstreamEventType = upstreamEventType; 
	}

	public String getUpstream() {
		return upstream;
	}

	public void setUpstream(String upstream) {
		this.upstream = upstream;
	}

	public UpstreamEventType getUpstreamEventType() {
		return upstreamEventType;
	}

	public void setUpstreamEventType(UpstreamEventType upstreamEventType) {
		this.upstreamEventType = upstreamEventType;
	}
	
}
