package com.ordint.tcpears.track.geom;

import static java.lang.Double.parseDouble;

import java.awt.geom.Point2D;

import org.apache.commons.lang3.StringUtils;

import com.ordint.tcpears.domain.Position;

public interface PositionToPointConverter extends LatLonToMeters {
	
	Point2D toPoint(String trackPosition);
	
	Point2D toPoint(Position position);
}
