package com.loadbalancer.app.handlers;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.enums.AppLoadBalancerAlgorithms;
import com.loadbalancer.app.exceptions.NoUpstreamAvailableException;
import com.loadbalancer.app.interfaces.AppLoadBalancerMethod;
import com.loadbalancer.app.model.AppHTTPUpstream;


@Component
public class AppLoadBalancerMethodImpl implements AppLoadBalancerMethod {
	
	private AppLoadBalancerAlgorithms appLoadBalancerAlgorithm;
	private List<AppHTTPUpstream> upstreams;
	private int roundsRobinIndex = 0; 
	private int upstreamListlength = 0; 
	
	@Value("${load.balancer.upstream.server.list}")
	String upstream_list;
	
	@Value("${load.balancer.algorithm}")
	String algo; 
	
	@Autowired 
	Logger logger;
	
	public AppLoadBalancerMethodImpl() {}

	@Override
	public AppHTTPUpstream getUpstream() {
		if(this.appLoadBalancerAlgorithm.equals(AppLoadBalancerAlgorithms.ROUNDS_ROBIN)) {
			int index = this.getRounRobinIndex(); 
			return this.upstreams.get(index); 
		}
		return this.upstreams.get(0); //default first server
	}
	
	
	private int getRounRobinIndex() {
		if(this.roundsRobinIndex==this.upstreamListlength) {
			this.roundsRobinIndex = 0; 
		}
		++this.roundsRobinIndex; 
		return (this.roundsRobinIndex-1); 
	}
	

	@Override
	public void setLoadBalancerAlgorithm() {
		this.appLoadBalancerAlgorithm = getAlgrithm(); 
		
		try {
			this.upstreams = getUpstreams();
			this.upstreamListlength = this.upstreams.size(); 
		} catch (NoUpstreamAvailableException e) {
			this.upstreamListlength=0;  
			logger.error(e);
		} 
		
		
		logger.info("List of upstreams : "+this.upstreams + ", Selected load balancing algorithm : "+this.appLoadBalancerAlgorithm);
	}
	
	public AppLoadBalancerAlgorithms getAlgrithm() {
		try {
			return Arrays.asList(AppLoadBalancerAlgorithms.values()).stream().filter( item -> { return item.toString().equals(algo.trim()); }).collect(Collectors.toList()).get(0); 
		}catch(IndexOutOfBoundsException e){
			logger.warn("Selected Load Balancing Algorithm "+algo.trim()+" is not present, using default - "+AppLoadBalancerAlgorithms.ROUNDS_ROBIN);
			return AppLoadBalancerAlgorithms.ROUNDS_ROBIN;
		}
	}
	
	public List<AppHTTPUpstream> getUpstreams() throws NoUpstreamAvailableException {
		
		if(upstream_list== null) {
			throw new NoUpstreamAvailableException(); 
		}
		
		List<AppHTTPUpstream> list =  Arrays.asList(( this.upstream_list.split(","))).stream().map(item->   {
			try {
				return new AppHTTPUpstream(item.trim());
			} catch (MalformedURLException e) {
				logger.error(e); 
			}
			return null;
		}).collect(Collectors.toList());   
		return list;
	}

}
