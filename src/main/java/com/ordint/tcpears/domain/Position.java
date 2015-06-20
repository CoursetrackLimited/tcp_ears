package com.ordint.tcpears.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Position {

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
	
		
	public String getGroupId() {
		return clientDetails.getGroupId();
	}
	
	public String getClientId() {
		return clientDetails.getClientId();
	}
	

}
