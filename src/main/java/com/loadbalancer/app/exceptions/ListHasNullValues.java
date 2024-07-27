package com.loadbalancer.app.exceptions;

import java.util.List;

public class ListHasNullValues extends Exception{

	public ListHasNullValues(String list) {
		super(list+" Has Null Values"); 
	}

}
