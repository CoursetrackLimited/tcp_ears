package com.ordint.tcpears.service.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.SectorTime;
import com.ordint.tcpears.service.AdministrationService;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.PositionPublisher;
import com.ordint.tcpears.service.RaceService;
import com.ordint.tcpears.service.ReplayService;
import com.ordint.tcpears.service.position.ClientManagerImpl;
import com.ordint.tcpears.service.race.RaceServiceException;

@Component("administrationService")
public class AdministrationServiceImpl implements AdministrationService {
	private final static Logger log = LoggerFactory.getLogger(AdministrationServiceImpl.class);
	@Autowired
	private  ClientManagerImpl clientManager;
	@Autowired
	private  ClientDetailsResolver clientDetailsResolver;
	@Autowired
	private  ReplayService replayService;
	@Autowired
	private PositionPublisher positionPublisher;
	
	@Autowired
	private RaceService raceService;

	@Override
	public void startTracking(String groupId) {
		clientManager.trackGroup(groupId);
	}

	@Override
	public void stopTracking(String groupId) {
		clientManager.stopTrackingGroup(groupId);
	}

	@Override
	public void setClientGroup(String clientId, String groupId) {
		clientDetailsResolver.updateClientDetails(new ClientDetails( groupId, clientId));
	}
	@Override
	public void setClientsGroup(String[] clientIds, String groupId) {
		for(String clientId : clientIds) {
			setClientGroup(clientId, groupId);
		}

	}

	@Override
	public void setDefaultGroup(String groupId) {
		clientDetailsResolver.setDefaultGroup(groupId);
	}

	@Override
	public void clearTrack(String groupId) {
		clientManager.clearSnake(groupId);
		positionPublisher.clearSnake(groupId);
	}
	@Override
	public void clearAllTracks() {
		clientManager.clearAllSnakes();
		positionPublisher.clearAllSnakes();
	}
	@Override
	public String replay(String start, String numberOfSeconds, boolean userOriginalTimeStamp) {
		String id = replayService.replayFrom(LocalDateTime.parse(start), Integer.parseInt(numberOfSeconds), userOriginalTimeStamp);
		log.info("returning replay id {}", id);
		return id;
	}

	@Override
	public void cancelReplay(String replayId) {
		replayService.endReplay(replayId);
	}

	@Override
	public void refreshClientDetails() {
		clientDetailsResolver.refresh();
		
	}

	@Override
	public ConcurrentMap<String, ConcurrentMap<String, String>> getGroupTracks() {
		return clientManager.getSnakes();
	}

	@Override
	public ConcurrentMap<String, List<Position>> groupClientsByGroup() {
		return clientManager.groupClientsByGroup();
	}

	@Override
	public void startRace(long raceId) throws RaceServiceException{
		try {
			raceService.startRace(raceId);
		} catch (DataAccessException | RaceServiceException e) {
			log.error("Error starting race with id {}", raceId, e);
			throw e;
		}
		
	}

	@Override
	public void finishRace(long raceId) {
		raceService.finishRace(raceId);
		
	}

	@Override
	public String replayRace(long raceId) throws RaceServiceException {
		
		try {
			return raceService.replayRace(raceId);
		} catch (Exception e) {
			log.error("Error replaying race with id {}", raceId, e);
			throw e;
		}
	}

	@Override
	public void updateClientDetails(ClientDetails clientDetails) {
		clientDetailsResolver.updateClientDetails(clientDetails);
		
	}

	@Override
	public Map<String, List<SectorTime>> getSectorTimes() {
		return raceService.getSectorTimes();
	}


}
