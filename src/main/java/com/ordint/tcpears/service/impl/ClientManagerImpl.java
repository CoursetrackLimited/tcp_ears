package com.ordint.tcpears.service.impl;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ordint.tcpears.domain.DefaultTrackWriter;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.TrackWriter;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionDataProvider;


/**
 * M
 * @author Tom
 *
 */

public class ClientManagerImpl implements ClientManager, PositionDataProvider {


	private static final int OLD_CLIENT_TIMEOUT_SECONDS = 7200;

	private final static Logger log = LoggerFactory.getLogger(ClientManagerImpl.class);
	
	//map of position keyed on clientId
	private final ConcurrentMap<String, Position> clients = new ConcurrentHashMap<>();
	//map of map of client tracks, keyed on groupId
	private ConcurrentMap<String, ConcurrentMap<String, String>> groupTracks = new ConcurrentHashMap<>();
	private ConcurrentMap<String, TrackWriter> groupsToTrack = new ConcurrentHashMap<>();	
	private Clock clock = Clock.systemUTC();
	
	@Autowired
	private PositionDecorators positionDecorators = new PositionDecorators();
	

	public ClientManagerImpl(){
	}
	
	public ClientManagerImpl(Clock clock) {

		this.clock = clock;

	}
	

	
	@Override
	public void trackGroup(String groupId) {
		log.debug("Tracking group {}", groupId);
		groupsToTrack.put(groupId, new DefaultTrackWriter());
	}
	
	@Override
	public void stopTrackingGroup(String groupId) {
		log.debug("Stop Tracking group {}", groupId);
		groupsToTrack.remove(groupId);
	}
	
	@Override
	public void updatePostion(Position position1) {
		Position position = positionDecorators.applyPositionDecorators(position1);
		clients.compute(position.getClientId(), (k, v) -> { if(v==null) {
			return position;
		} else {
			return position.smoothAltitude(v).setPreviousLatLon(v);	
		}});
		
		updateTracks(position);
	}
	
	public void removeStaleClients() {
		clients.entrySet().removeIf(p -> isOld(p.getValue()));
		
	}
	
	private boolean isOld(Position p) {
/*		if(p.getTimeCreated().until(LocalDateTime.now(clock), ChronoUnit.SECONDS) > OLD_CLIENT_TIMEOUT_SECONDS) {
			return true;
		}*/
		return false;
	}
	
	private void updateTracks(Position p) {
		TrackWriter trackWriter = groupsToTrack.get(p.getGroupId());		
		if (trackWriter != null) {
			ConcurrentMap<String, String> map = getTrackMap(p.getGroupId());
			String track = map.computeIfPresent(p.getClientId(), (key,value) -> trackWriter.write(p, value));
			if (track == null) {
				track = map.computeIfAbsent(p.getClientId(), value -> trackWriter.write(p, ""));
			}
	  
		}
	}
	

	
	public ConcurrentMap<String, String> getTrackMap(String groupId) {
		return groupTracks.computeIfAbsent(groupId, map -> new ConcurrentHashMap<>());
	}

	@Override
	public void clearTrack(String groupId) {
		groupTracks.remove(groupId);
	}
	@Override
	public void clearAllTracks() {
		groupTracks.clear();
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.impl.PositionDataProvider#getGroupTracks()
	 */
	@Override
	public ConcurrentMap<String, ConcurrentMap<String, String>> getGroupTracks() {
		ConcurrentMap<String, List<Position>> trackedClients =  groupClientsByTrackedGroup();
		for(String groupId : groupTracks.keySet()) {
			List<Position> clients = trackedClients.get(groupId);
			if (clients == null) {
				groupTracks.remove(groupId);
			} else {
				Set<String> clientsInGroup =  trackedClients.get(groupId)
						.stream()
						.map(p -> p.getClientId())
						.collect(Collectors.toSet());
				groupTracks.put(groupId, filterTrackMap(groupTracks.get(groupId), clientsInGroup));
				groupsToTrack.get(groupId).calculateTrackLength(clientsInGroup.size());
			}
		}
		return groupTracks;
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.impl.PositionDataProvider#groupClientsByGroup()
	 */
	@Override
	public ConcurrentMap<String, List<Position>> groupClientsByGroup() {
		
		ConcurrentMap<String, List<Position>> groups = clients.values()
				.stream()
				.collect(Collectors.groupingByConcurrent(Position::getGroupId));
		
		for(String groupId: groups.keySet()) {
			groups.replace(groupId, positionDecorators.applyGroupDecorators(groupId,groups.get(groupId)));
		}
		return groups;
	}
		
	private ConcurrentMap<String, List<Position>> groupClientsByTrackedGroup() {
		return clients.values()
				.stream()
				.filter(p -> !isOld(p))
				.filter(p -> groupsToTrack.keySet().contains(p.getGroupId()))
				.collect(Collectors.groupingByConcurrent(Position::getGroupId));
	}	
	
	private ConcurrentMap<String, String> filterTrackMap(ConcurrentMap<String, String> trackMap, Set<String> clientsInGroup) {
		return trackMap
				.entrySet()
				.stream()
				.filter(p -> clientsInGroup.contains(p.getKey()))
				.collect(Collectors.toConcurrentMap(p -> p.getKey(), p -> p.getValue()));
		
	}
	

}
