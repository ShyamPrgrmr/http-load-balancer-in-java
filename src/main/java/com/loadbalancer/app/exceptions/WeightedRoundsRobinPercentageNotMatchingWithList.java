package com.loadbalancer.app.exceptions;

public class WeightedRoundsRobinPercentageNotMatchingWithList extends Exception {

	public WeightedRoundsRobinPercentageNotMatchingWithList(String upstream, String weights, int upstream_size, int weight_size) {
		super("Upstreams "+upstream+"  Percentages : "+weights+".  Number of upstreams("+upstream_size+") is less or not equal to Number of percentages("+weight_size+")" ); 
	}

}
