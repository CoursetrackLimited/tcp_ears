package com.ordint.tcpears.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.PositionDecorator;
import com.ordint.tcpears.util.prediction.RacePosition;
import com.ordint.tcpears.util.prediction.RacePositionCalculator;
import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;



public class RacePositionDecorator implements PositionDecorator {
	private final static Logger log = LoggerFactory.getLogger(RacePositionDecorator.class);
	
	private StaticTrackPathBuilder pathBuilder = new StaticTrackPathBuilder();
	
	private RacePositionCalculator racePositionCalculator = new RacePositionCalculator(pathBuilder.build(StaticTrackPathBuilder.KEMPTON_740_TRACK));
	
	public RacePositionDecorator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Position> decorate(List<Position> positions) {
		
		Map<String, RacePosition> standings = racePositionCalculator.calculate(positions);
		List<Position> updatedPositions = new ArrayList<>();
		for(Position p : positions) {
			RacePosition rp = standings.get(p.getClientId());
			if (rp != null) {
				updatedPositions.add(Position.builder()
						.position(p)
						.standing(standings.get(p.getClientId()).getStanding())
						.build());
			} else {
				//log.info("No position info for {} \n{}", p.getClientDetails().getFixedName());
				//log.info("LatLong {},{}", p.getLat(), p.getLon());
				
				updatedPositions.add(p);
			}
			
		}
		return updatedPositions;
	}	
	

}
