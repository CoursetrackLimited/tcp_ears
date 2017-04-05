package com.ordint.tcpears.server;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
@Component
@Scope("prototype")
public class UdpChannelInitializer extends AbstractChannelInitializer<DatagramChannel>{

	@Override
	protected void initChannel(DatagramChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("udpDecoder", new UdpPacketDecoder());
		pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(255,true, DELIMITER));
		pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
		pipeline.addLast(new StringHandler(positionService));		
		
	}



}
