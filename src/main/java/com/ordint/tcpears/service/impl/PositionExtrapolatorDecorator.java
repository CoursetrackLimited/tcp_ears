package com.ordint.tcpears.service.impl;

import java.awt.geom.Point2D;
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
import com.ordint.tcpears.service.PositionDecorator;
import com.ordint.tcpears.util.ConversionUtil;
import com.ordint.tcpears.util.MeasuredShape;
import com.ordint.tcpears.util.PredictionUtil;
import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;

public class PositionExtrapolatorDecorator implements PositionDecorator {
	
	private final static Logger log = LoggerFactory.getLogger(PositionExtrapolatorDecorator.class);
	private Clock clock = Clock.systemUTC();
	private MeasuredShape track;
	private StaticTrackPathBuilder pathBuilder = new StaticTrackPathBuilder();
	private boolean useRelativeTime = false;
	private long offset = 0;
	public PositionExtrapolatorDecorator() {
		track = new MeasuredShape(pathBuilder.build(StaticTrackPathBuilder.KEMPTON_740_TRACK));
	}
	public PositionExtrapolatorDecorator(boolean useRelativeTime) {
		track = new MeasuredShape(pathBuilder.build(StaticTrackPathBuilder.KEMPTON_740_TRACK));
		this.useRelativeTime =useRelativeTime;
	}
	public PositionExtrapolatorDecorator(Clock clock) {
		track = new MeasuredShape(pathBuilder.build(StaticTrackPathBuilder.KEMPTON_740_TRACK));
		this.clock = clock;
	}
	@Override
	public List<Position> decorate(List<Position> positions) {
		List<Position> updatedPositions = new ArrayList<>();
		for(Position p : positions) {
			Position e = extrapolate(p);
			 updatedPositions.add(e);
/*			if ("kmp05".equals(p.getClientDetails().getFixedName())) {
				log.info("Before {},{},{},{},{}",p.getLat(),p.getLon(), p.getTimestamp(), p.getLag(), p.getLastLat());
				log.info("After  {},{},{},{}",e.getLat(),e.getLon(), e.getTimestamp(), e.getLag()); 
			}*/
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
		if (timeInMilis > 100 && timeInMilis < 400) {
			
			return calculateLinearPoint(p, timeInMilis);
		}
/*		if ("kmp05".equals(p.getClientDetails().getFixedName())) {
			log.info("Time in millis {}", timeInMilis);
		}*/
	
		try {
			return calculateTrackBasedPoint(p, timeInMilis);
		} catch (IllegalArgumentException e) {
			return calculateLinearPoint(p, timeInMilis);
		}
		
	}

	private Position calculateTrackBasedPoint(Position p, long timeInMilis) {
		
		//log.info("Generating track point for {}", p.getClientDetails().getRunnerIdent());
		Point2D extra = track.predict(PredictionUtil.toPoint(p), p.getSpeedValue(), timeInMilis);
		
	    return Position.builder().position(p)
				.timeCreated(currrentInstant())
				.timestampFromDateTime(p.getTimestamp().plus(timeInMilis, ChronoUnit.MILLIS))
				.lat(Double.toString(PredictionUtil.metersToLat(extra.getY())))
				.lon(Double.toString(PredictionUtil.metersToLon(extra.getX()))).build();
	}
	private Position calculateLinearPoint(Position p, long timeInMilis) {
		Point2D previous = PredictionUtil.toPoint(Double.parseDouble(p.getLastLat()), Double.parseDouble(p.getLastLon()));
		Point2D current = PredictionUtil.toPoint(p);
		double distance = current.distance(previous);
		double sin = (current.getY() - previous.getY()) / distance;
		double cos = (current.getX() - previous.getX()) / distance;
		double extra = p.getSpeedValue() * timeInMilis/ 1000; 
		double y = current.getY() + (extra * sin);
		double x =current.getX() + (extra * cos);
		double newLat = PredictionUtil.metersToLat(y);
		double newLon = PredictionUtil.metersToLon(x);
		LocalDateTime now = currrentInstant();
		return Position.builder().position(p)
				.timeCreated(now)
				.timestampFromDateTime(p.getTimestamp().plus(timeInMilis, ChronoUnit.MILLIS))
				.lat(Double.toString(newLat))
				.lon(Double.toString(newLon)).build();		
		
	}
	
	private Position calculateLinearPoint2(Position p, long timeInMilis) {
		Point2D previous = PredictionUtil.toPoint(Double.parseDouble(p.getLastLat()), Double.parseDouble(p.getLastLon()));
		Point2D current = PredictionUtil.toPoint(p);
		double distance = current.distance(previous);
		double sin = (current.getY() - previous.getY()) / distance;
		double cos = (current.getX() - previous.getX()) / distance;
		double extra = p.getSpeedValue() * (timeInMilis/1000) * PredictionUtil.ONE_METER_FACTOR;
		return Position.builder().position(p)
				.lat(Double.toString(current.getY() + (extra * sin)))
				.lon(Double.toString(current.getX() + (extra * cos))).build();		
		
	}
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
	public Position decorate(Position position) {
		return extrapolate(position);
	}

}
