package com.ordint.tcpears.service.race;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ordint.tcpears.domain.TrackConfig;

public class TrackConfigRowMapper implements RowMapper<TrackConfig> {
	@Override
	public TrackConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
		return TrackConfig.builder()
			.finishLine(rs.getString("finish_line"))
			.kml(rs.getString("kml"))
			.name(rs.getString("name"))
			.trackConfigId(rs.getLong("track_config_id"))
			.venueId(rs.getLong("venue_id"))
			.build();
		
	}

}
