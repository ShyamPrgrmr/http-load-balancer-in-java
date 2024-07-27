package com.loadbalancer.app.exceptions;

public class UpstreamNotAvailable extends Exception{
	public UpstreamNotAvailable(String e) {
		super(e+" Not available in healthcheck map");
	}
}
