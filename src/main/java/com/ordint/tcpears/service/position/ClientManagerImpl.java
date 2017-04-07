package com.ordint.tcpears.service.position;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ordint.tcpears.domain.DefaultSnakeWriter;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.SnakeWriter;
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
	//map of map of client snakes, keyed on groupId
	private ConcurrentMap<String, ConcurrentMap<String, String>> snakes = new ConcurrentHashMap<>();
	private ConcurrentMap<String, SnakeWriter> groupsToTrack = new ConcurrentHashMap<>();	
	
	private boolean useSnakes;
	
	@Autowired
	private PositionEnhancers positionEnhancers = new PositionEnhancers();;
	
	
	public ClientManagerImpl(boolean useSnakes) {
		this.useSnakes = useSnakes;
	}
	
	@Override
	public void trackGroup(String groupId) {
		if (useSnakes) {
			log.debug("Tracking group {}", groupId);
			groupsToTrack.put(groupId, new DefaultSnakeWriter());
		}
	}
	
	@Override
	public void stopTrackingGroup(String groupId) {
		if (useSnakes) {
			log.debug("Stop Tracking group {}", groupId);
			groupsToTrack.remove(groupId);
		}
	}
	
	@Override
	public void updatePostion(Position position1) {
		
		Position lastPosition = clients.get(position1.getClientId());
		
		if (lastPosition != null) {
			position1.setPreviousLatLon(lastPosition);
		}
		
		Position position = positionEnhancers.applyPositionEnhancers(position1);
		
		clients.compute(position.getClientId(), (k, v) -> { if(v==null) {
			return position;
		} else {
			
			return position.smoothAltitude(v);	
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
		if (useSnakes) {
			SnakeWriter snakeWriter = groupsToTrack.get(p.getGroupId());		
			if (snakeWriter != null) {
				ConcurrentMap<String, String> map = getTrackMap(p.getGroupId());
				String snake = map.computeIfPresent(p.getClientId(), (key,value) -> snakeWriter.write(p, value));
				if (snake == null) {
					snake = map.computeIfAbsent(p.getClientId(), value -> snakeWriter.write(p, ""));
				}
		  
			}
		}
	}
	

	
	public ConcurrentMap<String, String> getTrackMap(String groupId) {
		return snakes.computeIfAbsent(groupId, map -> new ConcurrentHashMap<>());
	}

	@Override
	public void clearSnake(String groupId) {
		snakes.remove(groupId);
	}
	@Override
	public void clearAllSnakes() {
		snakes.clear();
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.admin.PositionDataProvider#getGroupTracks()
	 */
	@Override
	public ConcurrentMap<String, ConcurrentMap<String, String>> getSnakes() {
		ConcurrentMap<String, List<Position>> trackedClients =  groupClientsByTrackedGroup();
		for(String groupId : snakes.keySet()) {
			List<Position> clients = trackedClients.get(groupId);
			if (clients == null) {
				snakes.remove(groupId);
			} else {
				Set<String> clientsInGroup =  trackedClients.get(groupId)
						.stream()
						.map(p -> p.getClientId())
						.collect(Collectors.toSet());
				snakes.put(groupId, filterSnakesMap(snakes.get(groupId), clientsInGroup));
				groupsToTrack.get(groupId).calculateSnakeLength(clientsInGroup.size());
			}
		}
		return snakes;
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.admin.PositionDataProvider#groupClientsByGroup()
	 */
	@Override
	public ConcurrentMap<String, List<Position>> groupClientsByGroup() {
		
		ConcurrentMap<String, List<Position>> groups = clients.values()
				.stream()
				.collect(Collectors.groupingByConcurrent(Position::getGroupId));
		
		for(String groupId: groups.keySet()) {
			groups.replace(groupId, positionEnhancers.applyGroupPositionEnhancer(groupId,groups.get(groupId)));
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
	
	private ConcurrentMap<String, String> filterSnakesMap(ConcurrentMap<String, String> snakeMap, Set<String> clientsInGroup) {
		return snakeMap
				.entrySet()
				.stream()
				.filter(p -> clientsInGroup.contains(p.getKey()))
				.collect(Collectors.toConcurrentMap(p -> p.getKey(), p -> p.getValue()));
		
	}

	@Override
	public Optional<Position> getClientPosition(String clientId) {
		return Optional.ofNullable(clients.get(clientId));
	}
	

}
