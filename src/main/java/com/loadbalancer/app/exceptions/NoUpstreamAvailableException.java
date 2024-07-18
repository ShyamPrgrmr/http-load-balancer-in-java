package com.loadbalancer.app.exceptions;

public class NoUpstreamAvailableException extends Exception{

	public NoUpstreamAvailableException() { super("No upstrem server configured. Please add servers in configuration --> load.balancer.upstream.server.list ");  }

}
