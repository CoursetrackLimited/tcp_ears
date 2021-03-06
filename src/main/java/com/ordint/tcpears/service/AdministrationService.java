package com.ordint.tcpears.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.ordint.tcpears.domain.lombok.ClientDetails;
import com.ordint.tcpears.domain.lombok.Position;
import com.ordint.tcpears.domain.lombok.SectorTime;
import com.ordint.tcpears.service.race.RaceServiceException;

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
	
	
	void startRace(long raceId) throws RaceServiceException;
	
	void finishRace(long raceId);
	
	String replayRace(long raceId) throws RaceServiceException;
	
	void updateClientDetails(ClientDetails clientDetails);
	
	Map<String, List<SectorTime>> getSectorTimes();
	
}
