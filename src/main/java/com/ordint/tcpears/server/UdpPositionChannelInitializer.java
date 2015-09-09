package com.ordint.tcpears.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.service.PositionService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
@Component
@Scope("prototype")
public class UdpPositionChannelInitializer extends ChannelInitializer<DatagramChannel>{
	@Autowired
	private PositionService positionService;
	@Override
	protected void initChannel(DatagramChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("udpDecoder", new UdpPacketDecoder());
		pipeline.addLast(new StringHandler(positionService));		
		
	}



}
