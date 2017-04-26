package com.ordint.tcpears.server;

import io.netty.channel.Channel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


public class Udp2ChannelInitializer extends AbstractChannelInitializer<Channel>{

	@Override
	protected void initChannel(Channel channel) throws Exception {
		channel.pipeline()
		.addLast("udpDecoder", new UdpPacketDecoder())
		.addLast("frameDecoder", new DelimiterBasedFrameDecoder(255,true, AbstractChannelInitializer.DELIMITER))
		.addLast("stringDecoder", new StringDecoder())
		.addLast( new StringHandler(positionService));
	}

}
