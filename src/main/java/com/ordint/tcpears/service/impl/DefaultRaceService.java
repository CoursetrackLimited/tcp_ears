package com.ordint.tcpears.service.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionPublisher;
import com.ordint.tcpears.service.RaceService;
import com.ordint.tcpears.service.ReplayService;

@Component
public class DefaultRaceService implements RaceService {
	
	private static final String RACE_DETAILS_MEMCACHE_KEY = "/ggps/RaceDetails";
	private static final Logger log = LoggerFactory.getLogger(DefaultRaceService.class);


	public enum RaceStatus {NOT_STARTED, STARTED, FINISHED}
	
	static final String CLIENT_DETAILS_FOR_RACE_SQL = 
			"SELECT clients.client_ident as clientId, friendly_name as fixedName, "
			+ "groups.group_id as groupId, groups.name as groupName, runners.name as tempName "
			+ "FROM clients "
			+ "INNER JOIN runners ON clients.client_id = runners.client_id "
			+ "INNER JOIN races ON races.race_id = runners.race_id "
			+ "INNER JOIN groups ON races.group_id = groups.group_id "
			+ "WHERE races.race_id = ?";
	
	static final String RESET_CLIENT_DETAILS_SQL =
			"SELECT clients.client_ident AS clientId, clients.friendly_name AS fixedName, "
			+ "groups.group_id AS groupId, groups.name AS groupName, NULL AS tempName "
			+ "FROM clients "
			+ "INNER JOIN groups ON groups.group_id = clients.group_id "
			+ "INNER JOIN runners ON clients.client_id = runners.client_id "
			+ "WHERE runners.race_id =?";
	
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
	
	private static final DateTimeFormatter MYSQL_DATETIME_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	@Override
	public void startRace(long raceId) throws RaceServiceException {
		//update race status in db
		checkRaceStatus(raceId, RaceStatus.NOT_STARTED);
		jdbcTemplate.update("update races set status ='STARTED', actualStartTime = NOW() where race_id=?", raceId);
		//publish??
		
		//update the clientDetailsResolver with runner details
		List<ClientDetails> clientDetails = updateClientDetails(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		String groupId = clientDetails.get(0).getGroupId();
		
		clientManager.trackGroup(groupId);
		
		
		publishRaceDetails(raceId, groupId, clientDetails);
		
		
	}
	
	
	private void publishRaceDetails(long raceId , String groupId, List<ClientDetails> clientDetails) throws RaceServiceException {
		//clients.client_ident, runners.name AS friendly_name, groups.group_id , groups.name AS group_name 
		HashMap<String, Object> raceDetails = new HashMap<>();
		raceDetails.put("race_group_id", groupId);
		raceDetails.put("race_name", clientDetails.get(0).getCurrentGroupName());
		for(ClientDetails cd : clientDetails) {
			HashMap<String, String> runner = new HashMap<>();
			runner.put("client_ident", cd.getClientId());
			runner.put("friendly_name", cd.getCurrentName());
			raceDetails.put(cd.getClientId(), runner);
		}
		try {
			memcacheHelper.set(RACE_DETAILS_MEMCACHE_KEY, RACE_DETAILS_MEMCACHE_KEY, raceDetails);
		} catch (IOException e) {
			log.error("Error publishing race details for race with id {}", raceId, e);
			throw new RaceServiceException(e);
		}
	}


	private void checkRaceStatus(long raceId, RaceStatus requirdStatus) {
		RaceStatus status = raceStatus(raceId);
		if (!status.equals(requirdStatus)) {
			throw new IllegalStateException(String.format("Race with race_id %s was in %s state, needed to be %s",
					raceId, status, requirdStatus));
		}
	}
	
	
	
	private List<ClientDetails> updateClientDetails(String sql, long raceId) {
		 List<ClientDetails> clientDetails =jdbcTemplate.query(sql, new RowMapper<ClientDetails>() {
			@Override
			public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ClientDetails(rs.getString("groupId"), rs.getString("clientId"),
						 rs.getString("fixedName"), rs.getString("tempName"), rs.getString("groupName"));
			}}, raceId);
		 	
		 	clientDetails.forEach(cd -> clientDetailsResolver.updateClientDetails(cd));
		 	return clientDetails;
		
	}
	
	private RaceStatus raceStatus(long raceId) {
		String status = jdbcTemplate.queryForObject("select status from races where race_id=" + raceId ,String.class);
		Objects.requireNonNull(status, "no race with race_id " + raceId);
		
		return RaceStatus.valueOf(status);
	}
	@Override
	public void finishRace(long raceId) {
		checkRaceStatus(raceId, RaceStatus.STARTED);
		jdbcTemplate.update("update races set status ='FINISHED', finishTime = NOW() where race_id=?", raceId);
		//update the clientDetailsResolver with default details
		updateClientDetails(RESET_CLIENT_DETAILS_SQL, raceId);
		//remove rece details to memcache

	}
	
	
	
	@Override
	public String replayRace(long raceId) throws RaceServiceException {
		checkRaceStatus(raceId, RaceStatus.FINISHED);
		//get start and end of race
		
		Map<String, Object> row = jdbcTemplate.queryForMap("select group_id, actualStartTime, finishTime from races where race_id =?", raceId);
		
		LocalDateTime start = LocalDateTime.parse(row.get("actualStartTime").toString(), MYSQL_DATETIME_FORMATTER);
		LocalDateTime finish = LocalDateTime.parse(row.get("finishTime").toString(), MYSQL_DATETIME_FORMATTER);
		
		int timeInSecs = (int) start.until(finish, ChronoUnit.SECONDS);
		String groupId = row.get("group_id").toString();
		List<ClientDetails> clientDetails = updateClientDetails(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		positionPublisher.clearTrack(groupId);
		clientManager.clearTrack(groupId);
		clientManager.trackGroup(groupId);
		publishRaceDetails(raceId, groupId, clientDetails);
		
		return replayService.replayFrom(start, timeInSecs, true);
		
	}

}
