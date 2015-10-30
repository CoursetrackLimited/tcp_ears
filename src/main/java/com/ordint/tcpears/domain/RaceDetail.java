package com.ordint.tcpears.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class RaceDetail {
	public enum RaceStatus {NOT_STARTED, STARTED, FINISHED, REPLAYING}
	
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private String name;
	private LocalDateTime scheduledStartTime;
	private LocalDateTime actualStartTime;
	private LocalDateTime finishTime;
	private Long venueId;
	private String venueName;
	private Long id;
	private Long groupId;
	private Long trackConfigId;
	private RaceStatus status;

	
	public String getStartTime() {
		ZonedDateTime zdt = ZonedDateTime.of(scheduledStartTime, ZoneId.of("GMT"));
		return zdt.withZoneSameInstant(ZoneId.of("Europe/London")).truncatedTo(ChronoUnit.MINUTES).format(TIME_FORMATTER);
	}
}
