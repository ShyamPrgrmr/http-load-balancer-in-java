package com.loadbalancer.app.struct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.exceptions.QueueIsEmptyException;
import com.loadbalancer.app.exceptions.QueueIsFullException;
import com.loadbalancer.app.model.AppHTTPRequest;
import org.apache.logging.log4j.Logger;

@Component
@Scope("singleton")
public class AppHTTPRequestQueue {
	
	@Autowired
	Logger logger; 
	
	private AppHTTPRequest[] queue; 
	private int quantity;
	private int size=0; 
	private int head; 
	private int tail;
	
	public AppHTTPRequestQueue(){
		quantity = 0; 
		head=0; 
		tail=0; 
	}
	
	public void intiate(int size) {
		this.size=size; 
		logger.info("Queue size initiated to : " + this.size );
		queue = new AppHTTPRequest[size];
	}
	
	public synchronized boolean add(AppHTTPRequest request) throws QueueIsFullException  {
		if(quantity == size) {
			logger.error("Queue is full"); 
			throw new QueueIsFullException(size); 
		}
		
		if(head==tail && quantity==0) {
			head=tail=0; 
		}
		
		try {
			queue[tail++] = request; 
			quantity++;			
		}catch(Exception e) {
			logger.info(e.toString());
		}
		
		//logger.info("New request has been added in queue RequestID : " + request.getRequestID());
		return true; 
	}
	
	public synchronized AppHTTPRequest get() throws QueueIsEmptyException {
		if(quantity==0) {
			//logger.error("Queue is empty"); 
			throw new QueueIsEmptyException(); 
		}
		
		if(head==tail) {
			int temp = head; 
			head=tail=quantity=0; 
			//logger.info("Queue pointer set to zero");
			return queue[temp]; 
		}
		
		//logger.info("Request was pulled from queue : " + queue[head].getRequestID());
		quantity--; 
		return queue[head++]; 
	}
}
