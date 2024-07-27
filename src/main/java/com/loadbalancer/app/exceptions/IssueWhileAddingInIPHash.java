package com.loadbalancer.app.exceptions;

public class IssueWhileAddingInIPHash extends Exception{

	public IssueWhileAddingInIPHash(String upstream) {
		super("Issue while adding "+upstream+" in hashmap. (IPHASH Algorithm)"); 
	}

}
