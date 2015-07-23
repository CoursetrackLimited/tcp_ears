package com.ordint.tcpears.domain;

import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class PositionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetLag() throws Exception {
		Position p = Position.builder()
			.timestamp("102345.34")
			.timeCreated(LocalDateTime.parse("2007-12-03T10:23:46.355"))
			.build();
		
		assertThat(p.getLag(), Matchers.equalTo(1015L));
		
	}

}
