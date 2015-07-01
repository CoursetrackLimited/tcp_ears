package com.ordint.tcpears.server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.rpc.RpcServer;
import com.ordint.tcpears.service.AdministrationService;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionService;
import com.ordint.tcpears.service.impl.AdministrationServiceImpl;
import com.ordint.tcpears.service.impl.ClientManagerImpl;
import com.ordint.tcpears.service.impl.HorseDetailsResolver;
import com.ordint.tcpears.service.impl.PositionServiceImpl;
@Configuration
@ComponentScan("com.ordint.tcpears")
@PropertySource("classpath:netty.properties")
public class Config {
	
	@Value("${boss.thread.count}")
	private int bossCount;

	@Value("${tcp.port}")
	private int tcpPort;
	@Value("${admin.port}")
	private int adminPort;	

	@Value("${so.keepalive}")
	private boolean keepAlive;

	@Value("${so.backlog}")
	private int backlog;
	@Autowired
	private Environment environment;	
	
	@Autowired
	private PositionChannelHandler positionChannelHandler;
	
	@Bean
	public DataSource dataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setUsername(environment.getProperty("user", "root"));
		ds.setUrl("jdbc:mysql://"
				+ environment.getProperty("dbhost", "10.10.0.148")
				+ "/ggps01");
		//		+ "/tcp_ears");
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setDefaultAutoCommit(true);
		ds.setPassword(environment.getProperty("password", "Grotto1Frop"));
		//ds.setPassword(environment.getProperty("password", "sqlgod"));
		return ds;
	}

	@Bean
	public PlatformTransactionManager txManager() {
		return new DataSourceTransactionManager(dataSource());
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}
	
	@SuppressWarnings("unchecked")
	@Bean(name = "serverBootstrap")
	public ServerBootstrap bootstrap() {
		ServerBootstrap b = new ServerBootstrap();
		b.option(ChannelOption.SO_BACKLOG, 1024);
		b.group(bossGroup(), workerGroup())
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
				.childHandler(positionChannelHandler);
		/*
		Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
		Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
		for (@SuppressWarnings("rawtypes")
		ChannelOption option : keySet) {
			b.option(option, tcpChannelOptions.get(option));
		}
		*/
		return b;
	}
	
	@Bean(destroyMethod="interrupt", initMethod="start")
	public Thread restService() throws Exception {
		return new Thread(new RpcServer(administrationService(), adminPort));
	}
	
	@Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup bossGroup() {
		return new NioEventLoopGroup(bossCount);
	}

	@Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup workerGroup() {
		return new NioEventLoopGroup();
	}

	@Bean(name = "tcpSocketAddress")
	public InetSocketAddress tcpPort() {
		return new InetSocketAddress(tcpPort);
	}

	@Bean(name = "tcpChannelOptions")
	public Map<ChannelOption<?>, Object> tcpChannelOptions() {
		Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
		options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
		options.put(ChannelOption.SO_BACKLOG, backlog);
		return options;
	}
	
	@Bean
	@Scope("prototype")
	public ChannelHandler channelHandler() {
		return new DefaultChannelInitializer();
	}
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	@Bean
	public MemcacheHelper memcacheHelper() throws Exception {
		//return  new MemcacheHelper();
		return  new MemcacheHelper("localhost", 11211);
	}

	@Bean
	public ClientDetailsResolver clientDetailsResolver() {
		return new HorseDetailsResolver();
	}
	@Bean
	public ClientManager clientManager() throws Exception {
		return new ClientManagerImpl(memcacheHelper(), jdbcTemplate());
	}
	
	@Bean
	public PositionService positionService() throws Exception {
		return new PositionServiceImpl(clientManager(), clientDetailsResolver());
	}
	@Bean
	public AdministrationService administrationService() throws Exception {
		return new AdministrationServiceImpl(clientManager(), clientDetailsResolver());
	}
	private final class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(255));
			pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
			pipeline.addLast(new StringHandler(positionService()));
		}
	}	
}
