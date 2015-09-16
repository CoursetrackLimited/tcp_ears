package com.ordint.tcpears.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.RaceService;
import com.ordint.tcpears.service.ReplayService;

@Component
public class DefaultRaceService implements RaceService {
	
	public enum RaceStatus {NOT_STARTED, STARTED, FINISHED}
	
	private static final String CLIENT_DETAILS_FOR_RACE_SQL = 
			"SELECT clients.client_ident as clientId, friendly_name as fixedName, "
			+ "groups.group_id as groupId, groups.name as groupName, runners.name as tempName "
			+ "FROM clients "
			+ "INNER JOIN runners ON clients.client_id = runners.client_id "
			+ "INNER JOIN races ON races.race_id = runners.race_id "
			+ "INNER JOIN groups ON races.group_id = groups.group_id "
			+ "WHERE races.race_id = ?";
	
	private static final String RESET_CLIENT_DETAILS_SQL =
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
	
	@Override
	public void startRace(long raceId) {
		//update race status in db
		checkRaceStatus(raceId, RaceStatus.NOT_STARTED);
		jdbcTemplate.update("update races set status ='STARTED', actualStartTime = NOW() where race_id=?", raceId);
		//publish??
		
		//update the clientDetailsResolver with runner details
		String groupId = updateClientDetails(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		
		clientManager.trackGroup(groupId);
		

	}



	private void checkRaceStatus(long raceId, RaceStatus requirdStatus) {
		RaceStatus status = raceStatus(raceId);
		if (!status.equals(requirdStatus)) {
			throw new IllegalStateException(String.format("Race with race_id %s was in %s state, needed to be %s",
					raceId, status, requirdStatus));
		}
	}
	
	
	
	private String updateClientDetails(String sql, long raceId) {
		 List<ClientDetails> clientDetails =jdbcTemplate.query(sql, new RowMapper<ClientDetails>() {
			@Override
			public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ClientDetails(rs.getString("groupId"), rs.getString("clientId"),
						 rs.getString("fixedName"), rs.getString("tempName"), rs.getString("groupName"));
			}}, raceId);
		 	
		 	clientDetails.forEach(cd -> clientDetailsResolver.updateClientDetails(cd));
		 	return clientDetails.get(0).getGroupId();
		
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
	public void replayRace(long raceId) {
		checkRaceStatus(raceId, RaceStatus.FINISHED);
		//get start and end of race
		
		Map<String, Object> row = jdbcTemplate.queryForMap("select actualStartTime, finishTime from races where race_id =?", raceId);
		
		LocalDateTime start = LocalDateTime.parse(row.get("actualStartTime").toString());
		LocalDateTime finish = LocalDateTime.parse(row.get("finishTime").toString());
		
		int timeInSecs = (int) start.until(finish, ChronoUnit.SECONDS);
		
		updateClientDetails(CLIENT_DETAILS_FOR_RACE_SQL, raceId);
		
		replayService.replayFrom(start, timeInSecs, true);
		
		

	}

}
