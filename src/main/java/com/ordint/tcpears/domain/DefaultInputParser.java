package com.ordint.tcpears.domain;

import static com.ordint.tcpears.util.ConversionUtil.formatDouble;
import static com.ordint.tcpears.util.ConversionUtil.posToDec;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.server.StringHandler;
import com.ordint.tcpears.service.ClientDetailsResolver;

public class DefaultInputParser implements InputParser {
	private final static Logger log = LoggerFactory.getLogger(DefaultInputParser.class);
	private ClientDetailsResolver clientDetailsResolver;
	//private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hhmmss.SS");
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
					.altitude(parts[8])
					.status(parts[9]);
			log.debug(message);
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
