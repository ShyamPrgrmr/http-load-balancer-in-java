package com.loadbalancer.app.exceptions;

public class PercentageIsnotMultipleOf25OrMoreThanHundreadOrAddtionIsNot100 extends Exception {
	public PercentageIsnotMultipleOf25OrMoreThanHundreadOrAddtionIsNot100(Integer per) {
		super("Percentage is more than 100% or not multiple of 25%"+per); 
	}
	//addition more than hundread
	public PercentageIsnotMultipleOf25OrMoreThanHundreadOrAddtionIsNot100() {
		super("Percentages Addtion is more than 100%"); 
	}
}
