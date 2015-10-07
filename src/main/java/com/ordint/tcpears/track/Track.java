package com.ordint.tcpears.track;

import java.awt.geom.Point2D;

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
	
	public Track(MeasuredShape guideTrack, String finishLatLong) {
		this.guideTrack = guideTrack;
		double[] info = guideTrack.getDistanceAlongTrack(PredictionUtil.toPoint(finishLatLong));
		finishDistance = info[2];
	}
		
	public PositionDistanceInfo calculateDistanceInfo(Position position) {
		Point2D currentPoint = PredictionUtil.toPoint(position);
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
	

}
