package com.ordint.tcpears.domain.lombok;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.PositionUtil;

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
	@Test
	public void setPreviousLatLon() {
		
		Position previous = PositionUtil.createPosition("51.1", "-0.4", "-1");
		Position current = PositionUtil.createPosition("51.11", "-0.41", "-1");
		
		current = current.setPreviousLatLon(previous);
		
		assertThat(current.getLastLat(), equalTo("51.1"));
		assertThat(current.getLastLon(), equalTo("-0.4"));
		
	}
	@Test
	public void setPreviousLatLonShouldNotChangeIfSame() {
		
		Position previous = PositionUtil.createPosition("51.1", "-0.4", "-1");
		Position current = PositionUtil.createPosition("51.11", "-0.41", "-1");
		
		current = current.setPreviousLatLon(previous);
		
		Position newP = PositionUtil.createPosition("51.11", "-0.41", "-1").setPreviousLatLon(current);
		
		
		assertThat(newP.getLastLat(), equalTo("51.1"));
		assertThat(newP.getLastLon(), equalTo("-0.4"));
		
	}

}
