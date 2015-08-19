package com.ordint.tcpears.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@Builder
public class Position {
	private static final DateTimeFormatter GPS_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("HHmmss.SS");
	@NonFinal
	private String timestamp;
	private String lat;
	private String lon;
	private String speed;
	@NonFinal
	private String altitude;
	private String heading;
	private String horizontalAccuracy;
	private String verticalAccuracy ;
	private String status;
	private ClientDetails clientDetails; 
	private LocalDateTime timeCreated;
	@NonFinal
	private Long lag;
		
	public String getGroupId() {
		return clientDetails.getGroupId();
	}
	
	public String getClientId() {
		return clientDetails.getClientId();
	}
	
	public long getLag() {
		if (lag == null) {
			lag = getCurrentLag();
		}
		return lag.longValue();
		
	}
	
	public long getCurrentLag() {
		return ChronoUnit.MILLIS.between(LocalTime.parse(timestamp, GPS_TIMESTAMP_FORMAT), timeCreated);
	}
	
	public String getTimestamp() {
		LocalDateTime t = LocalDateTime.of(LocalDate.now(), LocalTime.parse(timestamp, GPS_TIMESTAMP_FORMAT));
		return t.toString();
	}
	
	public Position smoothAltitude(Position previous) {
		if (StringUtils.equals(altitude, "-1")) {
			altitude = previous.getAltitude();
		}
		return this;
		
	}
	
	public double getSpeedValue() {
		return Double.parseDouble(speed);
	}
	

}
