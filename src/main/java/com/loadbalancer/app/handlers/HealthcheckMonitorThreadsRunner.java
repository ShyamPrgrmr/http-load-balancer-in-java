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
	
	
	private ExecutorService service; 

	public HealthcheckMonitorThreadsRunner() {}
	
	public void start() {
		try {
			
			List<AppHTTPUpstream> upstreams = getUpstreams(); 
			int size = upstreams.size(); 
			this.service = Executors.newFixedThreadPool(size);
			
			getUpstreams().stream().forEach(upstream->{
				try {
					map.action(Constants.ADD_UPSTREAM, upstream.getAddress().toString(), true);
					
					System.out.println("Monitor Creates : "+upstream.getAddress().toString()); 
					AppContext context = new AppContext(); 
					APPHTTPUpstreamMonitor monitor = context.returnContext().getBean(APPHTTPUpstreamMonitor.class); 
					monitor.initiate(upstream.getAddress().toString()); 
					this.service.submit(monitor); 
				} catch (MapActionNotAvailable | UpstreamNotAvailable e) {
					logger.error(e);
				} 
			});

		} catch (NoUpstreamAvailableException e) {
			logger.error(e);
		}
	}
	
	private List<AppHTTPUpstream> getUpstreams() throws NoUpstreamAvailableException {
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
