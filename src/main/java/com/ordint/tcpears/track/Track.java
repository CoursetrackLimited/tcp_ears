package com.ordint.tcpears.track;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.domain.TrackConfig;
import com.ordint.tcpears.track.geom.MeasuredShape;
import com.ordint.tcpears.track.geom.PositionToPointConverter;
import com.ordint.tcpears.track.geom.TrackGeomFactory;

public class Track {
	
	private static final Logger log = LoggerFactory.getLogger(Track.class);

	private double finishDistance;
	private MeasuredShape trackShape;
	private PositionToPointConverter positionToPointConverter;	
	

	public Track(String kmlPoints, String finishLatLong) {
		this(kmlPoints, finishLatLong, new TrackGeomFactory());
		
	}
	public Track(String kmlPoints, String finishLatLong, TrackGeomFactory geomFactory) {
		positionToPointConverter = geomFactory.createPositionToPointConverter(kmlPoints);
		this.trackShape = geomFactory.createTrackShape(kmlPoints, positionToPointConverter);
		double[] info = trackShape.getDistanceAlongTrack(positionToPointConverter.toPoint(finishLatLong));
		finishDistance = info[2];
		
	}
	
	public Track(TrackConfig trackConfig, TrackGeomFactory geomFactory) {
		this(trackConfig.getKml(), trackConfig.getFinishLine(), geomFactory);
	}
	
	
	public PositionDistanceInfo calculateDistanceInfo(Position position) {
		Point2D currentPoint = positionToPointConverter.toPoint(position);
		double[] info = trackShape.getDistanceAlongTrack(currentPoint);
		if (info != null) {
			return new PositionDistanceInfo(position.getClientId(), info[2], finishDistance - info[2], 0);
		} else {
			log.warn("Could not cacluclate distance info for {}", position);
			return  new PositionDistanceInfo(position.getClientId(), -1, finishDistance, 0);
		}		
	}

	public double getFinishDistance() {
		return finishDistance;
	}
	
	public Position predict(Position p, double timeInMillis) {
		
		//log.info("Generating track point for {}", p.getClientDetails().getRunnerIdent());
		double distance = p.getSpeedValue() * (timeInMillis / 1000);
		Point2D extra = trackShape.getOffTrackPoint(positionToPointConverter.toPoint(p), distance);
		
	    return Position.builder().position(p)
				.timeCreated(LocalDateTime.now())
				.timestampFromDateTime(p.getTimestamp().plus((long) timeInMillis, ChronoUnit.MILLIS))
				.lat(Double.toString(positionToPointConverter.metersToLat(extra.getY())))
				.lon(Double.toString(positionToPointConverter.metersToLon(extra.getX()))).build();
	}		
		
	
}
