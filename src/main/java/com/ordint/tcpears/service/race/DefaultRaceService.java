package com.ordint.tcpears.service.race;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.domain.RaceDetail;
import com.ordint.tcpears.domain.RaceDetail.RaceStatus;
import com.ordint.tcpears.domain.TrackConfig;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionPublisher;
import com.ordint.tcpears.service.RaceService;
import com.ordint.tcpears.service.ReplayService;
import com.ordint.tcpears.service.position.PositionEnhancers;

@Component
public class DefaultRaceService implements RaceService {
	
	private static final String CURRENT_RACE_DETAILS_MEMCACHE_KEY = "/ggps/CurrentRaceDetails";
	private static final String RACE_DETAILS_MEMCACHE_KEY = "/ggps/RaceDetails/%s";
	private static final Logger log = LoggerFactory.getLogger(DefaultRaceService.class);


	
	static final String RACE_DETAIL_SQL = "SELECT races.race_id, group_id, races.name as race_name, scheduledStartTime, actualStartTime, "
			+ "finishTime, races.venue_id, venues.name AS venue_name, status, track_config_id FROM races INNER JOIN venues "
			+ "ON races.venue_id = venues.venue_id WHERE races.race_id=?";
	
	static final String CLIENT_DETAILS_FOR_RACE_SQL = 
			"SELECT clients.client_ident as clientId, friendly_name as fixedName, "
			+ "groups.group_id as groupId, groups.name as groupName, runners.name as tempName, runners.ident as runner_ident "
			+ "FROM clients "
			+ "INNER JOIN runners ON clients.client_id = runners.client_id "
			+ "INNER JOIN races ON races.race_id = runners.race_id "
			+ "INNER JOIN groups ON races.group_id = groups.group_id "
			+ "WHERE races.race_id = ? and runners.non_runner=0";
	
	static final String RESET_CLIENT_DETAILS_SQL =
			"SELECT clients.client_ident AS clientId, clients.friendly_name AS fixedName, "
			+ "groups.group_id AS groupId, groups.name AS groupName, NULL AS tempName, NULL as runner_ident "
			+ "FROM clients "
			+ "INNER JOIN groups ON groups.group_id = clients.group_id "
			+ "INNER JOIN runners ON clients.client_id = runners.client_id "
			+ "WHERE runners.race_id =? and runners.non_runner=0";
	
	static final String TRACK_CONFIG_SQL = "select * from trackconfig where track_config_id = ?";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ClientDetailsResolver clientDetailsResolver;
	@Autowired
	private ReplayService replayService;
	@Autowired
	private ClientManager clientManager;
	@Autowired
	private MemcacheHelper memcacheHelper;
	@Autowired
	private PositionPublisher positionPublisher;
	@Autowired
	private PositionEnhancers positionEnhancers;
	
	private static final RaceRowMapper RACE_ROW_MAPPER = new RaceRowMapper();
	private static final TrackConfigRowMapper TRACK_CONFIG_ROW_MAPPER = new TrackConfigRowMapper();
	
	private Map<Long, Long> currentReplayRaces = new HashMap<Long, Long>();
	
	
	
	public static final DateTimeFormatter MYSQL_DATETIME_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	@Override
	public void startRace(long raceId) throws RaceServiceException {
		//update race status in db
		RaceDetail race = findRace(raceId, RaceStatus.NOT_STARTED);
		jdbcTemplate.update("update races set status ='STARTED', actualStartTime = NOW() where race_id=?", raceId);
		
		//update the clientDetailsResolver with runner details
		List<ClientDetails> clientDetails = updateClientDetails(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		
		configureRaceObserver(race, clientDetails.size());
	
		
		publishRaceDetails(race, clientDetails);
		
		
	}
	
	
	private void publishRaceDetails(RaceDetail race, List<ClientDetails> clientDetails) throws RaceServiceException {
	
		HashMap<String, Object> raceDetailsMap = buildRaceDetailsMap(race, clientDetails);
		
		String raceMemcaheKey = String.format(RACE_DETAILS_MEMCACHE_KEY, race.getGroupId().toString());
		try {
			memcacheHelper.set(CURRENT_RACE_DETAILS_MEMCACHE_KEY, CURRENT_RACE_DETAILS_MEMCACHE_KEY, raceDetailsMap);
			memcacheHelper.set(raceMemcaheKey, raceMemcaheKey, raceDetailsMap);
		} catch (IOException e) {
			log.error("Error publishing race details for race with id {}", race.getId(), e);
			throw new RaceServiceException(e);
		}
	}


	private HashMap<String, Object> buildRaceDetailsMap(RaceDetail race, List<ClientDetails> clientDetails) {
		HashMap<String, Object> raceDetailsMap = new HashMap<>();
		raceDetailsMap.put("race_group_id", race.getGroupId().toString());
		raceDetailsMap.put("race_name", clientDetails.get(0).getCurrentGroupName());
		raceDetailsMap.put("client_details", clientDetails);
		raceDetailsMap.put("venueName", race.getVenueName());
		raceDetailsMap.put("raceStartTime", race.getStartTime());
		return raceDetailsMap;
	}


	private RaceDetail findRace(long raceId, RaceStatus requirdStatus) {
		RaceDetail race = jdbcTemplate.queryForObject(RACE_DETAIL_SQL,RACE_ROW_MAPPER, raceId);
		Objects.requireNonNull(race, "no race with race_id " + raceId);
		if (race.getStatus() != requirdStatus) {
			throw new IllegalStateException(String.format("Race with race_id %s was in %s state, needed to be %s",
					raceId, race.getStatus(), requirdStatus));
		}
		return race;
	}
	
	
	
	private List<ClientDetails> updateClientDetails(String sql, long raceId) {
		 List<ClientDetails> clientDetails =jdbcTemplate.query(sql, new RowMapper<ClientDetails>() {
			@Override
			public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ClientDetails(rs.getString("groupId"), rs.getString("clientId"),
						 rs.getString("fixedName"), rs.getString("tempName"), rs.getString("groupName"),
						 rs.getString("runner_ident"));
			}}, raceId);
		 	
		 	clientDetailsResolver.updateClientDetails(clientDetails);
		 	return clientDetails;
		
	}

	@Override
	public void finishRace(long raceId) {
		findRace(raceId, RaceStatus.STARTED);
		jdbcTemplate.update("update races set status ='FINISHED', finishTime = NOW() where race_id=?", raceId);
		//update the clientDetailsResolver with default details
		updateClientDetails(RESET_CLIENT_DETAILS_SQL, raceId);
		positionEnhancers.clearEnhancers("");
	}
	
	
	
	@Override
	public String replayRace(long raceId) throws RaceServiceException {
		RaceDetail race = findRace(raceId, RaceStatus.FINISHED);

		Long currentRaceReplay = currentReplayRaces.get(race.getVenueId());
		if (currentRaceReplay != null) {
			throw new RaceServiceException(String.format("Race with id %s is already being replayed at venue with id %s", currentRaceReplay, race.getVenueId()));
		}
		
		int timeInSecs = (int) race.getActualStartTime().until(race.getFinishTime(), ChronoUnit.SECONDS);
		//String groupId = row.get("group_id").toString();
		
		jdbcTemplate.update("update races set status ='REPLAYING' where race_id=?", raceId);
		
		List<ClientDetails> clientDetails = updateClientDetails(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		
		clearSnake(race);
		
		configureRaceObserver(race, clientDetails.size());
		
		publishRaceDetails(race, clientDetails);
		String replayId = race.getVenueId() + "-" + race.getName() + "-" + race.getActualStartTime() + "-" + timeInSecs;
		replayService.replayFrom(race.getActualStartTime(), timeInSecs, true, replayId);
		
		currentReplayRaces.put(race.getVenueId(), raceId);
		
		return race.getName();
	}


	private void clearSnake(RaceDetail race) {
		positionPublisher.clearSnake(race.getGroupId().toString());
		clientManager.clearSnake(race.getGroupId().toString());
	}
	
	private void configureRaceObserver(RaceDetail race, int numberOfRunners) {
		clientManager.trackGroup(race.getGroupId().toString());
		TrackConfig trackConfig = jdbcTemplate.queryForObject(TRACK_CONFIG_SQL, TRACK_CONFIG_ROW_MAPPER, race.getTrackConfigId());
		
		positionEnhancers.addRacePositionEnhancers(race, trackConfig, numberOfRunners);	
	}
	
	public void replayEnded(String replayId) {
		positionEnhancers.clearEnhancers("");
		Long raceId = currentReplayRaces.remove(Long.parseLong(StringUtils.substringBefore(replayId, "-")));
		if (raceId != null) {
			jdbcTemplate.update("update races set status ='FINISHED' where race_id=?", raceId);	
		}
	}
	
	
	public Long getCurrentReplayRaceId(long venueId) {
		return currentReplayRaces.get(venueId);
	}


}
