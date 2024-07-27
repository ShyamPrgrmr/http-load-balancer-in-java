package com.loadbalancer.app.interfaces;
import com.loadbalancer.app.model.AppHTTPRequest;
import com.loadbalancer.app.model.AppHTTPUpstream;

public interface AppLoadBalancerMethod {
	public AppHTTPUpstream getUpstream(AppHTTPRequest request); 
	public void setLoadBalancerAlgorithm();
	public void checker();
}
