package com.loadbalancer.app.handlers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.enums.Constants;
import com.loadbalancer.app.exceptions.MapActionNotAvailable;
import com.loadbalancer.app.exceptions.UpstreamNotAvailable;
import com.loadbalancer.app.helper.HttpRequestE;
import com.loadbalancer.app.struct.APPHTTPUpstreamHealthCheckMap;


@Component
@Scope("prototype")
public class APPHTTPUpstreamMonitor implements Runnable {
	
	private Boolean localStatus=true; 
	
	private String upstream; 
	
	@Value("${load.balancer.healthcheck.scheduler.interval}")
	private String intervalS;
	
	@Value("${load.balancer.healthcheck.endpoint}")
	private String endpoint; 
	
	@Value("${load.balancer.healthcheck.endpoint.timeout}")
	private String timeout; 
	
	@Value("${load.balancer.healthcheck.initial.delay}")
	private String initialDelay; 
	
	@Autowired
	private APPHTTPUpstreamHealthCheckMap map; 
	
	@Autowired 
	Logger logger;
	
	public APPHTTPUpstreamMonitor() {}
	
	public void initiate(String upstream){
		this.upstream=upstream; 
	}

	@Override
	public void run() {
		
		//blocking thread for initial delay
		
		
		
		try {
			Thread.sleep((long) Long.parseLong(initialDelay.trim()));
		} catch (NumberFormatException | InterruptedException e) {
			e.printStackTrace();
		}
		
		logger.info("Health check monitor started for ----> " + upstream + endpoint);
		while(true){
			try {
				HttpRequest req = new HttpRequestE(); 		
				HttpRequest.Builder build = req.newBuilder( new URI(upstream+endpoint)).GET().timeout(Duration.ofMillis((long) Long.parseLong(timeout.trim()))); 	
				req = build.build();
				
				HttpResponse<String> res = HttpClient.newBuilder()
						  .build()
						  .send(req, HttpResponse.BodyHandlers.ofString());
				
				if(res.statusCode()>=200 || res.statusCode()<=299) {
					logger.info("(OUT) "+upstream+endpoint+" "+res.statusCode()); 
					if(!localStatus) {
						this.localStatus=!this.localStatus; 
						map.action(Constants.UPDATE_UPSTREAM_STATUS, this.upstream, this.localStatus); 
						logger.info(upstream + " is UP. MONITOR STATUS GREEN"); 
					}
				}else {
					if(localStatus) {
						this.localStatus=!this.localStatus; 
						map.action(Constants.UPDATE_UPSTREAM_STATUS, this.upstream, this.localStatus);
						logger.info(upstream + " is DOWN. MONITOR STATUS RED");
					}
				}
				
				Thread.sleep((int) Integer.parseInt(intervalS));
			} catch (NumberFormatException | InterruptedException | IOException | URISyntaxException | MapActionNotAvailable | UpstreamNotAvailable e) {
				try {
					e.printStackTrace();
					logger.error(e); 
					if(localStatus) {
						this.localStatus=!this.localStatus; 
						map.action(Constants.UPDATE_UPSTREAM_STATUS, this.upstream, this.localStatus);
						logger.info(upstream + " is DOWN. MONITOR STATUS RED");
					}
					Thread.sleep((int) Integer.parseInt(intervalS));
				} catch (NumberFormatException | InterruptedException | MapActionNotAvailable | UpstreamNotAvailable e1) {
					logger.error(e); 
				}
			}
		}
	}

}
