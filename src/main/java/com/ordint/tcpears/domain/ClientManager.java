package com.ordint.tcpears.domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.server.Position;



public class ClientManager {
	private static final int MAX_TRACK_LENGTH = 1000;
	private static final TrackWriter TRACK_BUILDER = new DefaultTrackWriter();
	//map of position keyed on clientId
	private final ConcurrentMap<String, Position2> clients = new ConcurrentHashMap<>();
	//map of map of client tracks, keeyed on groupId
	private ConcurrentMap<String, ConcurrentMap<String, String>> groupTracks = new ConcurrentHashMap<>();
	
	private OutputBuilder outputBuilder = new DefaultOutputBuilder();
	
	private MemcacheHelper memcacheHelper;
	
	private Set<String> groupsToTrack = new HashSet<>();
	
	public ClientManager(MemcacheHelper memcacheHelper) {
		this.memcacheHelper = memcacheHelper;
	}
	
	public void publishPositions()  throws IOException {
		//A map of lists of postitions keyed on groupId
		ConcurrentMap<String, List<Position2>> groups =  clients.values()
			.stream()
			.collect(Collectors.groupingByConcurrent(Position2::getGroupId));
	
		
		//build memecache objects for each of the groups of positions
		
		for(String groupId : groups.keySet()) {
			
			ConcurrentMap<String, String> postionMap = groups.get(groupId)
					.stream().collect(Collectors.toConcurrentMap(Position2::getClientId, p -> outputBuilder.build(p)));
			
			//save to memcache
			String groupKeyName = "/ggps/locations/" + groupId;
			
			memcacheHelper.set(groupKeyName, groupKeyName, postionMap);
						
			publishTracks(groupId, postionMap);
		}	
		
	}

	private void publishTracks(String groupId, ConcurrentMap<String, String> postionMap) throws IOException {
		String groupTracksName="/ggps/tracks/" + groupId;
		if(groupsToTrack.contains(groupId)) {
			Set<String> clientsInGroup = new HashSet<>(postionMap.keySet());
			//build memcache track objects the required group
			ConcurrentMap<String, String> track = groupTracks.get(groupId).entrySet().stream()
					.filter(p -> clientsInGroup.contains(p.getKey()))
					.collect(Collectors.toConcurrentMap(p -> p.getKey(), p -> p.getValue()));
			
			//save to memcache
			memcacheHelper.set(groupTracksName, groupTracksName, track);
			//replace
			groupTracks.put(groupId, track);
		} else {
			memcacheHelper.set(groupTracksName, groupTracksName, new HashMap<String,String>());
			groupTracks.remove(groupId);
		}
	}
	
	public void trackGroup(String groupId) {
		groupsToTrack.add(groupId);
	}
	
	public void stopTrackingGroup(String groupId) {
		groupsToTrack.remove(groupId);
	}
	
	public void updatePostion(Position2 position) {
		clients.put(position.getClientId(), position);
		updateTracks(position);
	}
	
	public void removeStaleClients() {
		clients.entrySet().removeIf(p -> isOld(p.getValue()));
	}
	
	private boolean isOld(Position2 p) {
		return p.getTimeCreated().until(LocalDateTime.now(), ChronoUnit.SECONDS) > 300;
	}
	
	private void updateTracks(Position2 p) {
		if (groupsToTrack.contains(p.getGroupId())) {
			ConcurrentMap<String, String> map = getTrackMap(p.getGroupId());
			map.computeIfPresent(p.getClientId(), (key,value) -> getTrackBuilder(p.getGroupId()).write(p, value));			
			map.computeIfAbsent(p.getClientId(), value -> getTrackBuilder(p.getGroupId()).write(p, ""));
		}
	}
	
	private TrackWriter getTrackBuilder(String groupId) {	
		return TRACK_BUILDER;
	}
	
	private ConcurrentMap<String, String> getTrackMap(String groupId) {
		return groupTracks.computeIfAbsent(groupId, map -> new ConcurrentHashMap<>());
	}
	
	
	
}
