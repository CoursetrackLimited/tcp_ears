package com.ordint.tcpears.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TCPServer {
	
	@Autowired
	private Thread adminServer;
	
	@Autowired
	@Qualifier("serverBootstrap")
	private ServerBootstrap bootstrap;
	
	@Autowired
	@Qualifier("tcpSocketAddress")
	private InetSocketAddress tcpPort;

	private Channel serverChannel;

	@PostConstruct
	public void start() throws Exception {
		System.out.println("Starting server at " + tcpPort);
		serverChannel = bootstrap.bind(tcpPort).sync().channel().closeFuture().sync()
				.channel();
	}

	@PreDestroy
	public void stop() {
		serverChannel.close();
	}



}
