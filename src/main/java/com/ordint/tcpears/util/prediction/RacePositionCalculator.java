package com.ordint.tcpears.util.prediction;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.service.impl.RacePositionDecorator;
import com.ordint.tcpears.util.MeasuredShape;


public class RacePositionCalculator {
	private final static Logger log = LoggerFactory.getLogger(RacePositionCalculator.class);
	private MeasuredShape track;
	
	public RacePositionCalculator(Path2D trackPath) {
		track = new MeasuredShape(trackPath);		
	}
	
	public List<Position> calculate(Collection<Position> currentPositions) {
		 
		List<Position> updatedPositions = new ArrayList<>();
		List<PositionDistanceInfo> distances = new ArrayList<>();
		for(Position p : currentPositions) {
			PositionDistanceInfo pdi = track.calculateDistanceInfo(p);
			distances.add(pdi);
			
		}
		distances.sort((PositionDistanceInfo p1, PositionDistanceInfo p2) -> Double.compare(p2.getDistanceFromStart(), p1.getDistanceFromStart()));
		int i = 1;
		for(PositionDistanceInfo pdi : distances) {
			int standing = 0;
			if (pdi.getDistanceFromStart() > 0) {
				standing = i++;
			}
			updatedPositions.add(Position.builder()
					.position(pdi.getPosition())
					.standing(standing)
					.build());
		}
		
		return updatedPositions;
	}
	

}
