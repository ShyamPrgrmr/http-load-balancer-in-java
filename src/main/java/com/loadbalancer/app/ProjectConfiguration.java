package com.loadbalancer.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import com.loadbalancer.app.handlers.AppHTTPAcceptor;
import com.loadbalancer.app.model.AppHTTPUpstream;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Configuration
@ComponentScan(basePackages = "com.loadbalancer.app") 
@PropertySource("classpath:application.properties")

public class ProjectConfiguration {
	
	@Bean
	public Logger getLogger() {
		return LogManager.getLogger(App.class);
	}
	
	
}
