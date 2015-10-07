package com.ordint.tcpears.service.position;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.ClientDetailsResolver;

@Component
public class HorseDetailsResolver implements ClientDetailsResolver {
	private final static Logger log = LoggerFactory.getLogger(HorseDetailsResolver.class);
	private ConcurrentMap<String, ClientDetails> clientDetailsMap = new ConcurrentHashMap<>();
	private final static String UNKNOWN_GROUP ="nogroup";
	private String defaultGroupId = UNKNOWN_GROUP;
	private final static String CLIENT_DETAILS_MEMCACHE_KEY = "/ggps/ClientDetailsV2";
	
	private final static String CLIENT_DETAILS_SELECT = "SELECT client_ident, friendly_name, g.name AS group_name, c.group_id, NULL AS runner_ident, NULL AS race_id, "
			+ "NULL AS raceDAtaTime, NULL AS runner_name FROM clients c LEFT JOIN groups g ON c.group_id = g.group_id  "
			+ "WHERE c.client_id NOT IN(SELECT client_id 	FROM runners INNER JOIN races ON races.race_id = runners.race_id  	) "
			+ "UNION SELECT client_ident, friendly_name, g.name AS group_name, c.group_id, runners.ident AS runner_ident, races.race_id,  "
			+ "(STR_TO_DATE(scheduledStartTime,'%Y-%m-%d %H:%i:%s')) AS raceDateTime, runners.name AS runner_name "
			+ "FROM clients c LEFT JOIN groups g ON c.group_id = g.group_id LEFT JOIN runners ON c.client_id = runners.client_id "
			+ "LEFT JOIN races ON runners.race_id = races.race_id INNER JOIN (SELECT client_id,  MAX(STR_TO_DATE(scheduledStartTime,'%Y-%m-%d %H:%i:%s')) AS maxDateTime 	"
			+ "FROM runners INNER JOIN races ON races.race_id = runners.race_id  GROUP BY client_id) AS ping ON scheduledStartTime = ping.maxDateTime "
			+ "AND c.client_id = ping.client_id ORDER BY friendly_name";
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private MemcacheHelper memcacheHelper;
	
	@PostConstruct
	@Override
	public void refresh() {
		List<ClientDetails> details = jdbcTemplate.query(CLIENT_DETAILS_SELECT, new RowMapper<ClientDetails>() {
			@Override
			public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ClientDetails(defaultIfBlank(rs.getString("group_id"), defaultGroupId), 
						rs.getString("client_ident"),
						rs.getString("friendly_name"),
						rs.getString("runner_name"),
						defaultIfBlank(rs.getString("group_name"), defaultGroupId),
						rs.getString("runner_ident"));
			}});
		updateClientDetails(details);
		
	}

	@Override
	public ClientDetails resolveClientDetails(String clientId) {
		ClientDetails cd = clientDetailsMap.get(clientId);
		if(cd == null) {
			cd = clientDetailsMap.computeIfAbsent(clientId, val -> addUnrecognisedClient(clientId));
			updateMemcache();
		}
		return cd;
	}
	@Override
	public void updateClientDetails(ClientDetails clientDetail) {
		log.debug("Updating client details {} ", clientDetail);
		clientDetailsMap.put(clientDetail.getClientId(), clientDetail);
		updateMemcache();
	}
	
	@Override
	public void setDefaultGroup(String groupId) {
		defaultGroupId = groupId;
	}
	
	
	private ClientDetails addUnrecognisedClient(String clientId) {
		jdbcTemplate.update("INSERT INTO clients (client_ident) values(?)", clientId);
		return new ClientDetails(defaultGroupId, clientId);
	}
	
	
	
	@Override
	public void updateClientDetails(List<ClientDetails> clientDetails) {
		
		clientDetails.forEach(clientDetail -> clientDetailsMap.put(clientDetail.getClientId(), clientDetail));
		updateMemcache();
		
	}
	
	private void updateMemcache() {		
		try {
			memcacheHelper.set(CLIENT_DETAILS_MEMCACHE_KEY, CLIENT_DETAILS_MEMCACHE_KEY, clientDetailsMap);
		} catch (IOException e) {
			log.error("Unable to update memcache with client details, check that memcache is up!");
		}
	}
	
	
}
