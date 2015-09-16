package com.ordint.tcpears.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.AdministrationService;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.PositionPublisher;
import com.ordint.tcpears.service.RaceService;
import com.ordint.tcpears.service.ReplayService;

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
		clientManager.clearTrack(groupId);
		positionPublisher.clearTrack(groupId);
	}
	@Override
	public void clearAllTracks() {
		clientManager.clearAllTracks();
		positionPublisher.clearAllTracks();
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
		return clientManager.getGroupTracks();
	}

	@Override
	public ConcurrentMap<String, List<Position>> groupClientsByGroup() {
		return clientManager.groupClientsByGroup();
	}

	@Override
	public void startRace(long raceId) {
		try {
			raceService.startRace(raceId);
		} catch (DataAccessException e) {
			log.error("Error starting race with id {}", raceId, e);
		}
		
	}

	@Override
	public void finishRace(long raceId) {
		raceService.finishRace(raceId);
		
	}


}
