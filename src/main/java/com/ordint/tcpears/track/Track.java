package com.ordint.tcpears.track;

import static java.lang.Double.parseDouble;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.track.geom.MeasuredShape;
import com.ordint.tcpears.util.PredictionUtil;

public class Track {
	
	private static final Logger log = LoggerFactory.getLogger(Track.class);

	private double finishDistance;
	private MeasuredShape guideTrack;
	private PredictionUtil coordinateConverter;	
	

	public Track(String kmlPoints, String finishLatLong) {
		String[] trackPoints = kmlPoints.split(" "); 
		coordinateConverter = new PredictionUtil(calculateCenter(trackPoints));
		this.guideTrack = buildShapeFromKml(trackPoints);
		double[] info = guideTrack.getDistanceAlongTrack(coordinateConverter.toPoint(finishLatLong));
		finishDistance = info[2];
		
	}
	private double calculateCenter(String[] trackPoints) {
		//average lat
		double centre =0;
		for(String point:trackPoints) {
			centre = centre + Double.parseDouble(StringUtils.substringBetween(point, ","));
		}
		return centre = centre / trackPoints.length;
	
	}
	protected MeasuredShape buildShapeFromKml(String[] trackPoints ) {
		
		List<Point2D> allpoints = new ArrayList<>();
		for(String trackPosition : trackPoints) {
			allpoints.add(coordinateConverter.toPoint(trackPosition));
		}	
		return new MeasuredShape(allpoints);
		
	}
	
	
	
	public PositionDistanceInfo calculateDistanceInfo(Position position) {
		Point2D currentPoint = coordinateConverter.toPoint(position);
		double[] info = guideTrack.getDistanceAlongTrack(currentPoint);
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
	
	public Position predict(Position p, int timeInMillis) {
		
		//log.info("Generating track point for {}", p.getClientDetails().getRunnerIdent());
		Point2D extra = guideTrack.predict(coordinateConverter.toPoint(p), p.getSpeedValue(), timeInMillis);
		
	    return Position.builder().position(p)
				.timeCreated(LocalDateTime.now())
				.timestampFromDateTime(p.getTimestamp().plus(timeInMillis, ChronoUnit.MILLIS))
				.lat(Double.toString(coordinateConverter.metersToLat(extra.getY())))
				.lon(Double.toString(coordinateConverter.metersToLon(extra.getX()))).build();
	}		
		
	
}
