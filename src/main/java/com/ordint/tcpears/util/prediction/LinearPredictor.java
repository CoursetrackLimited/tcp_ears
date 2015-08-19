package com.ordint.tcpears.util.prediction;

import static java.lang.Double.parseDouble;

import java.awt.geom.Point2D;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.util.PredictionUtil;

public class LinearPredictor implements PositionPredictor<Position> {

	@Override
	public String predict(Position currentPosition, int predictionTime, int numberOfPoints, Position previousPosition) {
		Point2D previous = PredictionUtil.toPoint(previousPosition);
		Point2D current = PredictionUtil.toPoint(currentPosition);
		//double length = Math.sqrt(Math.pow( (current.getX() - prediction.getX()), 2) + Math.pow((current.getY() - prediction.getY()), 2));	
		double length = current.distance(previous);
		double unitSlopeX = (current.getX() - previous.getX()) / length;
		double unitSlopeY = (current.getY() - previous.getY()) / length;		

		double totalDistance = parseDouble(currentPosition.getSpeed()) * (predictionTime/1000) * PredictionUtil.ONE_METER_FACTOR;

		double interval = totalDistance / numberOfPoints;
		
		StringBuilder out = new StringBuilder();

		for(int i = numberOfPoints; i > 0; i --) {
			Point2D point = new Point2D.Double(current.getX() + (unitSlopeX * interval * i),
					current.getY() + (unitSlopeY * interval * i));
			out.append(PredictionUtil.toTrackPosition(point, currentPosition.getAltitude()));
		}
		out.append(PredictionUtil.toTrackPosition(current, currentPosition.getAltitude()));
		return out.toString();
	}

}
