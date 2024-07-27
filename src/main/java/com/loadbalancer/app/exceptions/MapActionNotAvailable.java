package com.loadbalancer.app.exceptions;

public class MapActionNotAvailable extends Exception{
	public MapActionNotAvailable(String e) {
		super("Action not available "+e); 
	}
}
