package com.ordint.tcpears.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.memcache.Memcached;
import com.ordint.tcpears.server.Position;

public class GroupPositions {
	private static final int TRACK_LENGTH_FOR_1MBCACHE = 26000; //enough for 40 clients with 1mb item size in memcache
	private static final int MAX_TRACK_LENGTH = TRACK_LENGTH_FOR_1MBCACHE * 3;
	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> groups = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> tracks = new ConcurrentHashMap<>();
	private MemcacheHelper memcacheHelper;
	private ScheduledExecutorService executor; 
	
	public GroupPositions(MemcacheHelper memcacheHelper) {
		this.memcacheHelper = memcacheHelper;
		executor = Executors.newSingleThreadScheduledExecutor();
		init();
	}
	
	private void init() {
		// hardcode list of groups for now
		//"GR1"
		//memcacheHelper.getMap(namespace, objectKey)
		executor.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {

				for(String groupId : groups.keySet()){
					String groupKeyName = "/ggps/locations/" + groupId;
					String groupTracksName="/ggps/tracks/" + groupId;
					ConcurrentHashMap<String, String> group = groups.get(groupId);
					
					ConcurrentHashMap<String, String> track = tracks.get(groupId);
					try {
						memcacheHelper.set(groupKeyName, groupKeyName, group);
						memcacheHelper.set(groupTracksName, groupTracksName, track);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
				}
				
			}
		}, 1000, 333, TimeUnit.MILLISECONDS);
	}
	
	public void update(Position position) {
		
		groupsMap(position.getGroupId()).put(position.getClientId(), position.asMessage());
		updateTracks(position);
	}
	
	private ConcurrentHashMap<String, String> groupsMap(String groupId) {
		return groups.computeIfAbsent(groupId, map -> new ConcurrentHashMap<>());
	}
	private ConcurrentHashMap<String, String> tracksMap(String groupId) {
		return tracks.computeIfAbsent(groupId, map -> new ConcurrentHashMap<>());
	}
	
	private void updateTracks(Position p) {
		ConcurrentHashMap<String, String> map = tracksMap(p.getGroupId());
		map.computeIfPresent(p.getClientId(), (k,value) -> { 
			StringBuilder strTracks = new StringBuilder(value);
			if(strTracks.length() > MAX_TRACK_LENGTH) {
				int lastTripleIndex = strTracks.lastIndexOf(" ", strTracks.length()-100);
				strTracks.delete(lastTripleIndex + 1, strTracks.length() );
			}
			return p.concatTrack(strTracks.toString());
		});
		
		map.computeIfAbsent(p.getClientId(), val -> {return p.concatTrack("");});

	}
}
