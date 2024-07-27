package com.loadbalancer.app.helper;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublisher;
import java.time.Duration;
import java.util.Optional;

public class HttpRequestE extends HttpRequest{
	
	public HttpRequestE(){
		super(); 
	}
	
	public String toString(){
		return this.headers().toString();
	}

	@Override
	public Optional<BodyPublisher> bodyPublisher() {
		return Optional.empty();
	}

	@Override
	public String method() {
		return null;
	}

	@Override
	public Optional<Duration> timeout() {
		return Optional.empty();
	}

	@Override
	public boolean expectContinue() {
		return false;
	}

	@Override
	public URI uri() {
		return null;
	}

	@Override
	public Optional<Version> version() {
		return Optional.empty();
	}

	@Override
	public HttpHeaders headers() {
		return null;
	}
}
