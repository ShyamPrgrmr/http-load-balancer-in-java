package com.loadbalancer.app.struct;
import java.util.HashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.enums.Constants;
import com.loadbalancer.app.exceptions.MapActionNotAvailable;
import com.loadbalancer.app.exceptions.UpstreamNotAvailable;


/*
 * TRUE = Green
 * FALSE = RED
 * */
@Component
@Scope("singleton")
public class APPHTTPUpstreamHealthCheckMap{
	private HashMap<String, Boolean> map; 
	
	public APPHTTPUpstreamHealthCheckMap() {
		this.map = new HashMap<String,Boolean>();
	}
	
	public synchronized boolean action(Constants action,String upstream, Boolean status) throws MapActionNotAvailable, UpstreamNotAvailable   {
		switch(action) {
			case GET_UPSTREAM_STATUS:{
				if(map.containsKey(upstream)){ 
					return map.get(upstream); 
				}else {	
					throw new UpstreamNotAvailable(upstream); 
				}
			}
			case UPDATE_UPSTREAM_STATUS:{
				if(map.containsKey(upstream)){
					map.put(upstream, status); 
					return true; 
				}else {
					throw new UpstreamNotAvailable(upstream);
				}
			}
			case ADD_UPSTREAM:{
				map.put(upstream, true); 
				return true; 
			}
			default: {
				throw new MapActionNotAvailable(action.toString()); 
			}
		}
	}
}
