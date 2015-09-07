package com.ordint.tcpears.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.awt.geom.Point2D;

import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionUtil;

public class PredictionUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void shouldConvertTrackPositionToPoint() throws Exception {
		Point2D actual = PredictionUtil.toPoint("12.33,04.00004,10.90 ");
		Point2D expected = new Point2D.Double(12.33, 04.00004);
		
		assertThat(actual, equalTo(expected));
		
	}

	@Test
	public void shouldConvertLatLongToPoint() throws Exception {
		Point2D actual = PredictionUtil.toPoint(04.00004, 12.33);
		Point2D expected = new Point2D.Double(12.33, 04.00004);
		
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void shouldConvertPositionToPoint() throws Exception {
		Position p = PositionUtil.createPosition("04.00004", "12.33", "-1");
		Point2D actual = PredictionUtil.toPoint(p);
		Point2D expected = new Point2D.Double(12.33, 04.00004);
		
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void shouldConvertToTrackPosition() throws Exception {
		String trackPosition = PredictionUtil.toTrackPosition(new Point2D.Double(12.33, 04.00004), "-1");
		assertThat(trackPosition, equalTo("12.33,4.00004,-1 "));
	}

	@Test
	public void getMostRecentTrackPositionShouldReturnNull() throws Exception {
		String actual = PredictionUtil.getMostRecentPosition("");
		assertThat(actual, nullValue());
	}
	@Test
	public void getMostRecentTrackPositionShouldReturnOnlyPosition() throws Exception {
		String actual = PredictionUtil.getMostRecentPosition("12.33,04.00004,10.90 ");
		
		assertThat(actual, equalTo("12.33,04.00004,10.90 "));
	}
	@Test
	public void getMostRecentTrackPositionShouldReturnFirstPosition() throws Exception {
		String actual = PredictionUtil.getMostRecentPosition("12.33,04.00004,10.90 32.3333,12.333,-1 ");
		
		assertThat(actual, equalTo("12.33,04.00004,10.90 "));
	}
}
