package com.ordint.tcpears.service.race;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.service.position.PositionEnhancer;
import com.ordint.tcpears.track.StaticPathBuilder;
import com.ordint.tcpears.track.Track;
import com.ordint.tcpears.track.geom.MeasuredShape;



public class RaceObserver implements PositionEnhancer {
	
	private ConcurrentMap<String, PositionDistanceInfo> distances = new ConcurrentHashMap<>();
	private ConcurrentMap<String, Integer> standings = new ConcurrentHashMap<>();
	
	private final static Logger log = LoggerFactory.getLogger(RaceObserver.class);
	
	private StaticPathBuilder pathBuilder = new StaticPathBuilder();
	
	public enum RaceStatus {PRE_RACE, UNDER_STARTERS_ORDERS, STARTED, FINSIHED};
	
	private RaceStatus status = RaceStatus.PRE_RACE;
	
	private List<RaceStatusListener> statusListeners = new ArrayList<>();
	
	private ConcurrentMap<String, Integer> placings = new ConcurrentHashMap<>();

	private Track track;
	private long raceId;
	private int runnerCount;
	
	public RaceObserver(int runnerCount) {
		track = new Track(StaticPathBuilder.KEMPTON_740_TRACK, StaticPathBuilder.KEMPTON_FINISH);
		this.runnerCount = runnerCount;
	}
	
	public RaceObserver(Track track) {
		this.track = track;
		//this.runnerCount = runnerCount;
	}

	@Override
	public List<Position> decorate(List<Position> positions) {
		
		checkForRaceStart(positions);
		
		return addStandings(positions);
	}

	private void checkForRaceStart(List<Position> positions) {
		if (status == RaceStatus.PRE_RACE) {
			checkIfUnderStartersOrders(positions);
		} else if(status == RaceStatus.UNDER_STARTERS_ORDERS) {
			checkIfStarted(positions);
		}		
	}

	private void checkIfStarted(List<Position> positions) {
		long runningHorsesCount = positions.stream().filter(p -> p.getSpeedValue() > 5).count();
		if (runningHorsesCount > runnerCount * .70) {
			updateStatus(RaceStatus.STARTED);
		}	
	}
	
	private void updateStatus(RaceStatus status) {
		log.info("Status updated to {}", status);
		this.status = status;
		statusListeners.forEach(l -> l.onStatusChange(status));
	}


	private void checkIfUnderStartersOrders(List<Position> positions) {
		if (positions.size() > 3) {
			long stillHorsesCount = positions.stream().filter(p -> p.getSpeedValue() < 0.2).count();
			if (stillHorsesCount == runnerCount) {
				updateStatus(RaceStatus.UNDER_STARTERS_ORDERS);
			}
		}
		
	}
	
	
	private List<Position> addStandings(List<Position> positions) {
		
		if (status == RaceStatus.PRE_RACE || status == RaceStatus.UNDER_STARTERS_ORDERS) {
			//do nothing
			return positions;
		}
		if (status == RaceStatus.STARTED) {
			calculateStandings(positions);
		}
		return addStandingsToPositions(positions);			
	}

	private List<Position> addStandingsToPositions(List<Position> positions) {
		List<Position> updatedPositions = new ArrayList<>();
		for(Position p : positions) {
			int standing = standings.getOrDefault(p.getClientId(), 0);
			if (placings.containsKey(p.getClientId())) {
				standing = placings.get(p.getClientId());
			}
			updatedPositions.add(Position.builder()
					.position(p)
					.standing(standing)
					.build());
						
		}
		return updatedPositions;
	}

	private void calculateStandings(List<Position> positions) {
		List<PositionDistanceInfo> stillRunning = new ArrayList<>();	
		for(PositionDistanceInfo pdi : distances.values()) {
			if(pdi.getDistanceFromFinish() >= 0) {
				stillRunning.add(pdi);			
			} else {
				//crossed the finish line
				placings.putIfAbsent(pdi.getClientId(), placings.size() + 1);
			}
		}
		if (placings.size() == runnerCount && status != RaceStatus.FINSIHED) {
			updateStatus(RaceStatus.FINSIHED);
		}
		
		stillRunning.sort((PositionDistanceInfo pdi1, PositionDistanceInfo pdi2) -> Double.compare(pdi2.getDistanceFromStart(), pdi1.getDistanceFromStart()));
		int i = 1 + placings.size();
		for(PositionDistanceInfo pdi : stillRunning) {
			standings.put(pdi.getClientId(),  i++);		
		}
	}
	/**
	 * This method just returns the given position unmodifed, as to calculate the race standings we
	 * need all the other positions in the group. BUT it does need to be called as internally
	 * the Position's position on the track is calculated, and this is used to decorate the 
	 * List of Positions when {@link #decorate(List)} is called
	 */
	@Override
	public Position enhance(Position p) {	
		if (status == RaceStatus.STARTED ) {		
			PositionDistanceInfo pdi = track.calculateDistanceInfo(p);	
			distances.put(p.getClientId(), pdi);
		}
		return p;
	}
	
	public void addRaceStatusListener(RaceStatusListener listener) {
		statusListeners.add(listener);
	}

}
