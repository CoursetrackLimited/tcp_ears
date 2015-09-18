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
		Point2D startGuidePoint = getClosestPoint(currentPoint);
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
	
	
	
	public Point2D getClosestPoint(Point2D currentPoint) {
		Point2D.Double closestPoint = new Point2D.Double(-1, -1);
		Point2D.Double bestPoint = new Point2D.Double(-1, -1);
		ArrayList<Point2D.Double> closestPointList = new ArrayList<Point2D.Double>();
		ArrayList<Line2D.Double> areaSegments = new ArrayList<Line2D.Double>();
		// Note: we're storing double[] and not Point2D.Double
		ArrayList<double[]> areaPoints = new ArrayList<double[]>();
		double[] coords = new double[6];

		for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {

			// Because the Area is composed of straight lines
			int type = pi.currentSegment(coords);
			// We record a double array of {segment type, x coord, y coord}
			double[] pathIteratorCoords = { type, coords[0], coords[1] };
			areaPoints.add(pathIteratorCoords);
		}

		double[] start = new double[3]; // To record where each polygon starts
		for (int i = 0; i < areaPoints.size(); i++) {
			// If we're not on the last point, return a line from this point to the next
			double[] currentElement = areaPoints.get(i);

			// We need a default value in case we've reached the end of the ArrayList
			double[] nextElement = { -1, -1, -1 };
			if (i < areaPoints.size() - 1) {
				nextElement = areaPoints.get(i + 1);
			}

			// Make the lines
			if (currentElement[0] == PathIterator.SEG_MOVETO) {
				start = currentElement; // Record where the polygon started to close it later
			}

			if (nextElement[0] == PathIterator.SEG_LINETO) {
				areaSegments.add(new Line2D.Double(currentElement[1], currentElement[2], nextElement[1], nextElement[2]));
			} else if (nextElement[0] == PathIterator.SEG_CLOSE) {
				areaSegments.add(new Line2D.Double(currentElement[1], currentElement[2], start[1], start[2]));
			}
		}

		// Calculate the nearest point on the edge
		for (Line2D.Double line : areaSegments) {
			//System.out.println(String.format("(%s,%s) -(%s,%s)",line.x1, line.y1, line.x2, line.y2));
			// From: http://stackoverflow.com/questions/6176227
			double u = ((currentPoint.getX() - line.x1) * (line.x2 - line.x1) + (currentPoint.getY() - line.y1)
					* (line.y2 - line.y1))
					/ ((line.x2 - line.x1) * (line.x2 - line.x1) + (line.y2 - line.y1) * (line.y2 - line.y1));

			double xu = line.x1 + u * (line.x2 - line.x1);
			double yu = line.y1 + u * (line.y2 - line.y1);

			if (u < 0) {
				closestPoint.setLocation(line.getP1());
			} else if (u > 1) {
				closestPoint.setLocation(line.getP2());
			} else {
				closestPoint.setLocation(xu, yu);
			}

			closestPointList.add((Point2D.Double) closestPoint.clone());

			if (closestPoint.distance(currentPoint) < bestPoint.distance(currentPoint)) {
				bestPoint.setLocation(closestPoint);
			}
		}

		return bestPoint;
	}

}
