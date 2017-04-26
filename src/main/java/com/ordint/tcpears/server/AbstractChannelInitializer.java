package com.ordint.tcpears.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.ordint.tcpears.service.PositionService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public abstract class  AbstractChannelInitializer<T extends Channel> extends ChannelInitializer<T> {
	
	protected final static ByteBuf DELIMITER =  Unpooled.wrappedBuffer(";".getBytes());
	
	@Autowired
	protected PositionService positionService;


}
