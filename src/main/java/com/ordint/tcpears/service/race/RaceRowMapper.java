package com.ordint.tcpears.service.race;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.ordint.tcpears.domain.RaceDetail;

public class RaceRowMapper implements RowMapper<RaceDetail> {
	private static final DateTimeFormatter MYSQL_DATETIME_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public RaceDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return RaceDetail.builder()
				.actualStartTime(parseDateTime(rs,"actualStartTime"))
				.finishTime(parseDateTime(rs, "finishTime"))
				.groupId(rs.getLong("group_id"))
				.id(rs.getLong("race_id"))
				.name(rs.getString("race_name"))
				.scheduledStartTime(parseDateTime(rs, "scheduledStartTime"))
				.venueId(rs.getLong("venue_id"))
				.venueName(rs.getString("venue_name"))
				.build();
	}
	
	private LocalDateTime parseDateTime(ResultSet rs, String columnName) throws SQLException{
		String dateTime = rs.getString(columnName);
		if(StringUtils.isBlank(dateTime)) {
			return null;
		} else {
			return LocalDateTime.parse(dateTime, MYSQL_DATETIME_FORMATTER);
		}
	}
	
}
