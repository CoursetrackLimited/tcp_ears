package com.ordint.tcpears.service.impl;

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
import com.ordint.tcpears.service.ClientDetailsResolver;

@Component
public class HorseDetailsResolver implements ClientDetailsResolver {
	private final static Logger log = LoggerFactory.getLogger(HorseDetailsResolver.class);
	private ConcurrentMap<String, ClientDetails> clientDetailsMap = new ConcurrentHashMap<>();
	private final static String UNKNOWN_GROUP ="nogroup";
	private String defaultGroupId = UNKNOWN_GROUP;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@PostConstruct
	protected void init() {
		List<ClientDetails> details = jdbcTemplate.query("select * from clients where group_id is not null", new RowMapper<ClientDetails>() {
			@Override
			public ClientDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ClientDetails(rs.getString("group_id"), rs.getString("client_ident"));
			}});	
		for(ClientDetails d : details) {clientDetailsMap.put(d.getClientId(), d);}
	}

	@Override
	public ClientDetails resolveClientDetails(String clientId) {	
		return clientDetailsMap.computeIfAbsent(clientId, val -> new ClientDetails(defaultGroupId, clientId));
	}
	@Override
	public void updateClientDetails(ClientDetails clientDetail) {
		log.debug("Updating client details {} ", clientDetail);
		clientDetailsMap.put(clientDetail.getClientId(), clientDetail);		
	}
	
	@Override
	public void setDefaultGroup(String groupId) {
		defaultGroupId = groupId;
	}
}
