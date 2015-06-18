package com.ordint.tcpears.server;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class Position {
	
	private String lat;
	private String lon;
	private String speed;
	private String altitude;
	private String groupId;
	private String clientId;
	private String rawMessage;
	
	private final static String KEYS[] = ("groupId,clientId,type,PUBXType,UTC,impLat,Northing Indicator,impLon,Easting Indicator,Alt,Status, " +
            "HorizontalAccuracy,VerticalAccuracy,SOG,COG,VD,GPSAge," +
            "HDOP,VDOP,SVsUsed,DR Status").split(",");


	public Position(String message) {
		String[] vals = message.split(",");
		HashMap<String,String> params = new HashMap<>();
		int i =0;
		for(String key :KEYS) {
			params.put(key, vals[i++]);
		}
		this.lat = params.get("impLat");
		this.lon = params.get("impLon");
		this.speed = params.get("SOG");
		this.groupId = params.get("groupId");
		this.clientId = params.get("clientId");
		this.altitude = params.get("Alt");
		this.rawMessage = message;
	}
	

	
	public String getLat() {
		return lat;
	}

	public String getLon() {
		return lon;
	}

	public String getSpeed() {
		return speed;
	}
	
	
	public String getAltitude() {
		return altitude;
	}



	public String asMessage() {
		StringBuilder out = new StringBuilder();
		out.append(rawMessage).append(",").append(lat).append(",").append(lon);
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
	
	public String getGroupId() {
		return groupId;
	}

	public String getClientId() {
		return clientId;
	}

}
 