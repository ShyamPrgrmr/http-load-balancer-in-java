package com.loadbalancer.app.handlers;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.loadbalancer.app.struct.APPHTTPUpstreamHealthCheckMap;

@Component
public class HealthcheckMonitorThreadsRunner {
	
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
	
	private int count = 0; 
	private ExecutorService service; 

	public HealthcheckMonitorThreadsRunner() {}
	
	public int getCountOfupstream() {
		return count; 
	}
	
	public void addUpstream(String newUpstream) {
		this.upstream_list = "," + newUpstream;
		count++;
		reInitiate(); 
	}
	
	public void removeUpstream(String upstream) {
		List<String> list =  Arrays.asList(this.upstream_list.split(",")).stream().filter(up ->{
			return up.equalsIgnoreCase(upstream); 
		}).collect(Collectors.toList()); 
		
		this.upstream_list = "";  
		
		
		this.count=1; 
		list.forEach(item->{
			if(count==1) this.upstream_list+=item; 
			else this.upstream_list=","+item;
			count++; 
		});
		
		reInitiate(); 
		
	}
	
	//autoscalling
	public void reInitiate() {
		this.service.shutdownNow();
		start(); 
	}
	
	public void start() {
		try {
			
			List<AppHTTPUpstream> upstreams = getUpstreams(); 
			int size = upstreams.size(); 
			this.service = Executors.newFixedThreadPool(size);
			this.map.action(Constants.INITIATE_HC_MAP, upstream_list, true);
			
			getUpstreams().stream().forEach(upstream->{
				try {
					
					this.map.action(Constants.ADD_UPSTREAM, upstream.getAddress().toString(), true);
					logger.info("Health check monitor created for : "+upstream.getAddress().toString()); 
					//logger.info("MAP in Runner = "+this.map.hashCode());
					
					APPHTTPUpstreamMonitor monitor = new APPHTTPUpstreamMonitor(this.map, upstream.getAddress().toString(),
																				this.endpoint, 
																				DataHelper.StringToInt(intervalS), 
																				DataHelper.StringToLong(timeout), 
																				DataHelper.StringToInt(initialDelay),
																				this.logger); 
					
					this.service.submit(monitor); 
					
					
					
					//monitor.checker(); 
					
				} catch (MapActionNotAvailable | UpstreamNotAvailable e) {
					logger.error(e);
				} 
			});
			
			//logger.info("Current Upstream Status : "+ this.map.toString()); 

		} catch (NoUpstreamAvailableException e) {
			logger.error(e);
		} catch (MapActionNotAvailable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UpstreamNotAvailable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private List<AppHTTPUpstream> getUpstreams() throws NoUpstreamAvailableException {
		if(upstream_list== null) {
			throw new NoUpstreamAvailableException(); 
		}
		this.count=1; 
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
}
