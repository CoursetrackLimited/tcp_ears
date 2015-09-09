package com.ordint.tcpears.server;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.service.PositionService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

public class UdpPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
	

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
		String message = packet.content().toString(CharsetUtil.UTF_8);
		out.add(message);
		
	}

}
