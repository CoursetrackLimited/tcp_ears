package com.ordint.tcpears.domain;



import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ordint.tcpears.service.ClientDetailsResolver;
@RunWith(MockitoJUnitRunner.class)
public class DefaultInputParserTest {
	
	@Mock
	private ClientDetailsResolver clientDetailsResolver;
	
	private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
	
	private DefaultInputParser defaultInputParser;
	
	
	@Before
	public void setUp() throws Exception {
		defaultInputParser = new DefaultInputParser(clientDetailsResolver, clock);
	}

	@Test
	public void shouldParseShortInput() throws Exception {
		
		ClientDetails c = new ClientDetails("groupId", "clientId");
		BDDMockito.when(clientDetailsResolver.resolveClientDetails("400A7C")).thenReturn(c);
		String input = "400A7C,110338.40,5101.63261,-0.49920,0.080000";
			
		Position expected = Position.builder()
				.altitude("-1")
				.clientDetails(c)
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
		
		assertThat(defaultInputParser.parse(input), equalTo(expected));
		
	}
	@Test
	public void shouldParseLongInput() throws Exception {
		
		ClientDetails c = new ClientDetails("groupId", "clientId");
		BDDMockito.when(clientDetailsResolver.resolveClientDetails("400678")).thenReturn(c);
		String input = "400678,105413.15,5101.63261,-00019.47240,0.080000,119.430000,0.700000,0.600000,6073.14,D";
		Position expected = Position.builder()
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
		
		assertThat(defaultInputParser.parse(input), equalTo(expected));
		
	}

}
