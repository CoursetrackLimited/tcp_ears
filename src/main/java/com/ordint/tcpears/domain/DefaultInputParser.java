package com.ordint.tcpears.domain;

import static com.ordint.tcpears.domain.ConversionUtil.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ordint.tcpears.server.Position;
import com.ordint.tcpears.service.ClientDetailsResolver;

public class DefaultInputParser implements InputParser {
	
	private ClientDetailsResolver clientDetailsResolver;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hhmmss.SS");
	private Clock clock = Clock.systemUTC();
	
	/**
	 * Parses the following message formats into Position objects
	 * 
	 * <ui>
	 * <li>ident,time(hhmmss.ss),lat(mmss.sssss),long(mmmss.sssss),speed
	 * <li>ident,time(hhmmss.ss),lat(mmss.sssss),long(mmmss.sssss),speed,heading,horizontalAccuracy,verticalAccuracy,altitude,status
	 * </ul>
	 */
	@Override
	public Position2 parse(String message) {
		String parts[] = message.split(",");
		Position2.Position2Builder builder = Position2.builder()
				.clientDetails(clientDetailsResolver.resolveClientDetails(parts[0]))
				.timestamp(parts[1])
				.lat(posToDec(parts[2]))
				.lon(posToDec(parts[3]))
				.speed(formatDouble(Double.parseDouble(parts[4])*0.5144444444))
				.timeCreated(LocalDateTime.now(clock));
		
		if (parts.length == 10) {
			builder = builder.heading(parts[5])
					.horizontalAccuracy(parts[6])
					.verticalAccuracy(parts[7])
					.altitude(feetToMeters(parts[8]))
					.status(parts[9]);
		}
		
		return builder.build();
	}
	

}
