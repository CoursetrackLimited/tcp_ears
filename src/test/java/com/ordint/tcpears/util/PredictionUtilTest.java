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

	private final static double LAT = 51.419744;
	private final static double LON = -0.401670;
	private final static double X = -27853.825420170127;
	private final static double Y = 5717614.66216199;
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void shouldConvertTrackPositionToPoint() throws Exception {
		Point2D actual = PredictionUtil.toPoint(String.format("%s,%s,10.2 ", LON, LAT));
		Point2D expected = new Point2D.Double(X, Y);
		
		assertThat(actual, equalTo(expected));
		
	}

	@Test
	public void shouldConvertLatLongToPoint() throws Exception {
		Point2D actual = PredictionUtil.toPoint(LAT, LON);
		Point2D expected = new Point2D.Double(X, Y);
		
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void shouldConvertPositionToPoint() throws Exception {
		Position p = PositionUtil.createPosition(Double.toString(LAT), Double.toString(LON), "-1");
		Point2D actual = PredictionUtil.toPoint(p);
		Point2D expected = new Point2D.Double(X, Y);
		
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void shouldConvertToTrackPosition() throws Exception {
		String trackPosition = PredictionUtil.toTrackPosition(new Point2D.Double(X, Y), "-1");
		assertThat(trackPosition, equalTo(String.format("%s,%s,-1 ", LON, LAT)));
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
	
	@Test
	public void latLongToMeters() throws Exception {
		
		double lat, lon, x, y;
		lat = 51.419844;
		lon =  -0.401731;
		y =  PredictionUtil.latToMeters(lat);
		x =  PredictionUtil.lonToMeters(lon);
		System.out.println("Lat TO meters " + y);
		System.out.println("Lon TO meters " + x);
		System.out.println("y TO lat " + PredictionUtil.metersToLat(y));
		System.out.println("x TO lon " + PredictionUtil.metersToLon(x));
		
	}
		
	
	
}
