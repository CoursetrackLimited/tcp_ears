package com.ordint.tcpears.server;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.ordint.rpc.JsonRpcHandler;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.server.udp.NioUdpServerChannel;
import com.ordint.tcpears.service.AdministrationService;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionService;
import com.ordint.tcpears.service.ReplayService;
import com.ordint.tcpears.service.admin.AdministrationServiceImpl;
import com.ordint.tcpears.service.position.ClientManagerImpl;
import com.ordint.tcpears.service.position.FilePositionPublisher;
import com.ordint.tcpears.service.position.HorseDetailsResolver;
import com.ordint.tcpears.service.position.MemcachePositionPublisher;
import com.ordint.tcpears.service.position.MySqlPositionLogger;
import com.ordint.tcpears.service.position.PositionEnhancers;
import com.ordint.tcpears.service.position.PositionLogger;
import com.ordint.tcpears.service.position.PositionServiceImpl;
import com.ordint.tcpears.service.position.PublishingScheduler;
import com.ordint.tcpears.service.race.DefaultRaceService;
import com.ordint.tcpears.service.replay.MySqlReplayService;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
@Configuration
@PropertySource("classpath:netty.properties")
public class Config {
	
	public static int THREADS =1;
	
	@Value("${boss.thread.count}")
	private int bossCount;

	@Value("${admin.port}")
	private int adminPort;	

	@Value("${so.keepalive}")
	private boolean keepAlive;

	@Value("${so.backlog}")
	private int backlog;
	
	@Value("${listener.mode}")
	private String mode;
	
	@Value("${sectorDirectory}")
	private String sectorDirectoryPath;
	
	@Value("${useSnakes}")
	private boolean useSnakes;
	
	@Value("${udpbeta.threads}")
	private int threadCount;
	
	@Autowired
	private Environment environment;	
	
	
	@Bean(name="a")
	public DataSource dataSource() {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		ds.setUser(environment.getProperty("user", "root"));
		ds.setJdbcUrl("jdbc:mysql://"
				+ environment.getProperty("dbhost", "localhost")
				+ "/ggps01?useServerPrepStmts=false&rewriteBatchedStatements=true");
		try {
			ds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			throw new RuntimeException("Error creating c3p0 datasource", e);
		}
		ds.setAutoCommitOnClose(true);
		ds.setPassword(environment.getProperty("password", "Grotto1Frop"));
		ds.setMaxPoolSize(15);
		ds.setMinPoolSize(5);
		ds.setMaxStatements(100);
		ds.setMaxConnectionAge(7200);
		ds.setMaxIdleTime(7200);
		ds.setAutomaticTestTable("c3p0");
		ds.setTestConnectionOnCheckout(true);
		ds.setIdleConnectionTestPeriod(3600);
		
		
		//ds.setPassword(environment.getProperty("password", "sqlgod"));
		return ds;
	}

	@Bean(name="a2")
	public PlatformTransactionManager txManager() {
		return new DataSourceTransactionManager(dataSource());
	}
	
	@Bean(name="a3")
	public JdbcTemplate jdbcTemplate() { 
		return new JdbcTemplate(dataSource());
	}
	
	@Bean(name = "tcpBootstrap")
	public ServerBootstrap bootstrap() {
		if (!mode.contains("tcp")) {
			return null;
		}
		ServerBootstrap b = new ServerBootstrap();
		b.option(ChannelOption.SO_BACKLOG, 1024);
		b.group(bossGroup(), workerGroup())
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
				.childHandler(tcpChannelInitializer());
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
	
/*	@Bean(name = "udpBootstrap")
	public Bootstrap udpBootstrap() {
		if(!mode.contains("udp")) {
			return null;
		}
		final int THREADS = Runtime.getRuntime().availableProcessors() * 2;
		EventLoopGroup group = new EpollEventLoopGroup(THREADS);
		Bootstrap b = new Bootstrap();
		b.group(group)
			.channel(EpollDatagramChannel	.class)
			.option(ChannelOption.SO_BROADCAST, true)
            .option(EpollChannelOption.SO_REUSEPORT, true)
			.handler(udpChannelInitializer);
		return b;
	}*/
	
	@Bean(name = "udpBootstrap")
	public AbstractBootstrap udpBootstrap() {
		if(!mode.contains("udp")) {
			return null;
		}
		if(mode.contains("64")) {
			THREADS = Runtime.getRuntime().availableProcessors() * 2;
			EventLoopGroup group = new EpollEventLoopGroup(THREADS);
			Bootstrap b = new Bootstrap();
			b.group(group)
				.channel(EpollDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
	            .option(EpollChannelOption.SO_REUSEPORT, true)
				.handler(udpChannelInitializer());
			return b;			
		} else if (mode.contains("beta")) {
		    return newUdp();
		}else {	
			Bootstrap b = new Bootstrap();
			b.group(new NioEventLoopGroup(bossCount))
				.channel(NioDatagramChannel.class)
				.handler(udpChannelInitializer());
			return b;
		}
	}
	
	private ServerBootstrap newUdp() {
	    return new ServerBootstrap()
	            .group(new NioEventLoopGroup(bossCount), new DefaultEventLoopGroup(threadCount))
	            .channel(NioUdpServerChannel.class)
	            .handler(new LoggingHandler(LogLevel.ERROR))
	            .childHandler(udp2ChannelInitializer());
	    
	}
	

	@Bean(name = "adminServerBootstrap")
	public ServerBootstrap adminBootstrap() {
		ServerBootstrap b = new ServerBootstrap();
		b.option(ChannelOption.SO_BACKLOG, 1024);
		b.group(bossGroup(), workerGroup())
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.DEBUG))
				.childHandler(adminChannelInitializer());
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
	
	@Bean(name ="propertySourcesPlaceholderConfigurer")
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	@Bean(name="a4")
	@Profile("!dev")
	public MemcacheHelper memcacheHelper() throws Exception {
		
		return  new MemcacheHelper("localhost", 11211);
	}
	@Bean(name="a5")
	@Profile("dev")
	public MemcacheHelper devMemcacheHelper() throws Exception {
		return  new MemcacheHelper();
		
	}	
	@Bean(name="a6")
	public File sectorReportsDir() {
		File sectorDirectory = new File(sectorDirectoryPath);
		if (!sectorDirectory.exists()) {
			sectorDirectory.mkdirs();
		}
		return sectorDirectory;
	}
	
	@Bean(name="a7")
	public ClientDetailsResolver clientDetailsResolver() {
		return new HorseDetailsResolver();
	}
	@Bean(name= {"a8", "clientManager"})
	@Autowired
	public ClientManager clientManager() throws Exception {
		return new ClientManagerImpl(useSnakes);
	}
	@Bean(name="a9")
	public PublishingScheduler publishingScheduler() {
		return new PublishingScheduler(useSnakes);
	}
	@Bean(name="a10")
	public PositionLogger positionLogger() {
		return new MySqlPositionLogger();
	}
	@Bean(name="a11")
	public PositionService positionService() throws Exception {
		return new PositionServiceImpl(clientManager(), clientDetailsResolver(), positionLogger());
	}
	@Bean(name="administrationService")
	@Autowired
	public AdministrationService administrationService() throws Exception {
		return new AdministrationServiceImpl();
	}
	@Bean(name="a12")
	@Autowired
	public ReplayService replayService() throws Exception {
		return new MySqlReplayService();
	}
	@Bean(name="a13")
	public JsonRpcHandler jsonRpcHandler() throws Exception {
		return new JsonRpcHandler(administrationService());	
	}

	@Bean(name="a14")
	@Autowired	
	public AdminChannelInitializer adminChannelInitializer() {
		return new AdminChannelInitializer();
	}
	
	@Bean(name="a15")
	@Autowired
	public UdpChannelInitializer udpChannelInitializer() {
		return new UdpChannelInitializer();
	}
	@Bean(name="a16")
	@Autowired
	public Udp2ChannelInitializer udp2ChannelInitializer() {
		return new Udp2ChannelInitializer();
	}
	@Bean(name="a17")
	@Autowired
	public TcpChannelInitializer tcpChannelInitializer() {
		return new TcpChannelInitializer();
	}
	
	@Bean(name="a18")
	@Autowired
	public TCPServer tCPServer() {
		return new TCPServer();
	}

	@Bean(name="a19")
	@Autowired
	@Profile("dev")
	public FilePositionPublisher filePositionPublisher() throws IOException {
		return new FilePositionPublisher();
	}
	@Bean(name="a20")
	@Autowired
	@Profile("!dev")
	public MemcachePositionPublisher memcachePositionPublisher() throws IOException {
		return new MemcachePositionPublisher();
	}	
	@Bean(name="a21")
	@Autowired
	public MySqlPositionLogger mySqlPositionLogger() {
		return new MySqlPositionLogger();
	}	
	@Bean(name="a22")
	@Autowired
	public PositionEnhancers positionEnhancers() {
		return new PositionEnhancers();
	}
	@Bean(name="a23")
	@Autowired
	public DefaultRaceService defaultRaceService() {
		return new DefaultRaceService();
	}	


}
