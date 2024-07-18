package com.loadbalancer.app.exceptions;

public class QueueIsFullException extends Exception {

	public QueueIsFullException(int size) {
		super("Queue size is full Exception. Current Size = "+size);
	}

}
