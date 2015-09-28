package com.ordint.tcpears.util.prediction;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.util.MeasuredShape;
import com.ordint.tcpears.util.PredictionUtil;


public class TrackBasedPredictor implements PositionPredictor<Object> {

	private final static Logger log = LoggerFactory.getLogger(TrackBasedPredictor.class);
	private Path2D path;
	private MeasuredShape track;

	public TrackBasedPredictor(Path2D path) {
		this.path = path;
		track = new MeasuredShape(path);
	}

	
	@Override
	public String predict(Position currentPosition, int predictionTime, int numberOfPoints, Object data) {
		
		Point2D currentPoint = PredictionUtil.toPoint(currentPosition);
		Point2D startGuidePoint = track.getClosestPoint(currentPoint);
		double offset = currentPoint.distance(startGuidePoint);
		double xslope = (currentPoint.getX() - startGuidePoint.getX()) / offset;
		double yslope = (currentPoint.getY() - startGuidePoint.getY()) / offset;
		
		// get final prediction points based on distance
		double totalDistance = currentPosition.getSpeedValue() * (predictionTime / 1000)
				* PredictionUtil.ONE_METER_FACTOR;
		if (totalDistance > track.getClosedDistance()) {
			totalDistance = track.getClosedDistance();
		}
		List<Point2D> points = track.getPoints(totalDistance, startGuidePoint, numberOfPoints);

		StringBuilder out = new StringBuilder();
		for (Point2D point : points) {
			point.setLocation(point.getX() + (offset * xslope), point.getY() + (offset * yslope));
			out.append(PredictionUtil.toTrackPosition(point, currentPosition.getAltitude()));
		}
		out.append(PredictionUtil.toTrackPosition(currentPoint, currentPosition.getAltitude()));
		return out.toString();

	}
	

}
