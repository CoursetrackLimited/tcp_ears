package com.ordint.tcpears.server;



import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.rpc.RpcServer;
import com.ordint.tcpears.service.PositionService;



public class TCPEarsServer {
/*	
	private static final ThreadLocal<MemcacheHelper> memcacheHelper2 = new ThreadLocal<MemcacheHelper>() {
		 @Override
	        protected MemcacheHelper initialValue()
	        {
	            try {
					return new MemcacheHelper("localhost", 11211);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	        }		
	};
	*/
	private MemcacheHelper memcacheHelper;
	private PositionService positionService;
	StringHandler handler;
	public static void main(String[] args) throws Exception {
		TCPEarsServer server = new TCPEarsServer();
		server.startUp(5011);
	}
	
	private void startUp(final int port)
			throws Exception {
		final SslContext sslCtx = null;
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		Config config = new Config();
		
		try {			
			positionService = config.positionService();
			
			ServerBootstrap b = new ServerBootstrap();
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
					.childHandler(new DefaultChannelInitializer());
			Channel ch = b.bind(port).sync().channel();

		

			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	
	private final class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(255));
			pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
			pipeline.addLast(new StringHandler(positionService));
		}
	}
}
