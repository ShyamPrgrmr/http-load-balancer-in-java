package com.loadbalancer.app.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.exceptions.QueueIsEmptyException;
import com.loadbalancer.app.helper.DataHelper;
import com.loadbalancer.app.interfaces.AppLoadBalancerMethod;
import com.loadbalancer.app.model.AppHTTPRequest;
import com.loadbalancer.app.model.AppHTTPUpstreamRequest;
import com.loadbalancer.app.struct.AppHTTPRequestQueue;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;

@Component
@Scope("singleton")
public class WorkerThreadRunner implements Runnable{

	@Autowired 
	AppHTTPRequestQueue queue; 
	
	@Autowired 
	Logger logger; 
	
	@Autowired
	AppLoadBalancerMethod appLoadBalancerHandler; 

	@Value("${load.balancer.worker.thread.leader.wait.time}")
	String waitTime; 
	
	@Value("${load.balancer.worker.thread.count}")
	String threadCountS; 
	
	@Value("${load.balancer.context.path}")
	String lbContextpath; 
	
	@Value("${load.balancer.upstream.timeout}")
	String timeout; 
	
	private int MILI;
	private int threadCount; 
	private ExecutorService exe; 
	
	
	public WorkerThreadRunner() {}
	
	public void initiate() {
		this.MILI = (int) Integer.parseInt(waitTime.trim());
		this.threadCount = (int) Integer.parseInt(threadCountS.trim());
		this.appLoadBalancerHandler.setLoadBalancerAlgorithm();
		//this.appLoadBalancerHandler.checker();
		this.exe = Executors.newFixedThreadPool(threadCount) ;
		logger.info("Worker Threads initiated with pool size : "+threadCount); 
	}
	

	@Override
	public void run() {
		while(true) {
			try { 
				
				AppHTTPRequest request = queue.get(); 
				this.exe.submit(new AppHTTPWorkerThread(new AppHTTPUpstreamRequest(request, this.appLoadBalancerHandler.getUpstream(request)) , this.lbContextpath, logger,Duration.ofMillis( DataHelper.StringToLong(this.timeout)))); 
			} catch (QueueIsEmptyException e) {
				try {	 
					Thread.sleep(MILI);
				} catch (InterruptedException e1) {
					logger.error(e.toString()); 
				}
			} 
		}
	}

}
