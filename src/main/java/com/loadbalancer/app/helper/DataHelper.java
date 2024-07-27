package com.loadbalancer.app.helper;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.loadbalancer.app.exceptions.ListHasNullValues;
import com.loadbalancer.app.exceptions.NoUpstreamAvailableException;
import com.loadbalancer.app.model.AppHTTPUpstream;


public class DataHelper {
	
	private static final Random random = new Random(); 

	public static int StringToInt(String s) {
		return (int) Integer.parseInt(s); 
	}
	
	public static long StringToLong(String s) {
		return (long) Long.parseLong(s); 
	}
	
	public static boolean StringToBoolean(String s) {
		return (boolean) Boolean.parseBoolean(s); 
	}
	
	public static List<String> stringListSpliterToList(String str, String separator){
		return Arrays.asList((str.split(separator))); 
	}
	
	public static List<Integer> stringListSpliterToINTList(String str, String separator){
		return Arrays.asList((str.split(separator))).stream().map(item->{
			item = item.replace("%", "").trim();
			return Integer.parseInt(item);
		}).collect(Collectors.toList()); 
	}
	
	public static <T> boolean checkNullInList(List<T> list)  {
		return list.stream().anyMatch(item-> item==null); 
	}
	
	
	public static List<AppHTTPUpstream> getUpstreams(String upstream_list, String separator) throws NoUpstreamAvailableException, ListHasNullValues {
		if(upstream_list== null) {
			throw new NoUpstreamAvailableException(); 
		}
		
		List<AppHTTPUpstream> list = DataHelper.stringListSpliterToList(upstream_list, separator).stream().map(item->   {
			try {
				AppHTTPUpstream temp = new AppHTTPUpstream(item.trim());
				return temp; 
			} catch (MalformedURLException e) {
				e.printStackTrace(); 
			}
			return null;
		}).collect(Collectors.toList()); 
				
		if (DataHelper.checkNullInList(list)) throw new ListHasNullValues(upstream_list); 
		else return list;
	}
	
	
	public static String randomSessionID() { 
		return DataHelper.getSaltString();
	}
	
	private static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { 
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
	
	
	private static int addition(String str) {
		int addition=0; 
		for(int i : str.chars().toArray()) {
			addition+=i; 
		}
		return addition; 
	}
	
	private static int multiplication(String str) {
		int addition=1; 
		for(int i : str.chars().toArray()) {
			addition*=i; 
		}
		return addition; 
	}
	
	public static int hashFunction(String ip, String session_id) {
		int add=0; 
		int mul=0; 
		if(ip!=null){
			add=DataHelper.addition(ip); 
		}
		if(ip!=null && session_id!=null) {
			mul=DataHelper.multiplication(session_id); 
		}
		long val = (add*add)+mul; 
		
		return (val > Integer.MAX_VALUE) ? (int) (val - Integer.MAX_VALUE) : (int) val;  
	}
	
}
