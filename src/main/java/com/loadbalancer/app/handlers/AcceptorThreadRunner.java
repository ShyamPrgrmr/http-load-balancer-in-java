package com.loadbalancer.app.handlers;

public class AcceptorThreadRunner implements Runnable{
	private AppHTTPAcceptor acceptor; 
	public AcceptorThreadRunner(AppHTTPAcceptor acceptor){
		this.acceptor = acceptor; 
	}
	@Override
	public void run() {
		this.acceptor.initiate();
	}
}