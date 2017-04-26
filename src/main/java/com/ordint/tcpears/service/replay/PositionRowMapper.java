package com.ordint.tcpears.service.replay;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.ordint.tcpears.domain.lombok.Position;
import com.ordint.tcpears.service.ClientDetailsResolver;

public class PositionRowMapper implements RowMapper<Position>{
	
	private ClientDetailsResolver clientDetailsResolver;
	
	public PositionRowMapper(ClientDetailsResolver clientDetailsResolver) {
		this.clientDetailsResolver = clientDetailsResolver;
	}
	@Override
	public Position mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		LocalDateTime gpsTime = LocalDateTime.parse(rs.getString("gpsTimestamp"));
		return Position.builder()
			.clientDetails(clientDetailsResolver.resolveClientDetails(rs.getString("clientId")))
			.heading(rs.getString("heading"))
			.horizontalAccuracy(rs.getString("horizontalAccuracy"))
			.lat(rs.getString("lat"))
			.lon(rs.getString("lon"))
			.speed(rs.getString("speed"))
			.status(rs.getString("status"))
			.lag(0)
			.timestampFromDateTime(gpsTime)
			.timeCreated(gpsTime)
			.verticalAccuracy(rs.getString("verticalAccuracy"))
			.altitude(rs.getString("altitude"))
			.build();
		
	}
}

