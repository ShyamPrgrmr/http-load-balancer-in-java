package com.loadbalancer.app.exceptions;

public class ListHasDuplicatesException extends Exception{

	public ListHasDuplicatesException() {
		super("Internal Server Error : Please check if the upstream list has duplicate value"); 
	}

}
