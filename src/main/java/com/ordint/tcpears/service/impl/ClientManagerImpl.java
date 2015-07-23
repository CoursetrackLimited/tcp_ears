package com.ordint.tcpears.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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
	private static final String TRACK_PREFIX = "/ggps/tracks/";

	private final static Logger log = LoggerFactory.getLogger(ClientManagerImpl.class);
	
	private final static String TRACKING_LIST = "/ggps/trackinglist";
	//map of position keyed on clientId
	private final ConcurrentMap<String, Position> clients = new ConcurrentHashMap<>();
	//map of map of client tracks, keeyed on groupId
	private ConcurrentMap<String, ConcurrentMap<String, String>> groupTracks = new ConcurrentHashMap<>();
	
	private OutputWriter outputBuilder = new DefaultOutputWriter();
	
	private MemcacheHelper memcacheHelper;
	
	private ConcurrentMap<String, TrackWriter> groupsToTrack = new ConcurrentHashMap<>();
	
	private Clock clock = Clock.systemUTC();
	
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
	
	private JdbcTemplate jdbcTemplate;

	public ClientManagerImpl(MemcacheHelper memcacheHelper, JdbcTemplate jdbcTempalate) {
		this.memcacheHelper = memcacheHelper;
		this.jdbcTemplate = jdbcTempalate;
		
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					publishPositions();
				} catch (Exception e) {
					log.error("error publishing postions", e);
				}
			}
		},
		1000, 100, TimeUnit.MILLISECONDS);

		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					publishTracks();
				} catch (Exception e) {
					log.error("error publishing tracks", e);
				}
			}
		},
		1500, 333, TimeUnit.MILLISECONDS);
		
	}
	
	public ClientManagerImpl(MemcacheHelper memcacheHelper, Clock clock, JdbcTemplate jdbcTempalate) {
		this.memcacheHelper = memcacheHelper;
		this.clock = clock;
		this.jdbcTemplate = jdbcTempalate;
	}
	
	protected void publishPositions()  throws IOException {
		//A map of lists of postitions keyed on groupId
		ConcurrentMap<String, List<Position>> groups =  getAllGroups();
	
		//build memecache objects for each of the groups of positions	
		for(String groupId : groups.keySet()) {		
			ConcurrentMap<String, String> postionMap = groups.get(groupId)
					.stream()
					.collect(Collectors.toConcurrentMap(Position::getClientId, p -> outputBuilder.write(p)));

			//save to memcache
			String groupKeyName = "/ggps/locations/" + groupId;
			
			memcacheHelper.set(groupKeyName, groupKeyName, postionMap);
		}	
		
	}
	
	private ConcurrentMap<String, List<Position>> getAllGroups() {
		return clients.values()
				.stream()
				.collect(Collectors.groupingByConcurrent(Position::getGroupId));
	}
	
	private  ConcurrentMap<String, List<Position>> getGroups(Set<String> groupsToInclude) {
		return clients.values()
				.stream()
				.filter(p -> groupsToInclude.contains(p.getGroupId()))
				.collect(Collectors.groupingByConcurrent(Position::getGroupId));		
	}
	
	protected void publishTracks() throws Exception {
		ConcurrentMap<String, List<Position>> groups =  getGroups(groupsToTrack.keySet());
		for(String groupId : groups.keySet()) {
			Set<String> clientsInGroup =  groups.get(groupId)
					.stream()
					.map(p -> p.getClientId())
					.collect(Collectors.toSet());
			publishTrack(groupId, clientsInGroup);
		
		}
 	}
	
	private void publishTrack(String groupId, Set<String> clientsInGroup) throws IOException {
		String groupTracksName= TRACK_PREFIX + groupId;
		TrackWriter trackWriter = groupsToTrack.get(groupId);
		if(trackWriter != null) {
			//build a trackMap by only selecting the tracks for clients that are currently in this group
			//as some may have been removed or timed out since the last trackMap was updated
			trackWriter.calculateTrackLength(clientsInGroup.size());
			ConcurrentMap<String, String> trackMap = getTrackMap(groupId).entrySet().stream()
					.filter(p -> clientsInGroup.contains(p.getKey()))
					.collect(Collectors.toConcurrentMap(p -> p.getKey(), p -> p.getValue()));
			
			//save to memcache
			//TODO: how do we handle failure
			memcacheHelper.set(groupTracksName, groupTracksName, trackMap);
			//replace existing track map with latest
			groupTracks.put(groupId, trackMap);
		} 
		//memcacheHelper.set(TRACKING_LIST, TRACKING_LIST, StringUtils.join(groupTracks.keySet(), ","));
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
	public void updatePostion(Position position) {
		clients.put(position.getClientId(), position);
		updateTracks(position);
	}
	
	public void removeStaleClients() {
		clients.entrySet().removeIf(p -> isOld(p.getValue()));
		
	}
	
	private boolean isOld(Position p) {
		if(p.getTimeCreated().until(LocalDateTime.now(clock), ChronoUnit.SECONDS) > 300) {
			Timestamp t = Timestamp.valueOf(p.getTimeCreated());
			jdbcTemplate.update("insert into clients (client_ident, last_seen) values (?, ?) " +
                " on duplicate key UPDATE last_seen=?", p.getClientId(), t, t);
			return true;
		}
		return false;
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

	@Override
	public void clearTrack(String groupId) {
		String groupTracksName = TRACK_PREFIX + groupId;
		memcacheHelper.clear(groupTracksName, groupTracksName);
		groupTracks.remove(groupId);
	}
	
	
	
}
