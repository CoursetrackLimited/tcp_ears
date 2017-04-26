package com.ordint.tcpears.service.race;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.lombok.ClientDetails;
import com.ordint.tcpears.domain.lombok.Position;
import com.ordint.tcpears.domain.lombok.PositionDistanceInfo;
import com.ordint.tcpears.domain.lombok.RaceDetail;
import com.ordint.tcpears.domain.lombok.Sector;
import com.ordint.tcpears.domain.lombok.SectorTime;
import com.ordint.tcpears.service.position.PositionEnhancer;
import com.ordint.tcpears.track.SectorTimeCalculator;
import com.ordint.tcpears.track.Track;
import com.ordint.tcpears.util.DateUtil;



public class RaceObserver implements PositionEnhancer {
	
	private ConcurrentMap<String, PositionDistanceInfo> distances = new ConcurrentHashMap<>();
	private ConcurrentMap<String, Integer> standings = new ConcurrentHashMap<>();
	
	private final static Logger log = LoggerFactory.getLogger(RaceObserver.class);
	
	public enum EventState {PRE_RACE, UNDER_STARTERS_ORDERS, STARTED, FINSIHED};
	
	private volatile EventState status = EventState.PRE_RACE;
	
	private List<RaceStatusListener> statusListeners = new ArrayList<>();
	
	private ConcurrentMap<String, Integer> placings = new ConcurrentHashMap<>();
	private RaceDetail race;

	private Track track;
	private int runnerCount;
	private List<ClientDetails> runners;
	private SectorTimeCalculator sectorTimeCalculator;
	
	private static final Comparator<LocalDateTime> TIMESTAMP_COMPARATOR = new Comparator<LocalDateTime>() {
        @Override
        public int compare(LocalDateTime o1, LocalDateTime o2) {
            return o1.compareTo(o2);
        }
    };
	
	public RaceObserver(Track track, List<ClientDetails> runners, RaceDetail race) {
		this.track = track;
		this.runnerCount = runners.size();
		this.runners = runners;
		this.sectorTimeCalculator = new SectorTimeCalculator(track.getSectors());
		this.race = race;
	}
	
	
	@Override
	public List<Position> decorate(List<Position> positions) {
		if (status == EventState.PRE_RACE) {
			checkIfStarted(positions);
		}
		
		calculateStandings();
		return addStandingsToPositions(positions);
	}

	private void checkForRaceStart(List<Position> positions) {
		if (status == EventState.PRE_RACE) {
			checkIfUnderStartersOrders(positions);
		} else if(status == EventState.UNDER_STARTERS_ORDERS) {
			checkIfStarted(positions);
		}		
	}

	private void checkIfStarted(List<Position> positions) {
		if (isAfterRaceScheduledStart(positions)) {
			long runningHorsesCount = positions.stream().filter(p -> p.getSpeedValue() > 5).count();
			if (runningHorsesCount > runnerCount * .70) {
				updateStatus(EventState.STARTED);
			}
		}
	}
	
	private boolean isAfterRaceScheduledStart(List<Position> positions) {
		return positions.stream()
				.map(Position::getTimeCreated)
				.max(TIMESTAMP_COMPARATOR)
				.orElseGet(LocalDateTime::now)
				.isAfter(DateUtil.ukLocalDateTimeToUTC(race.getScheduledStartTime()));
	}
	
	private void updateStatus(EventState status) {
		
		if (this.status != status) {
			log.info("Status updated to {}", status);
			this.status = status;
			statusListeners.forEach(l -> l.onStatusChange(status));
		}
	}


	private void checkIfUnderStartersOrders(List<Position> positions) {
		if (positions.size() > 3) {
			long stillHorsesCount = positions.stream().filter(p -> p.getSpeedValue() < 0.2).count();
			if (stillHorsesCount == runnerCount) {
				updateStatus(EventState.UNDER_STARTERS_ORDERS);
			}
		}
		
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

	private void calculateStandings() {
		List<PositionDistanceInfo> stillRunning = new ArrayList<>();	
		for(PositionDistanceInfo pdi : distances.values()) {
			
			//HACK 
			stillRunning.add(pdi);	
/*			if(pdi.getDistanceFromFinish() >= 0) {
				stillRunning.add(pdi);			
			} else {
				//crossed the finish line
				placings.putIfAbsent(pdi.getClientId(), placings.size() + 1);
			}*/
		}
		if (placings.size() == runnerCount && status != EventState.FINSIHED) {
			updateStatus(EventState.FINSIHED);
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
		//if (status == EventState.STARTED ) {		

			
		//}
		PositionDistanceInfo pdi = null;
		if (status == EventState.STARTED) {
			 pdi = track.calculateDistanceInfo(p);	
			distances.put(p.getClientId(), pdi);
			sectorTimeCalculator.checkSector(p, pdi.getDistanceFromStart());
		}
		return Position.builder().position(p).distinceInfo(pdi).build();
	}
	
	public void addRaceStatusListener(RaceStatusListener listener) {
		statusListeners.add(listener);
	}
	
	public Map<String, List<SectorTime>> getSectorTimes() {
		 Map<String, List<SectorTime>> retval = new HashMap<>();
		 for(ClientDetails cd : runners) {
			 retval.put(cd.getRunnerIdent() + " : " + cd.getTempName(), sectorTimeCalculator.getSectorTimes(cd.getClientId()));
		 }
		 return retval;
	}
	
	public long getRaceId() {
		return race.getId();
	}


	public String[] getReportHeader() {
		ArrayList list = new ArrayList<>();
		list.add("Runner");
		list.addAll(sectorTimeCalculator.getSectors().stream().map(Sector::getName).collect(Collectors.toList()));
		return (String[]) list.toArray(new String[] {});
	}

}
