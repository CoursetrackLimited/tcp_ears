package com.ordint.tcpears.service;

public interface RaceService {

	void startRace(long raceId);
	
	void finishRace(long raceId);
	
	void replayRace(long raceId);
}
