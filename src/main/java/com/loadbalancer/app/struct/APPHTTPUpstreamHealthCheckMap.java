package com.loadbalancer.app.struct;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.SubmissionPublisher;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.enums.Constants;
import com.loadbalancer.app.enums.UpstreamEventType;
import com.loadbalancer.app.exceptions.MapActionNotAvailable;
import com.loadbalancer.app.exceptions.UpstreamNotAvailable;
import com.loadbalancer.app.model.UpstreamEvent;


@Component
@Scope("singleton")
public class APPHTTPUpstreamHealthCheckMap  {
	private HashMap<String, Boolean> map; 
	private SubmissionPublisher<UpstreamEvent> publisher;
	
	public APPHTTPUpstreamHealthCheckMap() {
		//this.map = new HashMap<String,Boolean>();
	}
	
	public String toString() {
		return map.toString(); 
	}
	
	public SubmissionPublisher<UpstreamEvent> getPublisher(){
		return this.publisher; 
	}
	
	public synchronized HashMap<String, Boolean> getHashMap() {
		return this.map; 
	}
	
	
	public synchronized boolean action(Constants action,String upstream, Boolean status) throws MapActionNotAvailable, UpstreamNotAvailable   {
		switch(action) {
			case INITIATE_HC_MAP:{
				this.map = new HashMap<String,Boolean>();
				this.publisher = new SubmissionPublisher<>();
				return true; 
			}
			case GET_UPSTREAM_STATUS:{
				if(map.containsKey(upstream)){ 
					return map.get(upstream); 
				}else {	
					throw new UpstreamNotAvailable(upstream); 
				}
			}
			case ADD_UPSTREAM:{
				this.map.put(upstream, status); 
				this.publisher.submit(new UpstreamEvent(upstream, UpstreamEventType.UPSTREAM_ADDED));
				return true; 
			}
			case UPDATE_UPSTREAM_STATUS:{
				if(map.containsKey(upstream)){
					map.put(upstream, status); 
					this.publisher.submit(new UpstreamEvent(upstream, UpstreamEventType.UPSTREAM_HC_CHANGED)); 
					return true; 
				}else {
					throw new UpstreamNotAvailable(upstream);
				}
			}
			case REMOVE_UPSTREAM:{
				map.remove(upstream);
				this.publisher.submit(new UpstreamEvent(upstream, UpstreamEventType.UPSTREAM_REMOVED)); 
				return true; 
			}
			default: {
				throw new MapActionNotAvailable(action.toString()); 
			}
		}
	}
}
