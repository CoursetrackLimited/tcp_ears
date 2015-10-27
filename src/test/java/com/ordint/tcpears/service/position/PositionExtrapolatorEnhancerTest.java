package com.ordint.tcpears.service.position;

import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.track.geom.PositionToPointConverterImpl;

public class PositionExtrapolatorEnhancerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
		public void testEnhance() throws Exception {
			Clock c = Clock.fixed(Instant.now(), ZoneId.systemDefault());
			LocalDateTime t = LocalDateTime.now(c);
			Position base = Position.builder(c)
					.lat("51.419845")
					.lon("-0.401732")
					.clientDetails(new ClientDetails("groupId", "clientId"))
					.timeCreated(t.minus(200, ChronoUnit.MILLIS))
					.timestampFromDateTime(t.minus(400, ChronoUnit.MILLIS))
					.lag(200)
					.speed("20")
					.lastLat("51.419895")
					.lastLon("-0.401763")
					.build();
					
			Position p = Position.builder(c).position(base)
					.timeCreated(t.minus(400, ChronoUnit.MILLIS))
					.timeCreated(t.minus(600, ChronoUnit.MILLIS))
					.build();
			List<Position> pos1 = Arrays.asList(base);
			List<Position> pos2 = Arrays.asList(p);
			PositionExtrapolatorEnhancer ped = new PositionExtrapolatorEnhancer(c);
			
			List<Position> result = ped.decorate(pos1);
			List<Position> result2 = ped.decorate(pos2);
			
			System.out.println(result.get(0));
			System.out.println(result2.get(0));
			
			assertThat(result.get(0).getLat(), Matchers.not(result2.get(0).getLat()));
			assertThat(result.get(0).getLon(), Matchers.not(result2.get(0).getLon()));
			
		}
	

	@Test
	public void realData() throws Exception {
		Clock c = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		LocalDateTime t = LocalDateTime.now(c);
		Position base = Position.builder(c)
				.lat("51.419754333333")
				.lon("-0.406444166667")
				.clientDetails(new ClientDetails("groupId", "clientId"))
				.timeCreated(t.minus(300, ChronoUnit.MILLIS))
				.timestampFromDateTime(t.minus(500, ChronoUnit.MILLIS))
				.lag(200)
				.speed("20")
				.lastLat("51.419742666667")
				.lastLon("-0.4064375")
				.build();
		List<Position> pos1 = Arrays.asList(base);
		
		PositionExtrapolatorEnhancer ped = new PositionExtrapolatorEnhancer(c);
		
		List<Position> result = ped.decorate(pos1);
		
		System.out.println(result.get(0));
		
		
		//double y = PositionToPointConverterImpl.latToMeters(51.419754333333);
		//double x =PositionToPointConverterImpl.lonToMeters(-0.406444166667);
		//System.out.println(PositionToPointConverterImpl.metersToLon(x+1));
		LatLng start = new LatLng(51.419753, -0.406444166667);
		System.out.println(start);
		LatLng extra = LatLngTool.travel(start, 90, 10, LengthUnit.METER);
		System.out.println(LatLngTool.travel(start, 90, 1, LengthUnit.METER));
		System.out.println(LatLngTool.travel(start, 90, 10, LengthUnit.METER));
		System.out.println(LatLngTool.travel(start, 90, 100, LengthUnit.METER));
	}
	

}
