package com.ordint.tcpears.server;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class PositionTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testCreationWithFullMessage() {
		String posInfo ="GR1,400678,$PUBX,99,105413.15,51.780208666667,N,-0.32454,W,6073.14,D,0.700000,0.600000,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		Position p = new Position(posInfo);
		
		assertThat("GR1", equalTo(p.getGroupId()));
		assertThat("400678", equalTo(p.getClientId()));
		assertThat("6073.14", equalTo(p.getAltitude()));
	}
	@Test
	public void concatTrackShouldReturnValidTrack() {
		//valid track is long,lat,altitude
		String posInfo ="GR1,400678,$PUBX,99,105413.15,51.780208666667,N,-0.32454,W,6073.14,D,0.700000,0.600000,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		Position p = new Position(posInfo);		
		
		assertThat("-0.32454,51.780208666667,6073.14 ", equalTo(p.concatTrack("")));
	}
	
	@Test
	public void concatTrackShouldNotConcatIfPostionUnchanged() throws Exception {
		String posInfo ="GR1,400A7C,$PUBX,99,110338.40,51.027210166667,N,-0.00832,W,-1,-1,-1,-1,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		
		Position p = new Position(posInfo);
		
		String track = p.concatTrack("");
		
		assertThat(p.concatTrack(track), equalTo(track));
		
		String posInfo2 ="GR1,400A7C,$PUBX,99,110338.40,51.227210166667,N,-0.00832,W,-1,-1,-1,-1,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		
		Position p2 = new Position(posInfo2);
		String actual = p2.concatTrack(track);
		assertThat(actual, not(equalTo(track)));
		assertThat(actual, Matchers.containsString(track));
	}
	@Test
	public void concatTrackShouldConcatIfPostionChanged() throws Exception {
		String posInfo ="GR1,400A7C,$PUBX,99,110338.40,51.027210166667,N,-0.00832,W,-1,-1,-1,-1,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		
		Position p = new Position(posInfo);
		
		String track = p.concatTrack("");
				
		String posInfo2 ="GR1,400A7C,$PUBX,99,110338.40,51.227210166667,N,-0.00832,W,-1,-1,-1,-1,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		
		Position p2 = new Position(posInfo2);
		String actual = p2.concatTrack(track);
		assertThat(actual, not(equalTo(track)));
		assertThat(actual, Matchers.containsString(track));
	}
	
	@Test
	public void concatTrackShouldUseLastAltitudeIfCurrentAltitudeIsMinus1() throws Exception {
		//valid track is long,lat,altitude
		String posInfo ="GR1,400678,$PUBX,99,105413.15,51.780208666667,N,-0.32454,W,6073.14,D,0.700000,0.600000,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		Position p = new Position(posInfo);
		String track = p.concatTrack("");
		
		String posInfo2 ="GR1,400A7C,$PUBX,99,110338.40,51.227210166667,N,-0.00832,W,-1,-1,-1,-1,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		Position p2 = new Position(posInfo2);
		String actual = p2.concatTrack(track);
		
		assertThat(actual, equalTo("-0.00832,51.227210166667,6073.14 -0.32454,51.780208666667,6073.14 "));
		
	}

}
