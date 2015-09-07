package com.ordint.tcpears.domain;



import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.util.TimeProvider;
import com.ordint.tcpears.util.Timestamper;

@RunWith(MockitoJUnitRunner.class)
public class DefaultInputParserTest {
	
	@Mock
	private ClientDetailsResolver clientDetailsResolver;
	@Mock
	private TimeProvider systemClock;
	
	private DefaultInputParser defaultInputParser;
	
	private Timestamper timestamper;
	
	
	@Before
	public void setUp() throws Exception {

		timestamper = Timestamper.fixedTimestamper();
		defaultInputParser = new DefaultInputParser(clientDetailsResolver, timestamper);
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
				.speed("0.08")
				.status("-1")
				.timeCreated(timestamper.now())
				.timestampFromTime("110338.40")
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
				.speed("0.08")
				.status("D")
				.timeCreated(timestamper.now())
				.timestampFromTime("105413.15")
				.build();
		
		assertThat(defaultInputParser.parse(input), equalTo(expected));
		
	}
	

}
