package com.loadbalancer.app.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URI;

import java.net.URISyntaxException;

import java.net.http.HttpHeaders;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import com.loadbalancer.app.enums.AppHTTPMethod;
import com.loadbalancer.app.helper.HttpRequestE;
import com.loadbalancer.app.model.AppHTTPRequest;
import com.loadbalancer.app.model.AppHTTPUpstream;
import com.loadbalancer.app.model.AppHTTPUpstreamRequest;


public class AppHTTPWorkerThread implements Runnable {

	Logger logger; 
	
	String lbContextpath; 
	
	int content_length=0;
	int counter = 0; 
	
	HttpRequest.Builder build; 
	
	private AppHTTPUpstreamRequest request; 
	
	public AppHTTPWorkerThread(AppHTTPUpstreamRequest request, String contextPath, Logger logger){
		this.request=request; 
		this.lbContextpath=contextPath; 
		this.logger = logger; 
	}
	

	@Override
	public void run() {
		
		AppHTTPRequest a_request = request.getRequest(); 
		AppHTTPUpstream upstream = request.getUpstream();
		
		try {
			//public URI(String scheme, String host, String path, String fragment)
			
			URI url = new URI(upstream.getAddress()+ a_request.getUrl().toString().substring(lbContextpath.trim().length()-1));
			
			HashMap<String, String> map = a_request.getHeader().getMap();			
			HttpRequest req = new HttpRequestE(); 		
			build = req.newBuilder(url); 
			
			map.entrySet().stream().forEach(item->{
				try {
					
					if(item.getKey().equals("Content-length")){
						content_length =   (int) Integer.parseInt(item.getValue()) ; 
					}
					build = build.setHeader(item.getKey(), item.getValue()); 
				}catch(IllegalArgumentException IAE) {
					//logger.info(item+ " Was Ignored"); 
				}
			});
		
			
			build = build.setHeader("request_id", a_request.getRequestID());
			
			build = getBuilderMethod(build, a_request);
			
			//build = build.version(a_request.getProtocol())  //backlog
			
			req = build.build();
			
			//System.out.println(HttpRequest.BodyPublishers.ofString("hellow")); 
			
			HttpResponse<String> res = HttpClient.newBuilder()
					  .build()
					  .send(req, HttpResponse.BodyHandlers.ofString());
			
			HttpHeaders res_heders = res.headers(); 
			
			 
			if(res.statusCode()<399)
			{
				try {
					
					String response = res.body(); 
					a_request.getExchange().sendResponseHeaders(200, response.length());	
					OutputStream os = a_request.getExchange().getResponseBody();
					os.write(response.getBytes());
					logger.info(" (OUT) "+a_request.getRequestID()+" : "+a_request.getMethod()+" "+url+" "+a_request.getProtocol()+" "+res.statusCode());
			        os.close();	
			        a_request.getExchange().close();
			        
				} catch (IOException e) {
					logger.error(e.toString());
				}
			
			}else {
				logger.info(" (OUT) "+a_request.getRequestID()+" : "+a_request.getMethod()+" "+url+" "+a_request.getProtocol()+" "+res.statusCode());
				a_request.getExchange().sendResponseHeaders(res.statusCode(), 0);
				a_request.getExchange().close();
			}
			
			
			
		} catch (URISyntaxException e) {
			logger.error(e.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	
	}
	
	
	HttpRequest.Builder getBuilderMethod(HttpRequest.Builder build, AppHTTPRequest a_request){
		
		AppHTTPMethod e = a_request.getMethod(); 
		String body = a_request.getBody().getBody(); 
		
		
		switch(e){
		case POST: {
			build.POST(HttpRequest.BodyPublishers.ofString(body));
			return build; 
		} 
		case GET: {
			build.GET();
			return build; 
		}
		case PUT: {
			build.PUT(HttpRequest.BodyPublishers.ofString(body)); 
			return build; 
		}
		case DELETE: {
			build.DELETE(); 
			return build; 
		} 
		default: {
			build.GET();
			return build;
		} 
	}
		

	}
	
	
	public String getMethod(AppHTTPMethod e) {
		switch(e){
			case POST: return "POST"; 
			case GET: return "GET"; 
			case PUT: return "PUT";
			case DELETE: return "DELETE";
			case OPTIONS: return "OPTION"; 
			default: return "GET"; 
		}
	}
	
}
