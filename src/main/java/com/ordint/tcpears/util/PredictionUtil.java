package com.ordint.tcpears.util;

import static java.lang.Double.parseDouble;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.gps.utils.LatLonPoint;
import org.gps.utils.ReferenceEllipsoids;
import org.gps.utils.UTMPoint;
import org.gps.utils.UTMUtils;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.ordint.tcpears.domain.Position;

public class PredictionUtil {
	
	
	public String predict(Position p, String track, int predictionTime, int numberOfPoints) {
		
		final float ONE_METER_FACTOR = (float) 8.000000001118224E-6;
		//convert track lat lngs to x,y cords
		String positions[] = StringUtils.split(track, " ", 6);
		
		if (positions.length < 5) {
			return null;
		}
		
		
		PolynomialFunctionLagrangeForm curve = createCurve(positions);
		
		//calculate furthest prediction point based on heading and speed
		
		Point2D outerBound = outerBound(p, positions[0], predictionTime);
		//calulate intermediate prediction x values
		Point2D currentPosition = toPoint(p);

		double[] xvals = extrapolationValues(currentPosition, outerBound, numberOfPoints);
		
		//create 2DPath using function values values
		Path2D path = new Path2D.Double();
		
		List<Point2D> allpoints = new ArrayList<>();
		allpoints.add(currentPosition);
		allpoints.addAll(predictedCurvePoints(curve, xvals));
		boolean first = true;
		for(Point2D pos : allpoints) {
			if(first) {
				path.moveTo(pos.getX(), pos.getY());
				first = false;
			} else {
				path.lineTo(pos.getX(), pos.getY());
			}
		}
		
		MeasuredShape shape = new MeasuredShape(path);
		//get final prediction points based on distance
		float totalDistance = Float.parseFloat(p.getSpeed()) * (predictionTime/1000) * ONE_METER_FACTOR;
		if (totalDistance > shape.getOriginalDistance()) {
			totalDistance = shape.getOriginalDistance();
		}
		float interval = totalDistance / numberOfPoints;
		List<Point2D> retval = new ArrayList<>();
		StringBuffer out = new StringBuffer();
		//out.append("<locations-file file-format-version=\"1.0\">");
		for(int i = numberOfPoints; i > 0; i --) {
			Point2D point = shape.getPoint(interval * (i), null);
			out.append(toTrackPosition(point, "10"));
		}
		
		//convert back in to lat lgs
	
		
		//out.append("</locations-file>");
		return out.toString();
	}
	
	protected List<Point2D> predictedCurvePoints(PolynomialFunctionLagrangeForm curve, double[] xvals) {
		List<Point2D> retval = new ArrayList<>();
		for(double x : xvals) {
			retval.add(new Point2D.Double(x, curve.value(x)));
		}
		return retval;
	}
	
	
	protected  PolynomialFunctionLagrangeForm createCurve(String[] positions) {
		double[] x= new double[3], y = new double[3];
		for(int i = 0; i < 3 - 1; i++) {
			Point2D point = toPoint(positions[i]);
			x[i] = point.getX();
			y[i] = point.getY();
			
		}
		return new PolynomialFunctionLagrangeForm(x, y);
	}
	
	protected double[] extrapolationValues2(Point2D currentPosition, Point2D outerBound, int numberOfPoints) {
		double interval = 0;
		double retval[] = new double[numberOfPoints];
		if (currentPosition.getX() > outerBound.getX() ) {
			interval = currentPosition.getX() - outerBound.getX();
			double increment = interval / numberOfPoints;
			for(int i = 0; i < numberOfPoints; i ++) {
				retval[i] = outerBound.getX() + (increment * (i+1));
			}
		} else {
			interval = outerBound.getX() - currentPosition.getX();
			double increment = interval / numberOfPoints;
			for(int i = 0; i < numberOfPoints; i ++) {
				retval[i] = currentPosition.getX() + (increment * (i+1));
			}
		}
		
		return retval;
	}
	protected double[] extrapolationValues(Point2D currentPosition, Point2D outerBound, int numberOfPoints) {	
		double interval = 0, lower = 0, higher = 0;
		double retval[] = new double[numberOfPoints];
		if (currentPosition.getX() > outerBound.getX() ) {
			interval = currentPosition.getX() - outerBound.getX();
			lower = outerBound.getX();
		} else {
			interval = outerBound.getX() - currentPosition.getX();
			lower = currentPosition.getX();

		}
		
		double increment = interval / numberOfPoints;
		for(int i = 0; i < numberOfPoints; i ++) {
			retval[i] = lower + (increment * (i+1));
		}
		return retval;
	}
	
	protected Point2D outerBound(Position p, String lastTrackPostion,  int predictionTime) {
		
		LatLng currentPostion = new LatLng(parseDouble(p.getLat()), parseDouble(p.getLon()));
		LatLong lastPosition = new LatLong(lastTrackPostion);
		double bearing = LatLngTool.initialBearing(lastPosition.toLatLng(), currentPostion);
		double distance = Double.parseDouble(p.getSpeed()) * predictionTime / 1000;
		
		LatLng result = LatLngTool.travel(currentPostion, bearing, distance, LengthUnit.METER);
		return toPoint(result.getLatitude(), result.getLongitude());
	}
	
	protected Point2D toPoint(String trackPosition) {
		LatLong l = new LatLong(trackPosition);
		return toPoint(l.lat, l.lon);
	}
	
	protected Point2D toPoint(double lat, double lon) {
		
		//UTMPoint p = UTMUtils.LLtoUTM(ReferenceEllipsoids.WGS_60, lat, lon);
		//System.out.println(lat + " " + lon + " " + p);
		//return new Point2D.Double(p.getEasting(), p.getNorthing());
		return new Point2D.Double(lon,lat);
	}
	
	protected Point2D toPoint(Position position) {
		
		return toPoint(parseDouble(position.getLat()), parseDouble(position.getLon()));
	}
	
	protected String toTrackPosition(Point2D point, String accuracy) {
		
		StringBuilder out = new StringBuilder();
		
		//LatLonPoint latLonPoint = UTMUtils.UTMtoLL(ReferenceEllipsoids.WGS_60,  (float)point.getY(),(float)point.getX(), 30,'U');
				
		//out.append(latLonPoint.getLongitude()).append(",").append(latLonPoint.getLatitude()).append(",").append(accuracy).append(" ");
		out.append(point.getX()).append(",").append(point.getY()).append(",").append(accuracy).append(" ");
		return out.toString();
	}
	
	protected String toLoc(Point2D point) {

	   String location = "<location><point1> %s, %s, %s </point1><point2 /></location>";
	   return String.format(location, "" +point.getX(), "" + point.getY(), "10");

	}
	
	
}

class LatLong {
	final double lat, lon;
	LatLong(String trackPosition) {
		String cells[] = StringUtils.split(trackPosition, ",");
		lat = parseDouble(cells[1]);
		lon = parseDouble(cells[0]);
		
	}
	
	LatLng toLatLng() {
		return new LatLng(lat, lon);
	}
}

