package com.ordint.tcpears.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ordint.tcpears.domain.DefaultOutputWriter;
import com.ordint.tcpears.domain.DefaultTrackWriter;
import com.ordint.tcpears.domain.OutputWriter;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.TrackWriter;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.util.PredictionUtil;
import com.ordint.tcpears.util.prediction.PositionPredictor;
import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;
import com.ordint.tcpears.util.prediction.TrackBasedPredictor;


/**
 * M
 * @author Tom
 *
 */
public class ClientManagerImpl2 implements ClientManager {
	//TODO:remove
	private static final String TRACK_PREFIX = "/ggps/tracks/";

	private final static Logger log = LoggerFactory.getLogger(ClientManagerImpl2.class);
	
	private final static String TRACKING_LIST = "/ggps/trackinglist";
	
	private final static String PREDICTIONS_KEY = "/ggps/predictions/";
	//map of position keyed on clientId
	private final ConcurrentMap<String, Position> clients = new ConcurrentHashMap<>();
	//map of map of client tracks, keeyed on groupId
	private ConcurrentMap<String, ConcurrentMap<String, String>> groupTracks = new ConcurrentHashMap<>();
	
	private final ConcurrentMap<String, String> predictions = new ConcurrentHashMap<>();
	
	//TODO:REMOVE
	private OutputWriter outputBuilder = new DefaultOutputWriter();
	
	//TODO:REMOVE
	private MemcacheHelper memcacheHelper;
	
	private ConcurrentMap<String, TrackWriter> groupsToTrack = new ConcurrentHashMap<>();
	
	private PositionPredictor predictor = new TrackBasedPredictor (new StaticTrackPathBuilder().build("KEMPTON_740"));
	
	private Clock clock = Clock.systemUTC();
	
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
	
	private JdbcTemplate jdbcTemplate;

	private int maxPredictionTime = 10000;
	private int numberOfPoints = 5;

	public ClientManagerImpl2(MemcacheHelper memcacheHelper) {
		this.memcacheHelper = memcacheHelper;

		
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
	
	public ClientManagerImpl2(MemcacheHelper memcacheHelper, Clock clock, JdbcTemplate jdbcTempalate) {
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
			
			memcacheHelper.set(groupKeyName, groupKeyName, postionMap, 5);
		}	
		
	}
	
	protected ConcurrentMap<String, List<Position>> getAllGroups() {
		return clients.values()
				.stream()
				.filter(p -> !isOld(p))
				.collect(Collectors.groupingByConcurrent(Position::getGroupId));
	}
	
	protected  ConcurrentMap<String, List<Position>> getGroups(Set<String> groupsToInclude) {
		return clients.values()
				.stream()
				.filter(p -> groupsToInclude.contains(p.getGroupId()))
				.collect(Collectors.groupingByConcurrent(Position::getGroupId));		
	}
	protected  ConcurrentMap<String, List<Position>> getTrackedGroups() {
		 return getGroups(groupsToTrack.keySet());
	}
	


	//TODO:remove
	protected void publishTracks() throws Exception {
		ConcurrentMap<String, List<Position>> groups =  getGroups(groupsToTrack.keySet());
		for(String groupId : groups.keySet()) {
			List<Position> positions = groups.get(groupId);
			Set<String> clientsInGroup =  positions
					.stream()
					.map(p -> p.getClientId())
					.collect(Collectors.toSet());
			//predictPositions(groupId, positions);	
			publishTrack(groupId, clientsInGroup);
			
		}
		if (!groupsToTrack.isEmpty()) {
			memcacheHelper.set(PREDICTIONS_KEY, PREDICTIONS_KEY, predictions);
		}
		
 	}
	
	//TODO:remove
	protected void predictPositions(String groupId, List<Position> positions) {
		for(Position p : positions) {
			int currentLag = (int) p.getCurrentLag();
			if ( p.getCurrentLag() > maxPredictionTime) {
				currentLag = maxPredictionTime;
			} 	
		   predictions.put(p.getClientId(), getPredictor(groupId).predict(p, currentLag, numberOfPoints, null));
		}
		
	}
	
	private PositionPredictor getPredictor(String groupId) {
		return predictor;
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
		clients.compute(position.getClientId(), (k, v) -> (v==null) ? position : position.smoothAltitude(v));
		
		updateTracks(position);
	}
	
	public void removeStaleClients() {
		clients.entrySet().removeIf(p -> isOld(p.getValue()));
		
	}
	
	private boolean isOld(Position p) {
		if(p.getTimeCreated().until(LocalDateTime.now(clock), ChronoUnit.SECONDS) > 5) {
			return true;
		}
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
	
	
	protected ConcurrentMap<String, String> getTrackMap(String groupId) {
		return groupTracks.computeIfAbsent(groupId, map -> new ConcurrentHashMap<>());
	}

	@Override
	public void clearTrack(String groupId) {
		groupTracks.remove(groupId);
	}

	public ConcurrentMap<String, ConcurrentMap<String, String>> getGroupTracks() {
		return groupTracks;
	}
	
	
	
}
