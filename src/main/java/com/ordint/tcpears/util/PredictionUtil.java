package com.ordint.tcpears.util;

import static java.lang.Double.parseDouble;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;



import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.ordint.tcpears.domain.Position;

public class PredictionUtil {
	
	public static final double ONE_METER_FACTOR = 8.000000001118224E-6;
	
	public static  Point2D toPoint(String trackPosition) {
		String cells[] = StringUtils.split(trackPosition, ",");
		return toPoint(parseDouble(cells[1]), parseDouble(cells[0]));
	}
	public static  Point2D toPoint(double lat, double lon) {
		return new Point2D.Double(lon,lat);
	}
	public static Point2D toPoint(Position position) {
		return toPoint(parseDouble(position.getLat()), parseDouble(position.getLon()));
	}	
	public static  String toTrackPosition(Point2D point, String altitude) {
		StringBuilder out = new StringBuilder();
		out.append(point.getX()).append(",").append(point.getY()).append(",").append(altitude).append(" ");
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
}


