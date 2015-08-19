package com.ordint.tcpears.util.prediction;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.ordint.tcpears.util.PredictionUtil;

public class AbstractTrackBuilder {

	protected Path2D buildTrack(String kmlPoints) {
		String[] trackPoints = kmlPoints.split(" ");
		List<Point2D> allpoints = new ArrayList<>();
		for(String trackPosition : trackPoints) {
			allpoints.add(PredictionUtil.toPoint(trackPosition));
		}	
		return buildPath(allpoints);
		
	}
	
	protected Path2D buildPath(List<Point2D> allpoints) {	
		Path2D path = new Path2D.Double();		
		boolean first = true;
		for(Point2D pos : allpoints) {
			if(first) {
				path.moveTo(pos.getX(), pos.getY());
				first = false;
			} else {
				path.lineTo(pos.getX(), pos.getY());
			}
		}
		path.closePath();		
		return path;
	}
}
