package com.ordint.tcpears.track.geom;

public interface LatLonToMeters {

	double metersToLat(double y);

	double latToMeters(double lat);

	double metersToLon(double x);

	double lonToMeters(double lon);

}