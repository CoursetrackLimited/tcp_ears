package com.ordint.tcpears.domain;

import static com.ordint.tcpears.domain.PositionUtil.createPosition;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.lombok.Position;

public class DefaultSnakeWriterTest {

	private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void writeShouldReturnValidTrackWithEmptyExistingTrack() throws Exception {
		DefaultSnakeWriter out = new DefaultSnakeWriter();
		Position position = createPosition("51.0277", "-0.54", "6073.14");
		
		String actual = out.write(position, "");
		
		assertThat(actual, Matchers.equalTo("-0.54,51.0277,6073.14 "));
		
		
	}
	@Test
	public void writeShouldNotConcatExistingTrackIfSame() throws Exception {
		DefaultSnakeWriter out = new DefaultSnakeWriter();
		Position position = createPosition("51.0277", "-0.54", "6073.14");
		
		String actual = out.write(position, "-0.54,51.0277,6073.14 ");
		
		assertThat(actual, Matchers.equalTo("-0.54,51.0277,6073.14 "));
		
		
	}
	@Test
	public void writeShouldConcatExistingTrackIfDifferent() throws Exception {
		DefaultSnakeWriter out = new DefaultSnakeWriter();
		Position position = createPosition("31.0277", "-0.54", "6073.14");
		
		String actual = out.write(position, "-0.54,51.0277,6073.14 ");
		assertThat(actual, Matchers.equalTo("-0.54,31.0277,6073.14 -0.54,51.0277,6073.14 "));
	}
	
	@Test
	public void writeShouldConcatExistingTrackAndUsePreviousAltitudeIfMinusOne() throws Exception {
		DefaultSnakeWriter out = new DefaultSnakeWriter();
		Position position = createPosition("31.0277", "-0.54", "-1");
		
		String actual = out.write(position, "-0.54,51.0277,6073.14 ");
		assertThat(actual, Matchers.equalTo("-0.54,31.0277,6073.14 -0.54,51.0277,6073.14 "));
	}
	
	@Test
	public void writeShouldNotExceedTheMaxLimit()  throws Exception {
		DefaultSnakeWriter out = new DefaultSnakeWriter();
		out.setMaxLength(200);
		//200 character string
		String existingTrack = StringUtils.repeat("-0.54,51.027721,6073.14 ", 8);
		Position position = createPosition("31.0277", "-0.54", "-1");
		
		String actual = out.write(position, existingTrack);
		
		assertThat(actual, startsWith("-0.54,31.0277,6073.14 "));
		
		assertThat(actual.length(), lessThan(200));
		
		
		
	}
	@Test
	public void writeShouldNotExceedTheMaxLimit2()  throws Exception {
		DefaultSnakeWriter out = new DefaultSnakeWriter();
		out.setMaxLength(200);
		//200 character string
		String existingTrack = StringUtils.repeat("-0.54,51.027721,6073.14 ", 80);
		Position position = createPosition("31.0277", "-0.54", "-1");
		
		String actual = out.write(position, existingTrack);
		
		assertThat(actual, startsWith("-0.54,31.0277,6073.14 "));
		
		assertThat(actual.length(), lessThan(200));
		
	}
	
	@Test
	public void increasingTheNumberOfClientsShouldReduceTrackSize() throws Exception {
	
		DefaultSnakeWriter out = new DefaultSnakeWriter();

		
		String tuples = StringUtils.repeat("-0.54,51.027721,6073.14 ", 10);
		String existingTrack = StringUtils.repeat("A", DefaultSnakeWriter.DEFAULT_MAX_TRACK - tuples.length()) + " " + tuples;
		Position position = createPosition("31.0277", "-0.54", "-1");
		
		String before = out.write(position, existingTrack);
		
		out.calculateSnakeLength(30);
		
		String after = out.write(position, before);
		
		assertThat(after.length(), lessThan(before.length()));
		
		
	}
	
	
}
