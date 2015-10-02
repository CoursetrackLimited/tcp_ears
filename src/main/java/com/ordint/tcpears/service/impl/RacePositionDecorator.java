package com.ordint.tcpears.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.service.PositionDecorator;
import com.ordint.tcpears.util.MeasuredShape;
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
		Map<PositionDistanceInfo, Position> ps = new HashMap<>();
		List<Position> updatedPositions = new ArrayList<>();
		List<PositionDistanceInfo> distances = new ArrayList<>();
		for(Position p : positions) {
			PositionDistanceInfo pdi = track.calculateDistanceInfo(p);
			distances.add(pdi);
			ps.put(pdi, p);
			
		}
		distances.sort((PositionDistanceInfo p1, PositionDistanceInfo p2) -> Double.compare(p2.getDistanceFromStart(), p1.getDistanceFromStart()));
		int i = 1;
		boolean logOut = false;
		for(PositionDistanceInfo pdi : distances) {
			int standing = 0;
			if (pdi.getDistanceFromStart() > 0) {
				standing = i++;
			} 
			updatedPositions.add(Position.builder()
					.position(ps.get(pdi))
					.standing(standing)
					.build());
		}

		return updatedPositions;
	}
	/**
	 * This method just returns the given position unmodifed, as to calculate the race standings we
	 * need all the other positions in the group
	 */
	@Override
	public Position decorate(Position position) {
		return position;
	}	
	

}

