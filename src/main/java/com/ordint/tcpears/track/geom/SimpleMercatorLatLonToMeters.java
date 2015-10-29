package com.ordint.tcpears.track.geom;

public class SimpleMercatorLatLonToMeters implements LatLonToMeters {
	
	private final static double EARTH_RADIUS = 6356752.3142;
											  
	
	public SimpleMercatorLatLonToMeters() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double metersToLat(double y) {
		double exp = Math.exp(y / EARTH_RADIUS);		
		return Math.toDegrees((2 * Math.atan(exp)) - (Math.PI/2));
	}

	@Override
	public double latToMeters(double lat) {
		double lat_rad = Math.toRadians(lat);
		return Math.log((Math.sin(lat_rad) + 1) / Math.cos(lat_rad)) * EARTH_RADIUS;
		//double tan = Math.tan((Math.toRadians(lat) + (Math.PI/2))/2);
		//return Math.log(tan) * EARTH_RADIUS;	
	}

	@Override
	public double metersToLon(double x) {
		
		return Math.toDegrees(x / EARTH_RADIUS);
	}

	@Override
	public double lonToMeters(double lon) {

		return Math.toRadians(lon) * EARTH_RADIUS;
	}

}
