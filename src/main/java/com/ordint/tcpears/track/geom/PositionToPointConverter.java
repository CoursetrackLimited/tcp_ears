package com.ordint.tcpears.track.geom;

import java.awt.geom.Point2D;

import com.ordint.tcpears.domain.Position;

public interface PositionToPointConverter extends LatLonToMeters {
	
	Point2D toPoint(String trackPosition);
	
	Point2D toPoint(Position position);
}
