package com.loadbalancer.app.interfaces;

import com.loadbalancer.app.model.AppHTTPRequest;
import com.loadbalancer.app.model.AppHTTPUpstream;

public interface AppHashRing {
	
	public boolean addUpstream(AppHTTPUpstream upstream);
	public boolean removeUpstream(String upstream);
	public AppHTTPUpstream getNext(AppHTTPRequest request); 
	public int hashing(String sessionID); 
	public void initialize();
	void initialize(String ringSizeS); 
	
}
