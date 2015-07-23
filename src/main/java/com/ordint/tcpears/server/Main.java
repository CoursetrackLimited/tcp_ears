package com.ordint.tcpears.server;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;


public class Main {

	public static void main(String[] args) {
		SimpleCommandLinePropertySource cmdArgs = new SimpleCommandLinePropertySource(args);
		@SuppressWarnings("resource")
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().getPropertySources().addFirst(cmdArgs);
		context.register(Config.class);
		context.refresh();
		context.registerShutdownHook();
		
		
	}

}