package com.ordint.tcpears.util;

import static java.lang.Double.parseDouble;

import java.awt.geom.Point2D;

import org.apache.commons.lang3.StringUtils;

import com.ordint.tcpears.domain.Position;

public class PredictionUtil {
	
	public static final double ONE_METER_FACTOR = 8.000000001118224E-6;
	
	public static  Point2D toPoint(String trackPosition) {
		String cells[] = StringUtils.split(trackPosition, ",");
		return toPoint(parseDouble(cells[1]), parseDouble(cells[0]));
	}
	public static  Point2D toPoint(double lat, double lon) {
		return new Point2D.Double(lonToMeters(lon),latToMeters(lat));
	}
	public static Point2D toPoint(Position position) {
		return toPoint(parseDouble(position.getLat()), parseDouble(position.getLon()));
	}	
	public static  String toTrackPosition(Point2D point, String altitude) {
		StringBuilder out = new StringBuilder();
		out.append(metersToLon(point.getX())).append(",").append(metersToLat(point.getY())).append(",").append(altitude).append(" ");
		return out.toString();
	}
	
	public static  String getMostRecentPosition(String existingTrack) {
        int firstTripleIndex=existingTrack.indexOf(" ");
        if (firstTripleIndex > 0) {
        	return existingTrack.substring(0, firstTripleIndex + 1);			
        } else {
        	return null;
        }
	}
	public final static double EARTH_RADIUS = 6_371_000;
	public final static double CENTER = Math.cos(Math.toRadians(51.417930));
		
	
	public static double metersToLat(double y) {
		return Math.toDegrees(y/ EARTH_RADIUS);
	}
	public static double latToMeters(double lat) {
		return Math.toRadians(lat) * EARTH_RADIUS;
	}
	public static double metersToLon(double x) {
		return Math.toDegrees(x / (CENTER * EARTH_RADIUS));
	}
	public static double lonToMeters(double lon) {
		return Math.toRadians(lon * CENTER * EARTH_RADIUS);
	}	
	
	
	
	

	  public static double y2lat(double aY) {
	    return Math.toDegrees(2* Math.atan(Math.exp(Math.toRadians(aY))) - Math.PI/2);
	  }
	 
	  public static double lat2y(double aLat) {
	    return Math.toDegrees(Math.log(Math.tan(Math.PI/4+Math.toRadians(aLat)/2)));
	  }
			
	
	static final double[] offsets = new double[] { -0.402276,51.419584,-0.397179,51.414296};


	


}


