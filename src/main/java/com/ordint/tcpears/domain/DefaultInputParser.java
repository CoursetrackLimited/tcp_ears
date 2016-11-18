package com.ordint.tcpears.domain;

import static com.ordint.tcpears.util.ConversionUtil.applyPositionDelta;
import static com.ordint.tcpears.util.ConversionUtil.formatDouble;
import static com.ordint.tcpears.util.ConversionUtil.posToDec;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.util.ConversionUtil;
import com.ordint.tcpears.util.time.Timestamper;

public class DefaultInputParser implements InputParser {
	private final static Logger log = LoggerFactory.getLogger(DefaultInputParser.class);
	private ClientDetailsResolver clientDetailsResolver;	
	private Timestamper timestamper;
	
	public DefaultInputParser(ClientDetailsResolver clientDetailsResolver, Timestamper timestamper) {
		super();
		this.clientDetailsResolver = clientDetailsResolver;
		this.timestamper = timestamper;
	}	
	/**
	 * Parses the following message formats into Position objects
	 * 
	 * <ui>
	 * <li>ident,time(hhmmss.ss),lat(mmss.sssss),long(mmmss.sssss),speed,heading,horizontalAccuracy,verticalAccuracy,altitude,status
	 * <li> or a delta message in the form ident,timeDelta,latDelta,lonDelta,speedDelta,AltDelta
	 * </ul>
	 */

	public Position parse(String message) {
		log.debug(message);
		String parts[] = message.split(",");
		Position.PositionBuilder builder = Position.builder()
				.clientDetails(clientDetailsResolver.resolveClientDetails(parts[0]))
				.timestampFromTime(parts[1])
				.lat(posToDec(parts[2]))
				.lon(posToDec(parts[3]))
				.speed(formatDouble(Double.parseDouble(parts[4])))
				.timeCreated(timestamper.now().truncatedTo(ChronoUnit.MICROS));
				
		if (parts.length == 10) {
			builder = builder.heading(formatDouble(parts[5]))
					.horizontalAccuracy(formatDouble(parts[6]))
					.verticalAccuracy(formatDouble(parts[7]))
					.altitude(parts[8])
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
	@Override
	public Optional<Position> parse(String message, ClientManager clientManager) {
		log.debug(message);
		int count = StringUtils.countMatches(message, ',');
		
		if (count == 9) {
			return parseLongMessage(message);
		} else if (count == 5) {
			return parseShortMessage(message, clientManager);
		}
		return Optional.empty();
	}
	
	private Optional<Position> parseLongMessage(String message) {
		String parts[] = message.split(",");
		try {
			return Optional.of(Position.builder()
					.clientDetails(clientDetailsResolver.resolveClientDetails(parts[0]))
					.timestampFromTime(parts[1])
					.lat(posToDec(parts[2]))
					.lon(posToDec(parts[3]))
					.speed(formatDouble(Double.parseDouble(parts[4])))
					.timeCreated(timestamper.now().truncatedTo(ChronoUnit.MICROS))
					.heading(formatDouble(parts[5]))
					.horizontalAccuracy(formatDouble(parts[6]))
					.verticalAccuracy(formatDouble(parts[7]))
					.altitude(parts[8])
					.status(parts[9])
					.build());
		} catch (Exception e) {
			log.warn("Could not parse message " + message, e);
		}
		return Optional.empty();
	}
	/**
	 * Short message is a delta in the form  ident,timeDelta,latDelta,lonDelta,speedDelta,AltDelta
	 * where all the values are scaled up to ints
	 * 
	 */
	private Optional<Position> parseShortMessage(String message, ClientManager clientManager) {
		String parts[] = message.split(",");
		//get the existing postion
		String clientId = parts[0];
		if (StringUtils.isNotBlank(clientId)) {
			Optional<Position> currentOpt = clientManager.getClientPosition(clientId);
			if (currentOpt.isPresent()) {
				Position current = currentOpt.get();
				try {
					long timeDelta = Integer.parseInt(parts[1]) * 10;
					double latDelta = scaleDown(parts[2], 100 *1000);
					double lonDelta = scaleDown(parts[3], 100 *1000);
					double speedDelta = scaleDown(parts[4], 100);
					double altDelta = scaleDown(parts[5], 100);
					return Optional.of(Position.builder().position(current)
						.timestampFromDateTime(current.getTimestamp().plus(timeDelta, ChronoUnit.MILLIS))
						.lat(applyPositionDelta(current.getLat(), latDelta))
						.lon(applyPositionDelta(current.getLon(), lonDelta))
						.speed(applyDoubleDelta(current.getSpeed(), speedDelta))
						.altitude(applyDoubleDelta(current.getAltitude(), altDelta))
						.build());
				}  catch (Exception e) {
					log.warn("Could not parse message " + message, e);
				}					
			}
		} else {
			log.warn("No Client Id");
		}
		return Optional.empty();
		
	}
	
	private String applyDoubleDelta(String current, double delta) {
		if (!NumberUtils.isParsable(current)) {
			return null;
		}
		return formatDouble(Double.parseDouble(current) + delta);
		
	}
	
	private double scaleDown(String intNumber, int by) {
		return (double)Integer.parseInt(intNumber) / by;
	}
	
	


	

}
