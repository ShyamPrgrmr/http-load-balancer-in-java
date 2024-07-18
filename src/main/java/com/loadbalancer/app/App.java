package com.loadbalancer.app;

import com.loadbalancer.app.handlers.AcceptorThreadRunner;
import com.loadbalancer.app.handlers.AppHTTPAcceptor;
import com.loadbalancer.app.handlers.AppHTTPRequestQueue;
import com.loadbalancer.app.handlers.WorkerThreadRunner;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

	public static void main(String[] args) {
		AppContext app = new AppContext(); 
		AnnotationConfigApplicationContext context = app.context;
		int queueSize = (int) Integer.parseInt( context.getEnvironment().getProperty("load.balancer.queue.size")); 
		AppHTTPRequestQueue queue = context.getBean(AppHTTPRequestQueue.class); 		
		queue.intiate(queueSize);	
	
		
		AcceptorThreadRunner runner = new AcceptorThreadRunner(context.getBean(AppHTTPAcceptor.class)); 
		runner.run();
		
		
		WorkerThreadRunner wRunner = context.getBean(WorkerThreadRunner.class); 
		wRunner.initiate();
		wRunner.run(); 
		
	}

}
