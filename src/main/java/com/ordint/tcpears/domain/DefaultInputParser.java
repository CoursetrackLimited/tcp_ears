package com.ordint.tcpears.domain;

import static com.ordint.tcpears.util.ConversionUtil.feetToMeters;
import static com.ordint.tcpears.util.ConversionUtil.formatDouble;
import static com.ordint.tcpears.util.ConversionUtil.posToDec;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ordint.tcpears.service.ClientDetailsResolver;

public class DefaultInputParser implements InputParser {
	
	private ClientDetailsResolver clientDetailsResolver;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hhmmss.SS");
	private Clock clock = Clock.systemUTC();
	
	public DefaultInputParser(ClientDetailsResolver clientDetailsResolver, Clock clock) {
		super();
		this.clientDetailsResolver = clientDetailsResolver;
		this.clock = clock;
	}	
	/**
	 * Parses the following message formats into Position objects
	 * 
	 * <ui>
	 * <li>ident,time(hhmmss.ss),lat(mmss.sssss),long(mmmss.sssss),speed
	 * <li>ident,time(hhmmss.ss),lat(mmss.sssss),long(mmmss.sssss),speed,heading,horizontalAccuracy,verticalAccuracy,altitude,status
	 * </ul>
	 */
	@Override
	public Position parse(String message) {
		String parts[] = message.split(",");
		Position.PositionBuilder builder = Position.builder()
				.clientDetails(clientDetailsResolver.resolveClientDetails(parts[0]))
				.timestamp(parts[1])
				.lat(posToDec(parts[2]))
				.lon(posToDec(parts[3]))
				.speed(formatDouble(Double.parseDouble(parts[4])*0.5144444444))
				.timeCreated(LocalDateTime.now(clock));
		
		if (parts.length == 10) {
			builder = builder.heading(formatDouble(parts[5]))
					.horizontalAccuracy(formatDouble(parts[6]))
					.verticalAccuracy(formatDouble(parts[7]))
					.altitude(feetToMeters(parts[8]))
					.status(parts[9]);
		} else {
			builder = builder.altitude("-1")
					.heading("-1")
					.horizontalAccuracy("-1")
					.verticalAccuracy("-1")
					.status("-1");
		}
		
		return builder.build();
	}


	

}
