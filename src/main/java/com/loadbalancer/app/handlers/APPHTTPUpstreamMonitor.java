package com.loadbalancer.app.handlers;


import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.apache.logging.log4j.Logger;
import com.loadbalancer.app.enums.Constants;
import com.loadbalancer.app.exceptions.MapActionNotAvailable;
import com.loadbalancer.app.exceptions.UpstreamNotAvailable;
import com.loadbalancer.app.helper.HttpRequestE;
import com.loadbalancer.app.struct.APPHTTPUpstreamHealthCheckMap;


public class APPHTTPUpstreamMonitor implements Runnable {
	
	private Boolean localStatus=true; 
	private String upstream; 
	private int interval;
	private String endpoint; 
	private long timeout; 
	private int initialDelay; 
	private APPHTTPUpstreamHealthCheckMap map; 
	private Logger logger; 
	
	public APPHTTPUpstreamMonitor(APPHTTPUpstreamHealthCheckMap map, String upstream, String endpoint, int interval, long timeout, int initialDelay, Logger logger){
		this.map = map; 
		this.upstream=upstream; 
		this.interval=interval; 
		this.endpoint=endpoint; 
		this.timeout=timeout;
		this.initialDelay=initialDelay; 
		this.logger = logger; 
	}
	
	//to be removed
	public void checker() {
		logger.info("MAP in Thread = "+this.map.hashCode());
		logger.info(this.map.toString()); 
	}
	

	@Override
	public void run() {
		
		//blocking thread for initial delay
		try {
			Thread.sleep(this.initialDelay);
		} catch (NumberFormatException | InterruptedException e) {
			e.printStackTrace();
		}
		
		logger.info("Health check monitor started for ----> " + upstream + endpoint);
		while(true){
			try {
				HttpRequest req = new HttpRequestE(); 		
				HttpRequest.Builder build = req.newBuilder( new URI(upstream+endpoint)).GET().timeout(Duration.ofMillis(this.timeout)); 	
				req = build.build();
				
				
				try {
					HttpResponse<String> res = HttpClient.newBuilder()
							  .build()
							  .send(req, HttpResponse.BodyHandlers.ofString());
					
					if(res.statusCode()>=200 || res.statusCode()<=299) {
						//logger.info("(OUT) "+upstream+endpoint+" "+res.statusCode()); 
						
						//red to green if red
						if(!localStatus) {
							this.localStatus=!this.localStatus; 
							map.action(Constants.UPDATE_UPSTREAM_STATUS, this.upstream, this.localStatus); 
							logger.info(upstream + " is UP. MONITOR STATUS GREEN"); 
						}
					}else {
						
						//green to red if green
						if(localStatus) {
							this.localStatus=!this.localStatus; 
							map.action(Constants.UPDATE_UPSTREAM_STATUS, this.upstream, this.localStatus);
							logger.info(upstream + " is DOWN. MONITOR STATUS RED");
						}
					}	
				}catch(Exception e) {
					if(localStatus) {
						try {
							this.localStatus=!this.localStatus; 
							map.action(Constants.UPDATE_UPSTREAM_STATUS, this.upstream, this.localStatus);
							logger.info(upstream + " is DOWN. MONITOR STATUS RED");
						} catch (MapActionNotAvailable | UpstreamNotAvailable e1) {
							logger.error(e);
							e.printStackTrace();
						}
					}
				}

				Thread.sleep(this.interval);
			} catch (NumberFormatException | InterruptedException | URISyntaxException e) {
				try {
					logger.error(e.getStackTrace());
					if(localStatus) {
						this.localStatus=!this.localStatus; 
						map.action(Constants.UPDATE_UPSTREAM_STATUS, this.upstream, this.localStatus);
						logger.info(upstream + " is DOWN. MONITOR STATUS RED");
					}
					Thread.sleep(this.interval);
				} catch (NumberFormatException | InterruptedException | MapActionNotAvailable | UpstreamNotAvailable e1) {
					logger.error(e.getStackTrace());
				}
			}
		}
	}

}
