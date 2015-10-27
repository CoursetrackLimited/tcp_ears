package com.ordint.tcpears.domain;

import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class RaceDetailTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void getStartTimeShouldReturnBST() throws Exception {
		LocalDateTime t = LocalDateTime.parse("2007-09-30T10:15:30");
		
		RaceDetail r = RaceDetail.builder().scheduledStartTime(t).build();
		
		assertThat(r.getStartTime(), Matchers.equalTo("11:15"));
		
		
	}
	@Test
	public void getStartTimeShouldReturnGMT() throws Exception {
		LocalDateTime t = LocalDateTime.parse("2007-11-30T10:15:30");
		
		RaceDetail r = RaceDetail.builder().scheduledStartTime(t).build();
		
		assertThat(r.getStartTime(), Matchers.equalTo("10:15"));
		
		
	}

}
