package com.ordint.tcpears.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.experimental.NonFinal;

import com.ordint.tcpears.service.ClientDetails;

@Value
@Builder
public class Position2 {

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
	
	public String asMessage() {
		StringBuilder out = new StringBuilder();
		out.append("").append(",").append(lat).append(",").append(lon);
		return out.toString();
	}
	public String concatTrack(String existingTrack) {
		return concatTrack(existingTrack, false);
	}
	public String concatTrack(String existingTrack, boolean alwaysConcat) {
		StringBuilder newPosition = new StringBuilder();
		newPosition.append(lon).append(",").append(lat).append(",").append(adjustAltitiude(existingTrack)).append(" ");
		//only append if its different from the last one
		if(!alwaysConcat) {
			String lastPosition = existingTrack.substring(0, existingTrack.indexOf(" ") + 1);
			if (newPosition.toString().equals(lastPosition)) {
				return existingTrack;
			}
		}
		return newPosition.append(existingTrack).toString();		
	}
	
	private String adjustAltitiude(String existingTrack) {
		if (altitude.equals("-1")) {
            int firstTripleIndex=existingTrack.indexOf(" ");
            if (firstTripleIndex > 0) {
            	String firstTriple=existingTrack.substring(0, firstTripleIndex);
            	return firstTriple.substring(firstTriple.lastIndexOf(",") + 1, firstTriple.length());			
            }
		}
		return altitude;
	}
}
