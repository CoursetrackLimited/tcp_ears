package com.ordint.tcpears.domain;

import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class PositionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetLag() throws Exception {
		Clock c = Clock.fixed(Instant.parse("2015-08-08T10:15:30.00Z"), ZoneId.of("UTC"));
		Position p = Position.builder(c)
			.timestampFromTime("122320.97")
			.timeCreated(LocalDateTime.parse("2015-08-08T12:23:21.018666"))
			.build();
		
		assertThat(p.getLag(), Matchers.equalTo(48L));
		
	}

}
