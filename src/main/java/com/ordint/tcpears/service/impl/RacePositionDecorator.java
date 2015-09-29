package com.ordint.tcpears.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.service.PositionDecorator;
import com.ordint.tcpears.util.MeasuredShape;
import com.ordint.tcpears.util.prediction.RacePositionCalculator;
import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;



public class RacePositionDecorator implements PositionDecorator {
	private final static Logger log = LoggerFactory.getLogger(RacePositionDecorator.class);
	
	private StaticTrackPathBuilder pathBuilder = new StaticTrackPathBuilder();
	

	private MeasuredShape track;
	public RacePositionDecorator() {
		track = new MeasuredShape(pathBuilder.build(StaticTrackPathBuilder.KEMPTON_740_TRACK));
	}

	@Override
	public List<Position> decorate(List<Position> positions) {
		
		List<Position> updatedPositions = new ArrayList<>();
		List<PositionDistanceInfo> distances = new ArrayList<>();
		for(Position p : positions) {
			PositionDistanceInfo pdi = track.calculateDistanceInfo(p);
			distances.add(pdi);
			
		}
		distances.sort((PositionDistanceInfo p1, PositionDistanceInfo p2) -> Double.compare(p2.getDistanceFromStart(), p1.getDistanceFromStart()));
		int i = 1;
		boolean logOut = false;
		for(PositionDistanceInfo pdi : distances) {
			int standing = 0;
			if (pdi.getDistanceFromStart() > 0) {
				standing = i++;
			} else {
				logOut = true;
			}
			updatedPositions.add(Position.builder()
					.position(pdi.getPosition())
					.standing(standing)
					.build());
		}
/*		if (logOut) {
			for(Position p : updatedPositions) {
				if(p.getStanding() == 0) {
					log.info("No Position : {}, {}, {}, {}", p.getLat(), p.getLon(), p.getTimestamp(), p.getClientDetails().getFixedName());;
				}
				
			}
		}*/
		return updatedPositions;
	}	
	

}
