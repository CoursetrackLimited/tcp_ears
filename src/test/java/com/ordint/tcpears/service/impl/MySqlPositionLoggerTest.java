package com.ordint.tcpears.service.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.Position;

public class MySqlPositionLoggerTest {

	private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
	private ScheduledExecutorService exector  = Executors.newScheduledThreadPool(2);
	@Before
	public void setUp() throws Exception {
	}
	
	
	@Test
	public void testBoolean() throws Exception {
		
		AtomicInteger i = new AtomicInteger(Integer.MAX_VALUE);

	}
	
	@Test
	public void shouldNotMiss() throws Exception {
		JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
		List<Object> actualArgs = new ArrayList<>();
		StringBuilder actualSql = new StringBuilder();
		BDDMockito.given(jdbcTemplate.batchUpdate(Mockito.anyString(), anyList())).will(new Answer<Object>() {
		
			@Override
			public  Object answer(InvocationOnMock invocation) throws Throwable {
				
				List<Object[]> tmp = (List<Object[]>) invocation.getArguments()[1];
				actualSql.setLength(0);
				actualSql.append((String) invocation.getArguments()[0]);
				
				for(Object[] o : tmp) {
					for(Object o1 : o)
						actualArgs.add(o1);
				}
				Thread.sleep(200);
				return null;
			}
		});
		ClientDetails c = new ClientDetails("groupId", "clientId1");
		ClientDetails c2 = new ClientDetails("groupId", "clientId2");
		
	
		final Position p1 = Position.builder()
				.altitude("6073.14")
				.clientDetails(c)
				.heading("119.43")
				.horizontalAccuracy("0.7")
				.verticalAccuracy("0.6")
				.lat("51.027210166667")
				.lon("-0.32454")
				.speed("0.041155555552")
				.status("D")
				.timeCreated(LocalDateTime.now(clock))
				.timestamp("105413.15")
				.build();		

		final Position p2 = Position.builder()
				.altitude("-1")
				.clientDetails(c2)
				.heading("-1")
				.horizontalAccuracy("-1")
				.verticalAccuracy("-1")
				.lat("51.027210166667")
				.lon("-0.00832")
				.speed("0.041155555552")
				.status("-1")
				.timeCreated(LocalDateTime.now(clock))
				.timestamp("110338.40")
				.build();
		MySqlPositionLogger logger =  new MySqlPositionLogger(jdbcTemplate);
		
		logger.log(p1, "test", "horse");
		
		exector.schedule(new Runnable() {		
			@Override
			public void run() {
				logger.writeToDb();				
			}
		}, 20, TimeUnit.MILLISECONDS);
		exector.schedule(new Runnable() {		
			@Override
			public void run() {
				logger.log(p2, "test", "horse");		
			}
		}, 30, TimeUnit.MILLISECONDS);
		
		exector.schedule(new Runnable() {		
			@Override
			public void run() {
				logger.writeToDb();
				
				
			}
		}, 40, TimeUnit.MILLISECONDS);		
		
		
		
		exector.awaitTermination(1, TimeUnit.SECONDS);
		
		verify(jdbcTemplate, times(2)).batchUpdate(Mockito.anyString(), Mockito.anyList());
		actualArgs.forEach(o -> System.out.println(o));
		assertThat(actualArgs.size(), equalTo(30));
		assertThat(actualSql.toString(), equalTo("INSERT INTO `positionHistory` (`altitude`,`clientId`,`groupId`, `heading`,`horizontalAccuracy`,`lat`,`lon`,`speed`,`status`,`gpsTimeStamp`,`timeReceived`,`verticalAccuracy`,`lag`,`vehicleType`,`source`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"));
		
	}
}
