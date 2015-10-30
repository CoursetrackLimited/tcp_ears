package com.ordint.tcpears.track.geom;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.track.StaticPathBuilder;

public class TrackGeomFactoryTest {
	TrackGeomFactory factory = new TrackGeomFactory();
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateTrackShape() throws Exception {

		
	}

	@Test
	public void testCreatePositionToPointConverter() throws Exception {
		
		assertThat(factory.createPositionToPointConverter(StaticPathBuilder.KEMPTON_740), Matchers.isA(PositionToPointConverter.class));
	}

}
