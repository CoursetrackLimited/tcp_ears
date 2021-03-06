package com.ordint.tcpears.domain;

import java.time.LocalDateTime;

import com.ordint.tcpears.domain.lombok.ClientDetails;
import com.ordint.tcpears.domain.lombok.Position;

public class PositionUtil {
	
	public static Position createPosition(String lat, String lon, String alt, String group, String clientId) {
		 return Position.builder().clientDetails(new ClientDetails( group, clientId))
			.altitude(alt)
			.lat(lat)
			.timestampFromTime("105413.15")
			.speed("10.4")
			.timeCreated(LocalDateTime.now())
			.lon(lon).build(); 
	}
	
	public static Position createPosition(String lat, String lon, String alt) {
  		ClientDetails c = new ClientDetails("groupId", "clientId");
  		return Position.builder()
			.altitude(alt)
			.clientDetails(c)
			.heading("119.43")
			.horizontalAccuracy("0.7")
			.verticalAccuracy("0.6")
			.lat(lat)
			.lon(lon)
			.speed("10.4")
			.status("D")
			.timestampFromTime("105413.15")
			.timeCreated(LocalDateTime.now())
			.build();
	}
}
