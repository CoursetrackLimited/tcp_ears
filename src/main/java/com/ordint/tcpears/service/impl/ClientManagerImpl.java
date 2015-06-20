package com.ordint.tcpears.service.impl;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.ordint.tcpears.domain.DefaultOutputWriter;
import com.ordint.tcpears.domain.DefaultTrackWriter;
import com.ordint.tcpears.domain.OutputWriter;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.TrackWriter;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientManager;


/**
 * M
 * @author Tom
 *
 */
public class ClientManagerImpl implements ClientManager {
	
	//map of position keyed on clientId
	private final ConcurrentMap<String, Position> clients = new ConcurrentHashMap<>();
	//map of map of client tracks, keeyed on groupId
	private ConcurrentMap<String, ConcurrentMap<String, String>> groupTracks = new ConcurrentHashMap<>();
	
	private OutputWriter outputBuilder = new DefaultOutputWriter();
	
	private MemcacheHelper memcacheHelper;
	
	private ConcurrentMap<String, TrackWriter> groupsToTrack = new ConcurrentHashMap<>();
	
	private Clock clock = Clock.systemUTC();
	
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	public ClientManagerImpl(MemcacheHelper memcacheHelper) {
		this.memcacheHelper = memcacheHelper;
		
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					publishPositions();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		},
		1000, 333, TimeUnit.MILLISECONDS);
		
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					removeStaleClients();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		},
		5, 5, TimeUnit.MINUTES);
		
		
	}
	
	public ClientManagerImpl(MemcacheHelper memcacheHelper, Clock clock) {
		this.memcacheHelper = memcacheHelper;
		this.clock = clock;
	}
	
	public void publishPositions()  throws IOException {
		//A map of lists of postitions keyed on groupId
		ConcurrentMap<String, List<Position>> groups =  clients.values()
			.stream()
			.collect(Collectors.groupingByConcurrent(Position::getGroupId));
	
		//build memecache objects for each of the groups of positions	
		for(String groupId : groups.keySet()) {		
			ConcurrentMap<String, String> postionMap = groups.get(groupId)
					.stream()
					.collect(Collectors.toConcurrentMap(Position::getClientId, p -> outputBuilder.write(p)));

			//save to memcache
			String groupKeyName = "/ggps/locations/" + groupId;
			
			memcacheHelper.set(groupKeyName, groupKeyName, postionMap);
						
			publishTracks(groupId, postionMap);
		}	
		
	}

	private void publishTracks(String groupId, ConcurrentMap<String, String> postionMap) throws IOException {
		String groupTracksName="/ggps/tracks/" + groupId;
		TrackWriter trackWriter = groupsToTrack.get(groupId);
		if(trackWriter != null) {
			//build a trackMap by only selecting the tracks for clients that are currently in this group
			//as some may have been removed or timed out since the last trackMap was created
			Set<String> clientsInGroup = new HashSet<>(postionMap.keySet());
			trackWriter.calculateTrackLength(clientsInGroup.size());
			ConcurrentMap<String, String> trackMap = groupTracks.get(groupId).entrySet().stream()
					.filter(p -> clientsInGroup.contains(p.getKey()))
					.collect(Collectors.toConcurrentMap(p -> p.getKey(), p -> p.getValue()));
			
			//save to memcache
			//TODO: how do we handle failure
			memcacheHelper.set(groupTracksName, groupTracksName, trackMap);
			//replace existing track map with latest
			groupTracks.put(groupId, trackMap);
		} else {
			memcacheHelper.clear(groupTracksName, groupTracksName);
			groupTracks.remove(groupId);
		}
	}
	
	@Override
	public void trackGroup(String groupId) {
		groupsToTrack.put(groupId, new DefaultTrackWriter());
	}
	
	@Override
	public void stopTrackingGroup(String groupId) {
		groupsToTrack.remove(groupId);
	}
	
	@Override
	public void updatePostion(Position position) {
		clients.put(position.getClientId(), position);
		updateTracks(position);
	}
	
	public void removeStaleClients() {
		clients.entrySet().removeIf(p -> isOld(p.getValue()));
	}
	
	private boolean isOld(Position p) {
		return p.getTimeCreated().until(LocalDateTime.now(clock), ChronoUnit.SECONDS) > 300;
	}
	
	private void updateTracks(Position p) {
		TrackWriter trackWriter = groupsToTrack.get(p.getGroupId());
		
		if (trackWriter != null) {
			ConcurrentMap<String, String> map = getTrackMap(p.getGroupId());
			map.computeIfPresent(p.getClientId(), (key,value) -> trackWriter.write(p, value));			
			map.computeIfAbsent(p.getClientId(), value -> trackWriter.write(p, ""));
		}
	}
	
	
	private ConcurrentMap<String, String> getTrackMap(String groupId) {
		return groupTracks.computeIfAbsent(groupId, map -> new ConcurrentHashMap<>());
	}
	
	
	
}
