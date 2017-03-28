package com.ordint.tcpears.service.position;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.track.StaticPathBuilder;
import com.ordint.tcpears.track.Track;
import com.ordint.tcpears.util.ConversionUtil;

public class PositionExtrapolatorEnhancer implements PositionEnhancer {
	
	private final static Logger log = LoggerFactory.getLogger(PositionExtrapolatorEnhancer.class);
	private Clock clock = Clock.systemUTC();
	private Track track;
	private boolean useRelativeTime = false;
	private long offset = 0;
	public PositionExtrapolatorEnhancer() {
		track =  new Track(StaticPathBuilder.KEMPTON_740, StaticPathBuilder.KEMPTON_FINISH, 1000);
	}
	public PositionExtrapolatorEnhancer(boolean useRelativeTime) {
		track =  new Track(StaticPathBuilder.KEMPTON_740, StaticPathBuilder.KEMPTON_FINISH,1000);
		this.useRelativeTime =useRelativeTime;
	}
	public PositionExtrapolatorEnhancer(Clock clock) {
		track =  new Track(StaticPathBuilder.KEMPTON_740, StaticPathBuilder.KEMPTON_FINISH, 1000);
		this.clock = clock;
	}
	@Override
	public List<Position> decorate(List<Position> positions) {
		List<Position> updatedPositions = new ArrayList<>();
		for(Position p : positions) {
			Position e = extrapolate(p);
			 updatedPositions.add(e);
		}
		
		return updatedPositions;
	}
	
	private long getAge(Position p) {
		return ChronoUnit.MILLIS.between( p.getTimeCreated(), currrentInstant());
	}
	private LocalDateTime currrentInstant() {
		return LocalDateTime.now(clock).minus(offset, ChronoUnit.MILLIS);
	}
	
	private Position extrapolate(Position p) {
		if (useRelativeTime && offset == 0) {
			offset = getAge(p);
			log.info("Offsetting current time by {} milliseconds", offset);
		}
		
		if (StringUtils.isBlank(p.getLastLat()) || StringUtils.isBlank(p.getLastLon())) {
			return p;
		}
		long timeInMilis = getAge(p);

/*		if ("kmp05".equals(p.getClientDetails().getFixedName())) {
			log.info("Time in millis {}", timeInMilis);
		}*/
	
		try {
			return track.predict(p, (int)timeInMilis);
		} catch (IllegalArgumentException e) {
			return p;
		}
		
	}

/*
	private Position calculateLinearPoint(Position p, long timeInMilis) {
		Point2D previous = PositionToPointConverterImpl.toPoint(Double.parseDouble(p.getLastLat()), Double.parseDouble(p.getLastLon()));
		Point2D current = PositionToPointConverterImpl.toPoint(p);
		double distance = current.distance(previous);
		double sin = (current.getY() - previous.getY()) / distance;
		double cos = (current.getX() - previous.getX()) / distance;
		double extra = p.getSpeedValue() * timeInMilis/ 1000; 
		double y = current.getY() + (extra * sin);
		double x =current.getX() + (extra * cos);
		double newLat = PositionToPointConverterImpl.metersToLat(y);
		double newLon = PositionToPointConverterImpl.metersToLon(x);
		LocalDateTime now = currrentInstant();
		return Position.builder().position(p)
				.timeCreated(now)
				.timestampFromDateTime(p.getTimestamp().plus(timeInMilis, ChronoUnit.MILLIS))
				.lat(Double.toString(newLat))
				.lon(Double.toString(newLon)).build();		
		
	}
	
*/
	public Position calculateLinearPoint3(Position p, long timeInMilis) {
		//log.info("Generating linear point for {}", p.getClientDetails().getRunnerIdent());
		LatLng start = ConversionUtil.toLatLng(p.getLastLat(), p.getLastLon());
		LatLng finish = ConversionUtil.toLatLng(p.getLat(), p.getLon());
		double bearing = LatLngTool.initialBearing(start, finish);
		LatLng extra = LatLngTool.travel(finish, bearing, p.getSpeedValue() * (timeInMilis/1000), LengthUnit.METER);
		
		return Position.builder().position(p)
				.lat(Double.toString(extra.getLatitude()))
				.lon(Double.toString(extra.getLongitude())).build();
	}
	@Override
	public Position enhance(Position position) {
		return extrapolate(position);
	}

}
