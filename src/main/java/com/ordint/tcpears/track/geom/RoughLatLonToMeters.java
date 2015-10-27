package com.ordint.tcpears.track.geom;

public class RoughLatLonToMeters implements LatLonToMeters {

	public final static double EARTH_RADIUS = 6_371_000;
	public final double CENTER ;
	public RoughLatLonToMeters(double center) {
		CENTER = Math.cos(Math.toRadians(center));
	}
	
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#metersToLat(double)
	 */
	@Override
	public double metersToLat(double y) {
		return Math.toDegrees(y/ EARTH_RADIUS);
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#latToMeters(double)
	 */
	@Override
	public double latToMeters(double lat) {
		return Math.toRadians(lat) * EARTH_RADIUS;
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#metersToLon(double)
	 */
	@Override
	public double metersToLon(double x) {
		return Math.toDegrees(x / (CENTER * EARTH_RADIUS));
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#lonToMeters(double)
	 */
	@Override
	public double lonToMeters(double lon) {
		return Math.toRadians(lon * CENTER * EARTH_RADIUS); 
	}	

}
