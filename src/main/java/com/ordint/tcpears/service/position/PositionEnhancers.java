package com.ordint.tcpears.service.position;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.RaceDetail;
import com.ordint.tcpears.domain.TrackConfig;
import com.ordint.tcpears.service.race.RaceObserver;
import com.ordint.tcpears.track.Track;
import com.ordint.tcpears.track.geom.TrackGeomFactory;

@Component
public class PositionEnhancers {
	private final static Logger log = LoggerFactory.getLogger(PositionEnhancers.class);
	private Map<String, List<PositionEnhancer>> positionEnhancers = new HashMap<>();
	private TrackGeomFactory trackGeomFactory = new TrackGeomFactory();	
	
	public PositionEnhancers() {}
	
	public List<Position> applyGroupPositionEnhancer(String groupId, List<Position> positions) {
		List<PositionEnhancer> enhancers = positionEnhancers.get(groupId);
		if (enhancers == null) {
			return positions;
		} else {
			return applyGroupPositionEnhancers(positions, 0, enhancers);
		}		
	}
	
	public Position applyPositionEnhancers(Position position) {
		List<PositionEnhancer> enhancers = positionEnhancers.get(position.getGroupId());
		if (enhancers == null) {
			return position;
		} else {
			return applyPositionEnhancers(position, 0, enhancers);
		}		
	}

	
	public RaceObserver addRacePositionEnhancers(RaceDetail race, TrackConfig trackConfig, List<ClientDetails> runners) {
		log.info("Adding race  psoition enhancers for groupId {}", race.getGroupId());
		Track track = new Track(trackConfig, trackGeomFactory, 1000);
		RaceObserver observer = new RaceObserver(track, runners);
		List<PositionEnhancer> pd = Arrays.asList(observer);
		//List<PositionEnhancer> pd = Arrays.asList(new RacePositionDecorator());
		positionEnhancers.put(race.getGroupId().toString(), pd);
		return observer;
	}
	
	public void clearEnhancers(String groupId) {
		log.info("Clearing enhancers"); 
		positionEnhancers.clear();
	}
	
	private List<Position> applyGroupPositionEnhancers(List<Position> positions, int index, List<PositionEnhancer> decorators) {
		if (index == decorators.size())
			return positions;
		else
			return decorators.get(index).decorate(applyGroupPositionEnhancers(positions, index +1, decorators));
	}
	
	private Position applyPositionEnhancers(Position position, int index, List<PositionEnhancer> decorators) {
		if (index == decorators.size())
			return position;
		else
			return decorators.get(index).enhance(applyPositionEnhancers(position, index +1, decorators));
	}

}
