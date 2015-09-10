package com.ordint.tcpears.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class UdpPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
	
	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {	
		out.add(packet.content().retain());
		
	}

}
