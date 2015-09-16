package com.ordint.tcpears.service;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.ordint.tcpears.domain.Position;

public interface AdministrationService {
	
	void startTracking(String groupId);
	
	void stopTracking(String groupId);
	
	void setClientGroup(String clientId, String groupId);
	
	void setClientsGroup(String[] clientId, String groupId);
	
	void setDefaultGroup(String groupId);
	
	void clearTrack(String groupId);
	
	void clearAllTracks();
	
	String replay(String start, String numberOfSeconds, boolean useOriginalTimeStamp);
	
	void cancelReplay(String replayId);
	
	void refreshClientDetails();
	
	ConcurrentMap<String, ConcurrentMap<String, String>> getGroupTracks();

	ConcurrentMap<String, List<Position>> groupClientsByGroup();
	
	
	void startRace(long raceId);
	
	void finishRace(long raceId);
}
