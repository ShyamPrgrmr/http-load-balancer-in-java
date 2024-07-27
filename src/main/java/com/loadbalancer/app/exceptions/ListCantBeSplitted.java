package com.loadbalancer.app.exceptions;

public class ListCantBeSplitted extends Exception{
	public ListCantBeSplitted(String s) {
		super("Provided String cannot be splitted"+s); 
	}
}
