package com.loadbalancer.app.interfaces;
import java.util.List;
import com.loadbalancer.app.enums.AppLoadBalancerAlgorithms;
import com.loadbalancer.app.model.AppHTTPUpstream;

public interface AppLoadBalancerMethod {
	public AppHTTPUpstream getUpstream(); 
	public void setLoadBalancerAlgorithm();
}
