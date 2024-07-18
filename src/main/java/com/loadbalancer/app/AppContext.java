package com.loadbalancer.app;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class AppContext {
	
	public AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ProjectConfiguration.class);
	
	public AnnotationConfigApplicationContext returnContext() {
		return this.context; 
	}
	
}
