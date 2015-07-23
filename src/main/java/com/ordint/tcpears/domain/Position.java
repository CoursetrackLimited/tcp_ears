package com.ordint.tcpears.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@Builder
public class Position {
	private static final DateTimeFormatter GPS_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("HHmmss.SS");
	private String timestamp;
	private String lat;
	private String lon;
	private String speed;
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
			lag = ChronoUnit.MILLIS.between(LocalTime.parse(timestamp, GPS_TIMESTAMP_FORMAT), timeCreated);
		}
		return lag.longValue();
		
	}
	
	public void predict(Position previousPosition) {
		
	}

}
