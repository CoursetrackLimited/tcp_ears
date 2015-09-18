package com.ordint.tcpears.service;

import com.ordint.tcpears.service.impl.RaceServiceException;

public interface RaceService {

	void startRace(long raceId) throws RaceServiceException;
	
	void finishRace(long raceId);
	
	String replayRace(long raceId) throws RaceServiceException;
}
