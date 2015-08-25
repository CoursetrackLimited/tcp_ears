package com.ordint.tcpears.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.service.PositionService;
@Component
@Scope("prototype")
public class PositionChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	@Autowired
	private PositionService positionService;
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(255));
		pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
		pipeline.addLast(new StringHandler(positionService));
	}
}
