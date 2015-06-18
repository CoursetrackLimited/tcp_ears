package com.ordint.tcpears.server;

import static org.apache.commons.lang3.StringUtils.leftPad;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.PositionService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class StringHandler extends SimpleChannelInboundHandler<String> {
	

	private final PositionService positionService;
	private final static Logger log = LoggerFactory.getLogger(StringHandler.class);
	public StringHandler(PositionService positionService) {
		this.positionService = positionService;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
		log.debug(message);
		
		
		
		positionService.update(message);

	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


	   
}
