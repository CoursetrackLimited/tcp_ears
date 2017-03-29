package com.ordint.tcpears.service;

import java.util.List;
import java.util.Map;

import com.ordint.tcpears.domain.SectorTime;
import com.ordint.tcpears.service.race.RaceServiceException;

public interface RaceService {

	void startRace(long raceId) throws RaceServiceException;
	
	void finishRace(long raceId);
	
	String replayRace(long raceId) throws RaceServiceException;
	
	Map<String, List<SectorTime>> getSectorTimes();
}
