package com.loadbalancer.app.handlers;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.enums.AppLoadBalancerAlgorithms;
import com.loadbalancer.app.exceptions.IssueWhileAddingInIPHash;
import com.loadbalancer.app.exceptions.ListHasNullValues;
import com.loadbalancer.app.exceptions.NoUpstreamAvailableException;
import com.loadbalancer.app.exceptions.PercentageIsnotMultipleOf25OrMoreThanHundreadOrAddtionIsNot100;
import com.loadbalancer.app.exceptions.WeightedRoundsRobinPercentageNotMatchingWithList;
import com.loadbalancer.app.helper.DataHelper;
import com.loadbalancer.app.interfaces.AppLoadBalancerMethod;
import com.loadbalancer.app.model.AppHTTPRequest;
import com.loadbalancer.app.model.AppHTTPUpstream;
import com.loadbalancer.app.model.UpstreamEvent;
import com.loadbalancer.app.struct.APPHTTPUpstreamHealthCheckMap;


@Component
public class AppLoadBalancerMethodImpl implements AppLoadBalancerMethod, Subscriber<UpstreamEvent> {
	
	private AppLoadBalancerAlgorithms appLoadBalancerAlgorithm;
	private List<AppHTTPUpstream> upstreams;
	private List<AppHTTPUpstream> backupUpstreams;
	private int roundsRobinIndex = 0; 
	private int upstreamListlength = 0; 
	private int backupUpstreamListLength = 0; 
	
	private Subscription subscription;
	
	@Value("${load.balancer.upstream.server.list}")
	String upstream_list;
	
	@Value("${load.balancer.algorithm}")
	String algo; 
	
	@Autowired 
	Logger logger;
	
	@Value("${load.balancer.algorithm.weightedroundrobin.percentage}")
	String weightedRRSplitPercentages; 
	
	@Value("${load.balancer.upstream.backup.server.list}")
	String upstream_backup_list;  
	
	@Value("${load.balancer.algorithm.hashring.length}")
	private String ringSizeS;
	
	@Value("${load.balancer.algorithm.hashring.failover.if.upstream.failing}")
	private String ipHashFailover; 
	
	private AppHashRingImpl ipHash; 
	
	@Autowired
	APPHTTPUpstreamHealthCheckMap hcMap;
	
	private HashMap<String, Boolean> localHcMap;
	
	private boolean initiated = false; 
	
	private Random random; 
	
	public AppLoadBalancerMethodImpl() {
		this.localHcMap = new HashMap<String, Boolean>(); 
		this.random = new Random(); 
	}

	public void checker() {
		logger.info("MAP in Load Balancer Method = "+this.hcMap.hashCode());
	}
	
	
	/* Load balancing Logic Start */
	
	private boolean getUpstreamStatus(String upstream) {
		return this.localHcMap.containsKey(upstream) ? this.localHcMap.get(upstream) : false;  
	}
	
	
	
	//call to get next upstream for RR or WRR or failover 
	//pass true for random algorithm 
	private AppHTTPUpstream failoverORRR(int in,boolean check) {
		int retries=0; 
		int index =  !check ? this.getRounRobinIndex() : in; 
		String prev= ""; 
		while(true) {
			
			if(retries==this.upstreamListlength) {
				
				if(this.backupUpstreamListLength==0) {
					logger.info("***None of the upstreams are available***");
					return this.upstreams.get(index);
				}	
				else {
					AppHTTPUpstream up = this.backupUpstreams.get(this.random.nextInt(0, this.backupUpstreamListLength));
					logger.info("***None of the upstreams are available, trying random backup upstreams ---> "+up.getAddress()+"***");
					return up; 
				}
			}
			
			if( this.getUpstreamStatus(this.upstreams.get(index).getAddress().toString()) ) {
				prev=this.upstreams.get(index).getAddress().toString();
				break; 
			}else {
				retries++;
				if(!prev.equals( this.upstreams.get(index).getAddress().toString()))
					logger.info("Upstream "+this.upstreams.get(index).getAddress().toString()+" is down, trying next available upstream");
				prev=this.upstreams.get(index).getAddress().toString();
				index=this.getRounRobinIndex(); 
			}
		}
		return this.upstreams.get(index); 
	}
	
	
	

	//this method should not return null 
	@Override
	public AppHTTPUpstream getUpstream(AppHTTPRequest request) {
		if(this.appLoadBalancerAlgorithm.equals(AppLoadBalancerAlgorithms.ROUNDS_ROBIN) || this.appLoadBalancerAlgorithm.equals(AppLoadBalancerAlgorithms.WEIGHTED_ROUNDS_ROBIN) ) {
			return failoverORRR(0,false); 
		}
		else if(this.appLoadBalancerAlgorithm.equals(AppLoadBalancerAlgorithms.RANDOM)) {
			int index = this.random.nextInt(0, this.upstreamListlength); 
			return failoverORRR(index,true);
		}	
		else if(this.appLoadBalancerAlgorithm.equals(AppLoadBalancerAlgorithms.IP_HASHING_AUTOSCALLING)) {
			AppHTTPUpstream upstream = this.ipHash.getNext(request); 
			
			
			//upstream is not null and status is green
			if(upstream!=null && this.getUpstreamStatus(upstream.getAddress().toString()) ) {
				return upstream; 
			}
			//upstream is not null and upstream status red and failover flag is true
			else if(upstream!=null && DataHelper.StringToBoolean(ipHashFailover)) {
				return failoverORRR(0,false); 
			}
			//upstream is not null and failover is false
			else if(upstream!=null && !DataHelper.StringToBoolean(ipHashFailover)) {
				return this.upstreams.get(0);
			}
			//upstream is null and failover is true
			else if(upstream==null && DataHelper.StringToBoolean(ipHashFailover)) {
				return failoverORRR(0,false); 
			}
			//upstream is null and failover is false
			else if(upstream==null && !DataHelper.StringToBoolean(ipHashFailover)) {
				return this.upstreams.get(0); 
			}
 		}
		
		return this.upstreams.get(0); //default first server
	}
	
	
	private int getRounRobinIndex() {
		if(this.roundsRobinIndex==this.upstreamListlength) {
			this.roundsRobinIndex = 0; 
		}
		++this.roundsRobinIndex; 
		return (this.roundsRobinIndex-1); 
	}
	
	private void subscribeToPublisher() {
		if(!this.initiated)
		{
			this.hcMap.getPublisher().subscribe(this);
		}
	}
	
	
	
	
	/* Load balancing Logic End */
	
	
	/*Initialization Logic Start*/
	
	private void initializeAlgorithms() {
		
		switch(this.appLoadBalancerAlgorithm){
			case ROUNDS_ROBIN:{
				roundsRobin(); 
				break; 
			}
			case WEIGHTED_ROUNDS_ROBIN:{
				weightedRoundsRobin();
				break; 
			}
			case RANDOM:{
				roundsRobin(); 
				break;
			}
			case IP_HASHING_AUTOSCALLING:{
				this.ipHash = new AppHashRingImpl(this.ringSizeS, logger);
				intializeIPHash();
				
				//for failover
				roundsRobin(); 
			}
			default:break;
		}
		
		initializeBackupServers(); 
	}
	
	
	private void intializeIPHash() {
		try {
			DataHelper.getUpstreams(upstream_list, ",").stream().forEach(item->{if(!this.ipHash.addUpstream(item)) {logger.error("Issue while adding "+item.getAddress()+" in hashmap. (IPHASH Algorithm)");}});
		} catch (NoUpstreamAvailableException | ListHasNullValues e) {
			logger.error(e);
		} 
	}
	
	
	private void initializeBackupServers() {
		if(this.upstream_backup_list==null) {
			this.backupUpstreamListLength = 0; 
		} else {
			try {
				this.backupUpstreams = DataHelper.getUpstreams(upstream_backup_list, ",");
				this.backupUpstreamListLength = this.backupUpstreams.size(); 
			} catch (NoUpstreamAvailableException | ListHasNullValues e) {
				logger.error(e);
			} 
		}
	}
	
	
	//Aim is to create list from weighted rounds robin and by using rounds robin method to return upstream to getUpstream method. 
	//using percentage like 75% 20%
	//we need to get exact length of upstream list and count = length*percentage/100, where count is number of time we are going add it. 
	//we can't have upstream with less than 25%. -- there should be some restriction to add 0%,25%,50%,75%,100% 
	//this method will check and set this.upstreams with weighted upstream list
	private void weightedRoundsRobin() {
		try {
			List<AppHTTPUpstream> list = DataHelper.getUpstreams(upstream_list, ",");
			List<Integer> weights = DataHelper.stringListSpliterToINTList(this.weightedRRSplitPercentages, ","); 
			int count = 0; 
			if(list.size()<=weights.size()) {
				
				if(this.upstreams==null) this.upstreams = new ArrayList<AppHTTPUpstream>(); 
				else if(this.upstreams.size()==0) this.upstreams.clear();
				
				int addition = 0; 
				for(int weight : weights) {
					addition += weight;
				} 
				if(addition>100 || addition<100) throw new PercentageIsnotMultipleOf25OrMoreThanHundreadOrAddtionIsNot100(); 
				
				int size = list.size(); 
				
				for(AppHTTPUpstream ups : list) {
					
					int cnt = ((size*size)*(weights.get(count)))  /100; 
					
					while(cnt!=0){
						this.upstreams.add(ups); 
						cnt--; 
					}
					count++; 
				}
				
				
				
			}else {
				throw new WeightedRoundsRobinPercentageNotMatchingWithList(this.upstream_list, this.weightedRRSplitPercentages, list.size(), weights.size()); 
			}
		} catch (NoUpstreamAvailableException | ListHasNullValues | WeightedRoundsRobinPercentageNotMatchingWithList | PercentageIsnotMultipleOf25OrMoreThanHundreadOrAddtionIsNot100 e) {
			logger.error(e);
		}  
	}
	
	
	private void roundsRobin() {
		try {
			this.upstreams = getUpstreams(); 
		} catch (NoUpstreamAvailableException e) {
			this.upstreamListlength=0;  
			logger.error(e);
		} 
	}

	
	//for setting algorithm and upstream list
	@Override
	public void setLoadBalancerAlgorithm() {
		this.localHcMap = this.hcMap.getHashMap(); 
		subscribeToPublisher(); 
		initiated=true; 
		this.appLoadBalancerAlgorithm = getAlgrithm();
		
		logger.info("Selected load balancing algorithm : "+this.appLoadBalancerAlgorithm);
		initializeAlgorithms(); 
		this.upstreamListlength = this.upstreams.size();
		
	}
	
	private AppLoadBalancerAlgorithms getAlgrithm() {
		try {
			return Arrays.asList(AppLoadBalancerAlgorithms.values()).stream().filter( item -> { return item.toString().equals(algo.trim()); }).collect(Collectors.toList()).get(0); 
		}catch(IndexOutOfBoundsException e){
			logger.warn("Selected Load Balancing Algorithm "+algo.trim()+" is not present, using default - "+AppLoadBalancerAlgorithms.ROUNDS_ROBIN);
			return AppLoadBalancerAlgorithms.ROUNDS_ROBIN;
		}
	}
	
	
	private List<AppHTTPUpstream> getUpstreams() throws NoUpstreamAvailableException {
		
		this.upstreamListlength = 1; 
		
		if(upstream_list== null) {
			throw new NoUpstreamAvailableException(); 
		}
		
		List<AppHTTPUpstream> list =  Arrays.asList(( this.upstream_list.split(","))).stream().map(item->   {
			try {
				AppHTTPUpstream temp = new AppHTTPUpstream(item.trim());
				this.upstreamListlength++; 
				return temp; 
			} catch (MalformedURLException e) {
				logger.error(e); 
			}
			return null;
		}).collect(Collectors.toList());   
		return list;
	}
	
	/*Initialization Logic End*/	
	
	
	/*Autoscaling and pubsub logic start*/	
	
	
	//autoscaling
	private void addUpstream(String newUpstream) {
		this.upstream_list = "," + newUpstream;
		upstreamListlength++;
		reInitiate(); 
	}
	
	//autoscaling
	private void removeUpstream(String upstream) {
		List<String> list =  Arrays.asList(this.upstream_list.split(",")).stream().filter(up ->{
			return up.equalsIgnoreCase(upstream); 
		}).collect(Collectors.toList()); 
		this.upstream_list = "";  
		this.upstreamListlength=1; 
		list.forEach(item->{
			if(upstreamListlength==1) this.upstream_list+=item; 
			else this.upstream_list=","+item;
			upstreamListlength++; 
		});
		reInitiate(); 
	}
	
	//autoscalling
	private void reInitiate() {
		setLoadBalancerAlgorithm(); 
	}
	

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
        subscription.request(1);
	}
	
	@Override
	public void onNext(UpstreamEvent item) {
		switch(item.getUpstreamEventType()){
			case UPSTREAM_HC_CHANGED:{
				this.healthStatusChanged(item);
				subscription.request(1);
				return;
			}
			case UPSTREAM_REMOVED:{
				this.removeUpstream(item.getUpstream());
				subscription.request(1);
				return;
			}
			case UPSTREAM_ADDED:{
				this.addUpstream(item.getUpstream()); 
				subscription.request(1);
				return; 
			}
		}
	}

	@Override
	public void onError(Throwable throwable) {
		logger.error(throwable); 
	}

	@Override
	public void onComplete() {}
	
	
	private void healthStatusChanged(UpstreamEvent item) {
		this.localHcMap = this.hcMap.getHashMap(); 
		String status = this.localHcMap.get(item.getUpstream())? "UP" : "DOWN";  
		//logger.info("Upstream : "+item.getUpstream()+" is "+status+" for taking a traffic");
	}

	
	/*Autoscaling and pubsub logic start*/		
}
