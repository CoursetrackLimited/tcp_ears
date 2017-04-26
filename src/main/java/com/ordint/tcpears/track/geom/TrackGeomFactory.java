package com.ordint.tcpears.track.geom;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TrackGeomFactory {
	
	public MeasuredShape createTrackShape(String kmlPoints, PositionToPointConverter converter) {
		String[] trackPoints = kmlPoints.split(" "); 
		return buildShapeFromKml(trackPoints, converter);		
	}
	
	public PositionToPointConverter createPositionToPointConverter(String kmlPoints) {
		String[] trackPoints = kmlPoints.split(" "); 
		return new PositionToPointConverterImpl(new RoughLatLonToMeters(calculateCenter(trackPoints)));		
	}
	private double calculateCenter(String[] trackPoints) {
		//average lat
		double centre = 0;
		int count = 0;
		for(String point:trackPoints) {
			String val = StringUtils.substringBetween(point, ",");
			if (!StringUtils.isBlank(val)) {
				centre = centre + Double.parseDouble(val);
				count++;
			}
		}
		return centre / count;
	
	}
	protected MeasuredShape buildShapeFromKml(String[] trackPoints, PositionToPointConverter converter) {
		
		List<Point2D> allpoints = new ArrayList<>();
		for(String point : trackPoints) {
			if (!StringUtils.isBlank(point)) {
				allpoints.add(converter.toPoint(point));
			}
		}
		
		return new MeasuredShape(allpoints);
		
	}
	
}
