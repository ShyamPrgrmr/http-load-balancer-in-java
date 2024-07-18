package com.loadbalancer.app.exceptions;

public class QueueIsEmptyException extends Exception {

	public QueueIsEmptyException() {
		super("Queue is empty, No item to pull"); 
	}

}
