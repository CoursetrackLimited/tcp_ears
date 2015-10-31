package com.ordint.tcpears.server;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
public class TCPServer {
	private final static Logger log = LoggerFactory.getLogger(TCPServer.class); 
	
	@Autowired
	@Qualifier("tcpBootstrap")	
	private ServerBootstrap tcpBootstrap;
	@Autowired
	@Qualifier("udpBootstrap")	
	private Bootstrap udpBootstrap;	
	
	@Autowired
	@Qualifier("adminServerBootstrap")	
	private ServerBootstrap adminBootstrap;
	@Value("#{T(java.util.Arrays).asList('${tcp.ports}')}") 
	private List<Integer> tcpPorts;
	@Value("${admin.port}")
	private int adminPort;
	
	private List<ChannelFuture> serverChannelFutures = new ArrayList<>();

	@PostConstruct
	public void start() throws Exception {
		log.info("Starting server....");
		if (tcpBootstrap != null) {
			log.info("Binding tcp ports");
			bindPorts(tcpBootstrap);
		}
		if (udpBootstrap != null) {
			log.info("Binding udp ports");
			bindPorts(udpBootstrap);
		}
		log.info("Admin rest server listening on {}", adminPort);
		serverChannelFutures.add(adminBootstrap.bind(adminPort));
		for(ChannelFuture f : serverChannelFutures) {
			f.sync();
		}
	}
	
	private void bindPorts(@SuppressWarnings("rawtypes") AbstractBootstrap bootstrap) {
		for(Integer port : tcpPorts) {
			log.info("Listening on port {}", port);
			serverChannelFutures.add(bootstrap.bind(port));	
		}		
	}
	
	@PreDestroy
	public void stop() {
		serverChannelFutures.forEach(f -> f.channel().close());
	}



}
