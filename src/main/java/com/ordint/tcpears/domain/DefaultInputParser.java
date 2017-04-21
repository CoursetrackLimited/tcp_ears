package com.ordint.tcpears.domain;

import static com.ordint.tcpears.util.ConversionUtil.formatDouble;
import static com.ordint.tcpears.util.ConversionUtil.posToDec;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position.PositionBuilder;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.util.ConversionUtil;
import com.ordint.tcpears.util.time.Timestamper;

public class DefaultInputParser implements InputParser {
	private final static Logger intpuLog = LoggerFactory.getLogger(DefaultInputParser.class);
	private final static Logger log = LoggerFactory.getLogger("com.ordint.tcpears.domain.DefaultInputParser2");
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


	@Override
	public Optional<Position> parse(String message, ClientManager clientManager) {
		intpuLog.debug(message);
		int count = StringUtils.countMatches(message, ',');
		switch (count) {
		case 9: return parseLongMessage(message);
		case 5: return parseDeltaMessage(message, clientManager);
		case 4: return parseShortMessage(message);
		default:
		}

		return Optional.empty();
	}
	/*
	 * <li>ident,time(hhmmss.ss),lat(mmss.sssss),long(mmmss.sssss),speed,heading,horizontalAccuracy,verticalAccuracy,altitude,status
	 */
	private Optional<Position> parseLongMessage(String message) {
		String parts[] = message.split(",");
		try {
			return Optional.of(parseCommon(parts)
					.heading(formatDouble(parts[5]))
					.horizontalAccuracy(formatDouble(parts[6]))
					.verticalAccuracy(formatDouble(parts[7]))
					.altitude(parts[8])
					.status(parts[9])
					.build());
		} catch (Exception e) {
			log.warn("Could not parse long message " + message, e);
		}
		return Optional.empty();
	}
	/*
	 * ident,time(hhmmss.ss),lat(mmss.sssss),long(mmmss.sssss),speed
	 */
	private Optional<Position> parseShortMessage(String message) {
		String parts[] = message.split(",");
		try {
			return Optional.of(parseCommon(parts)
					.altitude("-1")
					.heading("-1")
					.horizontalAccuracy("-1")
					.verticalAccuracy("-1")
					.status("-1")
					.build());
		} catch (Exception e) {
			log.warn("Could not parse short message " + message, e);
		}
		return Optional.empty();
	}
	
	/**
	 * Delta message is a delta in the form  ident,timeDelta,latDelta,lonDelta,speedDelta,AltDelta
	 * where all the values are scaled up to ints
	 * 
	 */
	private Optional<Position> parseDeltaMessage(String message, ClientManager clientManager) {
		String parts[] = message.split(",");
		//get the existing postion
		String clientId = parts[0];
		if (StringUtils.isNotBlank(clientId)) {
			Optional<Position> currentOpt = clientManager.getClientPosition(clientId);
			if (currentOpt.isPresent()) {
				Position current = currentOpt.get();
				try {
					long timeDelta = (long) (Double.parseDouble(parts[1]) * 10);
					double latDelta = scaleDown(parts[2], 100 *1000);
					double lonDelta = scaleDown(parts[3], 100 *1000);
					double speedDelta = scaleDown(parts[4], 100);
					double altDelta = scaleDown(parts[5], 100);
					String rawLat = applyPositionDelta(current.getRawLat(), latDelta);
					String rawLon = applyPositionDelta(current.getRawLon(), lonDelta);
					return Optional.of(Position.builder().position(current)
						.timeCreated(timestamper.now().truncatedTo(ChronoUnit.MICROS))
						.timestampFromDateTime(current.getTimestamp().plus(timeDelta, ChronoUnit.MILLIS))
						.rawLat(rawLat)
						.rawLon(rawLon)
						.lat(posToDec(rawLat))
						.lon(posToDec(rawLon))
						.speed(applyDoubleDelta(current.getSpeed(), speedDelta))
						.altitude(applyDoubleDelta(current.getAltitude(), altDelta))
						.build());
				}  catch (Exception e) {
					log.warn("Could not parse delta message " + message, e);
				}					
			}
		} else {
			log.warn("No Client Id");
		}
		return Optional.empty();
		
	}
	private PositionBuilder parseCommon(String[] parts) {
   		return Position.builder().clientDetails(clientDetailsResolver.resolveClientDetails(parts[0]))
			.timestampFromTime(parts[1])
			.rawLat(parts[2])
			.rawLon(parts[3])
			.lat(posToDec(parts[2]))
			.lon(posToDec(parts[3]))
			.speed(formatDouble(Double.parseDouble(parts[4])))
			.timeCreated(timestamper.now().truncatedTo(ChronoUnit.MICROS));

	}
	private String applyDoubleDelta(String current, double delta) {
		if (!NumberUtils.isParsable(current)) {
			return null;
		}
		return formatDouble(Double.parseDouble(current) + delta);
		
	}
	
	private double scaleDown(String number, int by) {
		
		return Double.parseDouble(number) / by;
		
	}
	
    private static String applyPositionDelta(String rawPos, double delta) {
    	double currentPosition = Double.parseDouble(rawPos) + delta;
    	return new BigDecimal(currentPosition).setScale(5, RoundingMode.HALF_DOWN).toString();

    }	


	

}
