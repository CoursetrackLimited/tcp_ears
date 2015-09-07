package com.ordint.tcpears.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.ordint.rpc.JsonRpcHandler;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.AdministrationService;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionLogger;
import com.ordint.tcpears.service.PositionService;
import com.ordint.tcpears.service.ReplayService;
import com.ordint.tcpears.service.impl.AdministrationServiceImpl;
import com.ordint.tcpears.service.impl.ClientManagerImpl;
import com.ordint.tcpears.service.impl.HorseDetailsResolver;
import com.ordint.tcpears.service.impl.MySqlPositionLogger;
import com.ordint.tcpears.service.impl.MySqlReplayService;
import com.ordint.tcpears.service.impl.PositionServiceImpl;
@Configuration
@ComponentScan("com.ordint.tcpears")
@PropertySource("classpath:netty.properties")
public class Config {
	
	@Value("${boss.thread.count}")
	private int bossCount;

;
	@Value("${admin.port}")
	private int adminPort;	

	@Value("${so.keepalive}")
	private boolean keepAlive;

	@Value("${so.backlog}")
	private int backlog;
	@Autowired
	private Environment environment;	
	
	@Autowired
	private PositionChannelInitializer positionChannelInitializer;
	
	@Autowired
	private AdminChannelInitializer adminChannelInitializer;
	
	@Bean
	public DataSource dataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setUsername(environment.getProperty("user", "root"));
		ds.setUrl("jdbc:mysql://"
				+ environment.getProperty("dbhost", "localhost")
				+ "/ggps01?useServerPrepStmts=false&rewriteBatchedStatements=true");
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
	
	@Bean(name = "serverBootstrap")
	public ServerBootstrap bootstrap() {
		ServerBootstrap b = new ServerBootstrap();
		b.option(ChannelOption.SO_BACKLOG, 1024);
		b.group(bossGroup(), workerGroup())
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
				.childHandler(positionChannelInitializer);
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
	@Bean(name = "adminServerBootstrap")
	public ServerBootstrap adminBootstrap() {
		ServerBootstrap b = new ServerBootstrap();
		b.option(ChannelOption.SO_BACKLOG, 1024);
		b.group(bossGroup(), workerGroup())
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
				.childHandler(adminChannelInitializer);
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

	@Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup bossGroup() {
		return new NioEventLoopGroup(bossCount);
	}

	@Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup workerGroup() {
		return new NioEventLoopGroup();
	}

	@Bean(name = "tcpChannelOptions")
	public Map<ChannelOption<?>, Object> tcpChannelOptions() {
		Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
		options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
		options.put(ChannelOption.SO_BACKLOG, backlog);
		return options;
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
		return new ClientManagerImpl();
	}
	@Bean
	public PositionLogger positionLogger() {
		return new MySqlPositionLogger();
	}
	@Bean
	public PositionService positionService() throws Exception {
		return new PositionServiceImpl(clientManager(), clientDetailsResolver(), positionLogger());
	}
	@Bean
	@Autowired
	public AdministrationService administrationService() throws Exception {
		return new AdministrationServiceImpl();
	}
	@Bean
	@Autowired
	public ReplayService replayService() throws Exception {
		return new MySqlReplayService();
	}
	@Bean
	public JsonRpcHandler jsonRpcHandler() throws Exception {
		return new JsonRpcHandler(administrationService());	
	}
	

	
}
