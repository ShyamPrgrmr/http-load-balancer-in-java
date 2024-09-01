package com.loadbalancer.app;

import com.loadbalancer.app.enums.Constants;
import com.loadbalancer.app.exceptions.MapActionNotAvailable;
import com.loadbalancer.app.exceptions.UpstreamNotAvailable;
import com.loadbalancer.app.handlers.AcceptorThreadRunner;
import com.loadbalancer.app.handlers.AppHTTPAcceptor;
import com.loadbalancer.app.handlers.HealthcheckMonitorThreadsRunner;
import com.loadbalancer.app.handlers.WorkerThreadRunner;
import com.loadbalancer.app.model.server.AddUpstreamBody;
import com.loadbalancer.app.springboot.Server;
import com.loadbalancer.app.struct.APPHTTPUpstreamHealthCheckMap;
import com.loadbalancer.app.struct.AppHTTPRequestQueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



@SpringBootApplication
@RestController
public class App {
	
	private static APPHTTPUpstreamHealthCheckMap map;  
	
	public static void main(String[] args) {
		AppContext app = new AppContext(); 
		AnnotationConfigApplicationContext context = app.context;
		
		map = context.getBean(APPHTTPUpstreamHealthCheckMap.class); 
		SpringApplication.run(App.class, args);		
		
		int queueSize = (int) Integer.parseInt( context.getEnvironment().getProperty("load.balancer.queue.size")); 
		AppHTTPRequestQueue queue = context.getBean(AppHTTPRequestQueue.class); 		
		queue.intiate(queueSize);	
	
		HealthcheckMonitorThreadsRunner hcm = context.getBean(HealthcheckMonitorThreadsRunner.class); 
		hcm.start();
		
		AcceptorThreadRunner runner = new AcceptorThreadRunner(context.getBean(AppHTTPAcceptor.class)); 
		runner.run();
		
		WorkerThreadRunner wRunner = context.getBean(WorkerThreadRunner.class); 
		wRunner.initiate();
		wRunner.run(); 
	}
	
	@GetMapping("/")
	public String getSpringHello() {
		System.out.print(map.hashCode());
		return "HELLO FROM JAVA LOADBALANCER!";
	}
	
	@PostMapping("/addupstream")
	@ResponseBody
	public ResponseEntity addUpstream(@RequestBody AddUpstreamBody body) {
		String addr = body.getAddress(); 
		try {
			map.action(Constants.ADD_UPSTREAM, addr, true);
		} 
		catch (MapActionNotAvailable e) { e.printStackTrace();} 
		catch (UpstreamNotAvailable e) { e.printStackTrace();} 		
		return new ResponseEntity(HttpStatus.CREATED);
	}
		
	@PostMapping("/removeupstream")
	@ResponseBody
	public ResponseEntity removeUpstream(@RequestBody AddUpstreamBody body) {
		
		String addr = body.getAddress(); 
		try {
			map.action(Constants.REMOVE_UPSTREAM, addr, true);
		} 
		catch (MapActionNotAvailable e) { e.printStackTrace();} 
		catch (UpstreamNotAvailable e) { e.printStackTrace();} 
		
		return new ResponseEntity(HttpStatus.OK);
	}
	
}
