package com.loadbalancer.app.handlers;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.AppContext;
import com.loadbalancer.app.enums.Constants;
import com.loadbalancer.app.exceptions.MapActionNotAvailable;
import com.loadbalancer.app.exceptions.NoUpstreamAvailableException;
import com.loadbalancer.app.exceptions.UpstreamNotAvailable;
import com.loadbalancer.app.helper.DataHelper;
import com.loadbalancer.app.model.AppHTTPUpstream;
import com.loadbalancer.app.model.UpstreamEvent;
import com.loadbalancer.app.struct.APPHTTPUpstreamHealthCheckMap;

@Component
public class HealthcheckMonitorThreadsRunner implements Subscriber<UpstreamEvent> {
	
	@Value("${load.balancer.upstream.server.list}")
	String upstream_list;
	
	@Autowired 
	Logger logger;
	
	@Autowired 
	APPHTTPUpstreamHealthCheckMap map; 
	
	@Value("${load.balancer.healthcheck.scheduler.interval}")
	private String intervalS;
	
	@Value("${load.balancer.healthcheck.endpoint}")
	private String endpoint; 
	
	@Value("${load.balancer.healthcheck.endpoint.timeout}")
	private String timeout; 
	
	@Value("${load.balancer.healthcheck.initial.delay}")
	private String initialDelay; 
	
	@Value("${load.balancer.max.monitor.count}")
	private String maxMonitorCount; 
	
	
	private int count = 0; 
	private ExecutorService service; 
	
	
	boolean initiated=false;

	private Subscription subscription; 
	
	public HealthcheckMonitorThreadsRunner() {}
	
	public int getCountOfupstream() {
		return count; 
	}
	
	public void addUpstream(String newUpstream) {
		
		AppHTTPUpstream upstream=null;
		try {
			upstream = new AppHTTPUpstream(newUpstream);
		} catch (MalformedURLException e) {

			e.printStackTrace();
		} 
		
		APPHTTPUpstreamMonitor monitor = new APPHTTPUpstreamMonitor(this.map, upstream.getAddress().toString(),
				this.endpoint, 
				DataHelper.StringToInt(intervalS), 
				DataHelper.StringToLong(timeout), 
				DataHelper.StringToInt(initialDelay),
				this.logger); 
		
		this.service.submit(monitor); 
		logger.info("Health check monitor created for : "+upstream.getAddress().toString()); 
		
		count++;
		//reInitiate(); 
	}
	
	
	//not sure how to search for thread which is running monitor for specific upstream. Will not implement this for now. 
	public void removeUpstream(String upstream) {
	
	}
	
	//autoscalling
	//Heavy lifting as halting all threads and creating new monitors for all upstreams
	public void reInitiate() {
		//logger.info("Reintiating healthcheck monitors");
		//this.service.shutdownNow();
		//intiateMonitors(); 
	}
	
	
	private void intiateMonitors(){
		try {
			List<AppHTTPUpstream> upstreams = getUpstreams(); 
			int size = DataHelper.StringToInt(maxMonitorCount); 
			
			this.service = Executors.newFixedThreadPool(size);
			if(!this.initiated) this.map.action(Constants.INITIATE_HC_MAP, upstream_list,true);
			
			upstreams.stream().forEach(upstream->{
				try {
					
					//Action_Type, Upstream Address, Health --> default true
					this.map.action(Constants.ADD_UPSTREAM, upstream.getAddress().toString(), true);
					
					
					APPHTTPUpstreamMonitor monitor = new APPHTTPUpstreamMonitor(this.map, upstream.getAddress().toString(),
																				this.endpoint, 
																				DataHelper.StringToInt(intervalS), 
																				DataHelper.StringToLong(timeout), 
																				DataHelper.StringToInt(initialDelay),
																				this.logger); 
					
					this.service.submit(monitor); 
					logger.info("Health check monitor created for : "+upstream.getAddress().toString()); 
					
				} catch (MapActionNotAvailable | UpstreamNotAvailable e) {
					logger.error(e);
				} 
			});
		} catch (NoUpstreamAvailableException e) {
			logger.error(e);
		} catch (MapActionNotAvailable e1) {
			e1.printStackTrace();
		} catch (UpstreamNotAvailable e1) {
			e1.printStackTrace();
		}
	}
	
	
	
	
	public void start() {
		intiateMonitors(); 
		subscribeToPublisher();
		this.initiated=true; 
	}
	
	private List<AppHTTPUpstream> getUpstreams() throws NoUpstreamAvailableException {
		if(upstream_list== null) {
			throw new NoUpstreamAvailableException(); 
		}
		this.count=1; 
		
		System.out.println(this.upstream_list); 
		
		List<AppHTTPUpstream> list =  Arrays.asList(( this.upstream_list.split(","))).stream().map(item->   {
			try {
				count++;
				return new AppHTTPUpstream(item.trim());
			} catch (MalformedURLException e) {
				logger.error(e); 
			}
			return null;
		}).collect(Collectors.toList());   
		return list;
	}

	
	/*Pub Sub logic Start*/
	
	private void subscribeToPublisher() {
		if(!this.initiated)
		{
			this.map.getPublisher().subscribe(this);
			
		}
	}
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		//System.out.println("Subscribed");
		subscription.request(2);
	}

	@Override
	public void onNext(UpstreamEvent item) {
		switch(item.getUpstreamEventType()) {
			case UPSTREAM_ADDED: {
				//System.out.println("Request Received");
				this.addUpstream(item.getUpstream());
				subscription.request(2);
				break; 
			}
			case UPSTREAM_REMOVED:{
				this.removeUpstream(item.getUpstream());
				subscription.request(2);
				break; 
			}
			default:{
				break; 
			}
		}
	}

	@Override
	public void onError(Throwable throwable) {
		this.logger.error(throwable);
	}

	@Override
	public void onComplete() {}
	/*Pub Sub logic Start*/
}
