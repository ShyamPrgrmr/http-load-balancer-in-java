package com.loadbalancer.app.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppHTTPWorkers {

	@Value("${load.balancer.worker.threads.count}")
	String thread_count; 
	
	
	public AppHTTPWorkers() {
		
	}

}
