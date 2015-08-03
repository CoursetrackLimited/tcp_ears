package com.ordint.tcpears.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
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

import com.ordint.tcpears.domain.DefaultInputParser;



@Component
public class TCPServer {
	private final static Logger log = LoggerFactory.getLogger(TCPServer.class); 
	
	@Autowired
	@Qualifier("serverBootstrap")
	
	private ServerBootstrap bootstrap;
	@Value("#{T(java.util.Arrays).asList('${tcp.ports}')}") 
	private List<Integer> tcpPorts;

	private List<ChannelFuture> serverChannelFutures = new ArrayList<>();

	@PostConstruct
	public void start() throws Exception {
		log.info("Starting server, listening at  {}", tcpPorts);
		for(Integer port : tcpPorts) {
			serverChannelFutures.add(bootstrap.bind(port));
		}
		for(ChannelFuture f : serverChannelFutures) {
			f.sync();
		}
	}

	@PreDestroy
	public void stop() {
		serverChannelFutures.forEach(f -> f.channel().close());
	}



}
