package com.ordint.tcpears.service.race;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.position.PositionEnhancer;
import com.ordint.tcpears.service.race.RaceObserver.EventState;

public class RaceStatusObserver implements PositionEnhancer {
	private EventState status = EventState.PRE_RACE;
	private final static Logger log = LoggerFactory.getLogger(RaceStatusObserver.class);
	private int runnerCount;
	private List<RaceStatusListener> statusListeners = new ArrayList<>();
	
	public RaceStatusObserver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Position> decorate(List<Position> positions) {
		
		checkForRaceStart(positions);
		
		return positions;
	}

	private void checkForRaceStart(List<Position> positions) {
		if (status == EventState.PRE_RACE) {
			checkIfUnderStartersOrders(positions);
		} else if(status == EventState.UNDER_STARTERS_ORDERS) {
			checkIfStarted(positions);
		}		
	}

	private void checkIfStarted(List<Position> positions) {		
		long runningHorsesCount = positions.stream().filter(p -> p.getSpeedValue() > 5).count();
		if (runningHorsesCount > runnerCount * .70) {
			updateStatus(EventState.STARTED);
		}	
	}
	
	private void updateStatus(EventState status) {
		log.info("Status updated to {}", status);
		this.status = status;
		statusListeners.forEach(l -> l.onStatusChange(status));
	}


	private void checkIfUnderStartersOrders(List<Position> positions) {
		if (positions.size() > 3) {
			long stillHorsesCount = positions.stream().filter(p -> p.getSpeedValue() < 0.2).count();
			if (stillHorsesCount == runnerCount) {
				updateStatus(EventState.UNDER_STARTERS_ORDERS);
			}
		}	
	}
	
	@Override
	public Position enhance(Position position) {
		return position;
	}

}
