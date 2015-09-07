package com.ordint.tcpears.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ordint.rpc.JsonRpcChannelHandler;
import com.ordint.rpc.JsonRpcHandler;

@Component
@Scope("prototype")
public class AdminChannelInitializer extends ChannelInitializer<SocketChannel> {
	
		@Autowired
		private JsonRpcHandler jsonRpcHandler;
	
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline p = ch.pipeline();
			 p.addLast("encoder", new HttpResponseEncoder());
			 p.addLast("decoder", new HttpRequestDecoder());
			 p.addLast("aggregator", new HttpObjectAggregator(1048576));
			 p.addLast(new JsonRpcChannelHandler(jsonRpcHandler));
			
		}
		
		
	
}
