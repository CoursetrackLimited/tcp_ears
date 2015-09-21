package com.ordint.tcpears.util.prediction;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionUtil;

public class RacePositionCalculatorTest {
	
	private StaticTrackPathBuilder trackBuilder = new StaticTrackPathBuilder();
	private RacePositionCalculator rpCalculator = new RacePositionCalculator(trackBuilder.build(StaticTrackPathBuilder.KEMPTON_740_TRACK));
	@Before
	public void setUp() throws Exception {
	
	}

	@Test
	public void testCalculate() throws Exception {
		List<Position> raceGroup = new ArrayList<>();
		raceGroup.add(PositionUtil.createPosition(" 51.419872", " -0.401605", "1", "group", "A"));
		//
		
		
		
		raceGroup.add(PositionUtil.createPosition(" 51.419883", " -0.401636", "1", "group", "B"));
		//
		
		raceGroup.add(PositionUtil.createPosition("51.419884", " -0.401674", "1", "group", "C"));
		
		Map<String, RacePosition> standings = rpCalculator.calculate(raceGroup);
		
		standings.values().forEach(x -> System.out.println(x + " " + x.getMetersFromStart()));
		
		assertThat(standings.get("A").getStanding(), equalTo(1));
		assertThat(standings.get("B").getStanding(), equalTo(2));
		assertThat(standings.get("C").getStanding(), equalTo(3));
		assertThat(standings.get("A").getDistanceFromStart(), Matchers.greaterThan(0d));
		
	}

}
