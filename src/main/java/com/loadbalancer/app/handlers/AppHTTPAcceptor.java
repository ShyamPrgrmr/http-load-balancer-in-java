package com.loadbalancer.app.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.loadbalancer.app.model.AppHTTPBody;
import com.loadbalancer.app.model.AppHTTPHeaders;
import com.loadbalancer.app.model.AppHTTPRequest;
import com.loadbalancer.app.struct.AppHTTPRequestQueue;
import com.loadbalancer.app.enums.AppHTTPMethod;
import com.loadbalancer.app.exceptions.QueueIsFullException;
import com.loadbalancer.app.helper.DataHelper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


import org.apache.logging.log4j.Logger;



@Component
public class AppHTTPAcceptor {
	
	private HttpServer server;
	
	@Value("${load.balancer.port}")
	String port; 
	
	@Value("${load.balancer.context.path}")
	String context_path; 
	
	@Value("${load.balancer.acceptor.threads.count}")
	String thread_count; 
	
	@Value("${load.balancer.request.payload.size}")
	String request_payload_size_s; 
	
	private int request_payload_size;  
	
	@Autowired
	Logger logger; 
	
	@Autowired
	AppHTTPRequestQueue appHTTPRequestQueue; 
	
	
	
	
	public AppHTTPAcceptor(){}

	
	public void initiate() {	
		try {
			
			request_payload_size = DataHelper.StringToInt(request_payload_size_s); 
			int port_int = DataHelper.StringToInt(port);  
			int thread_count_int = DataHelper.StringToInt(thread_count); 
			
			//random = new Random(); 
			
			this.server = HttpServer.create(new InetSocketAddress(port_int), 0);
			this.server.createContext(context_path, new AppHTTPHandler());
			this.server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(thread_count_int));
			
			logger.info("Load Balancer Started on PORT : "+port+" Context Path : "+context_path); 
			
			this.server.start(); 
			
		} catch (IOException e) {
			logger.error(e.getMessage()+"\n"+e.getStackTrace()); 
		}
		 
	}	
	
	private class AppHTTPHandler implements HttpHandler{
		
		private Random random=new Random(); 

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			            
            AppHTTPRequest request = getAppHTTPRequest(exchange); 
            request.setExchange(exchange);
            logger.info(" (IN) sessionId : "+request.getSessionId()+" requestId: "+request.getRequestID()+" url: "+request.getUrl()+" method: "+request.getMethod()+" clientip: "+request.getSenderIP());
            try {
				appHTTPRequestQueue.add(request);
			} catch (QueueIsFullException e) {
				String response = "Unavailable 503 : Resource Exhasted"; 
				logger.error(request.getRequestID()+" : "+e.getMessage()+"\n"+e.getStackTrace());
				request.getExchange().sendResponseHeaders(503, response.length());
	            OutputStream os = request.getExchange().getResponseBody();
				os.write(response.getBytes());
	            os.close();
			} 
		}
		
		
		private AppHTTPRequest getAppHTTPRequest(HttpExchange exchange) {
			
			AppHTTPRequest request = new AppHTTPRequest(); 
			
			AppHTTPBody appHTTPBody = getAppHTTPBody(exchange.getRequestBody());
			if(appHTTPBody!=null) {
				request.setBody(appHTTPBody);
			}
			
			AppHTTPHeaders appHTTPHeaders = getAppHTTTPHeaders(exchange.getRequestHeaders());
			
			if(appHTTPHeaders!=null) {
				request.setHeader(appHTTPHeaders);
			}
			
			request.setMethod(getappHTTPMethod(exchange.getRequestMethod()));
			request.setUrl(exchange.getRequestURI());
			
			request.setProtocol(exchange.getProtocol()); 
			
			HashMap<String, String> map = appHTTPHeaders.getMap(); 
		
			//System.out.println("Contains : "+ map.containsKey("session_id")); 
			
			String request_id = map.containsKey("request_id") ? map.get("request_id") : (System.currentTimeMillis())+""+(this.random.nextGaussian())+"@REQ";  
			String session_id = map.containsKey("session_id") ? map.get("session_id") : DataHelper.randomSessionID();  
			
			if(request_id!=null) {
				request.setRequestID(request_id);
			}
			
			if(session_id!=null) {
				request.setSessionId(session_id);
			}
			
			request.setSenderIP(exchange.getRemoteAddress().toString());
			
			return request; 
		}
		
		
		private AppHTTPMethod getappHTTPMethod(String method) {
			if(method.equalsIgnoreCase("DELETE")) return AppHTTPMethod.DELETE; 
			else if(method.equalsIgnoreCase("GET")) return AppHTTPMethod.GET;
			else if(method.equalsIgnoreCase("POST")) return AppHTTPMethod.POST;
			else if(method.equalsIgnoreCase("PUT")) return AppHTTPMethod.PUT;
			else if(method.equalsIgnoreCase("OPTIONS")) return AppHTTPMethod.OPTIONS;
			else return AppHTTPMethod.UNKNOWN; 
		}
		
		
		private AppHTTPHeaders getAppHTTTPHeaders(Headers head) {
			try {
				Iterator itr = head.entrySet().iterator();
	            ArrayList<String> list = new ArrayList<String>();
            	while(itr.hasNext()) list.add(itr.next().toString().split("=")[0]);
	            
				HashMap<String,String> map = new HashMap(); 
	            list.stream().forEach(item-> map.put( item.toString().toLowerCase(), straightItem(head.get(item).toString()))) ; 
	            return new AppHTTPHeaders(map); 
	            
			}catch(Exception e) {
				logger.error(e.getMessage()+"\n"+e.getStackTrace()); 
			}
			
			return null;
		}
		
		public String straightItem(String item) {return( item.substring(1, item.length()-1));}


		private AppHTTPBody getAppHTTPBody(InputStream inputStream) {

			InputStreamReader isr;
			try {
				isr = new InputStreamReader(inputStream,"utf-8");
			
				BufferedReader br = new BufferedReader(isr);
				
				
				
				int b;
	            StringBuilder buf = new StringBuilder(request_payload_size);
	            while ((b = br.read()) != -1) {
	                buf.append((char) b);
	            }

	            br.close();
	            isr.close();
	            
	            return new AppHTTPBody(buf.toString()); 
			
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage()+"\n"+e.getStackTrace()); 
			} catch (IOException e) {
				logger.error(e.getMessage()+"\n"+e.getStackTrace()); 
			}
			return null; 
		}
		
	}
}
