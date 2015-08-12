package com.ordint.tcpears.domain;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class SimplePredictingTrackWriterTest {

	@Before
	public void setUp() throws Exception {
	}

	
	
	@Test
	public void shouldAddNewPositionAndPredictedPostionToTrack() {
		
		SimplePredictingTrackWriter out = new SimplePredictingTrackWriter();
		
		String existingTrack = "-0.394572,51.215792,1000 ";
		
		Position p = Position.builder()
				.altitude("-1")
				.lat("51.215792")
				.lon("-0.394622")
				.speed("11")
				.lag(700l)
				.build();
				
		String positions[] = out.write(p, existingTrack).split(" ");
		
		assertThat(positions.length, equalTo(3));
		assertThat(positions[2], equalTo("-0.394572,51.215792,1000"));
		assertThat(positions[1], equalTo("-0.394622,51.215792,1000"));
		assertThat(positions[0], equalTo("-0.39473199999999997,51.215790999999996,1000"));
		
		
		
	}
	
	@Test
	public void shouldAddNewPositionAndReplacePreviousPostionToTrack() {
		
		SimplePredictingTrackWriter out = new SimplePredictingTrackWriter();
		
		String existingTrack = "-0.39473199999999997,51.215790999999996,1000 -0.394622,51.215792,1000 -0.394572,51.215792,1000 ";
		
		Position p = Position.builder()
				.altitude("-1")
				.lat("51.215792")
				.lon("-0.39471")
				.speed("11")
				.lag(700l)
				.build();
				
		String positions[] = out.write(p, existingTrack).split(" ");
		
		assertThat(positions.length, equalTo(4));
		assertThat(positions[3], equalTo("-0.394572,51.215792,1000"));
		assertThat(positions[2], equalTo("-0.394622,51.215792,1000"));
		assertThat(positions[1], equalTo("-0.39471,51.215792,1000"));
		assertThat(positions[0], equalTo("-0.39482,51.215790999999996,1000"));
		
	}
}
