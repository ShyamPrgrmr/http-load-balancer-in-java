package com.loadbalancer.app.handlers;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.loadbalancer.app.helper.DataHelper;
import com.loadbalancer.app.interfaces.AppHashRing;
import com.loadbalancer.app.model.AppHTTPRequest;
import com.loadbalancer.app.model.AppHTTPUpstream;

public class AppHashRingImpl implements AppHashRing {
	
	private int ring[];
	private Map<Integer, AppHTTPUpstream> upstreamMap; 
	
	
	private int ringSize=0; 
	private int stepSize=3; 
	
	private Logger logger; 
	
	public AppHashRingImpl(String ringSizeS, Logger logger) {
		upstreamMap = new HashMap<Integer,AppHTTPUpstream>();
		this.ringSize = DataHelper.StringToInt(ringSizeS); 
		this.ring = new int[this.ringSize];
		this.logger=logger; 
	}
	
	@Override
	public void initialize(String ringSizeS) {
		 
	}
	
	
	public int place(int retry, int hashVal, int fail) {
		
		if(hashVal>=this.ringSize) {
			hashVal = (hashVal-this.ringSize); 
		}
		
		if(fail==1) {
			return Integer.MAX_VALUE; 
		}
		
		if(retry==(this.ringSize)) {
			return place(++retry,++hashVal,1); 
		}
		
		if(this.ring[hashVal]==Integer.MAX_VALUE) {
			return place(++retry, this.stepSize+hashVal ,0); 
		}
		
		return hashVal; 
	}
	
	//put Integer.Max in ring position where upstream is set.  
	//return false if no place is available
	@Override
	public boolean addUpstream(AppHTTPUpstream upstream) {
		String addrs = upstream.getAddress().toString(); 
		int loc = place(0,(DataHelper.hashFunction("", addrs) % this.ringSize),0);  
		if(loc==Integer.MAX_VALUE) {
			return false; 
		}
		logger.info("Upstream : "+upstream.getAddress().toString()+" allocated to "+loc+" in ring. (IPHASH Algorithm)");
		this.ring[loc] = Integer.MAX_VALUE;
		this.upstreamMap.put(loc,upstream); 
		return true;
	}

	@Override
	public boolean removeUpstream(AppHTTPUpstream upstream) {
		return false;
	}

	//after performing hashing on session_id get location returned by hashing function and check next location in ring where Integer.Max is set. 
	@Override
	public AppHTTPUpstream getNext(AppHTTPRequest request) {
		
		String ip = request.getSenderIP(); 
		String session_id = request.getHeader().getMap().get("session_id"); 
		
		int hashVal = DataHelper.hashFunction(ip, session_id) % this.ringSize ; 
		
		//System.out.println("hashVal : "+ hashVal); 
		
		int retries = 0; 
		
		while(true) {
			
			if(ring[hashVal]==Integer.MAX_VALUE) {
				return this.upstreamMap.containsKey(hashVal) ?  this.upstreamMap.get(hashVal) : null;
			}
			
			
			//Fail Safe mechanism -- session will not be consistent
			++hashVal; 
			
			++retries; 
			
			//setting to zero to avoid out of bound
			if(hashVal==this.ringSize) {
				hashVal = 0; 
			}
			
			//if no upstream available -- complete outage
			if(retries == this.ringSize) {
				return null; 
			}
			
		}
		
	}

	@Override
	public int hashing(String str) {
		return 0;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}
	

}
