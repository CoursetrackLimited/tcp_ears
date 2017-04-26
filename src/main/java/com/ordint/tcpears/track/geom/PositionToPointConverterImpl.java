package com.ordint.tcpears.track.geom;

import static java.lang.Double.parseDouble;

import java.awt.geom.Point2D;

import org.apache.commons.lang3.StringUtils;

import com.ordint.tcpears.domain.lombok.Position;

public class PositionToPointConverterImpl implements PositionToPointConverter {
	
	private LatLonToMeters latLonToMeters;

	public PositionToPointConverterImpl(LatLonToMeters latLonToMeters) {
		this.latLonToMeters = latLonToMeters;
	}
	
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#metersToLat(double)
	 */
	@Override
	public double metersToLat(double y) {
		return latLonToMeters.metersToLat(y);
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#latToMeters(double)
	 */
	@Override
	public double latToMeters(double lat) {
		return latLonToMeters.latToMeters(lat);
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#metersToLon(double)
	 */
	@Override
	public double metersToLon(double x) {
		return latLonToMeters.metersToLon(x);
	}
	/* (non-Javadoc)
	 * @see com.ordint.tcpears.track.geom.LatLonToMeters#lonToMeters(double)
	 */
	@Override
	public double lonToMeters(double lon) {
		return latLonToMeters.lonToMeters(lon);
	}	
	public  Point2D toPoint(String trackPosition) {
		String cells[] = StringUtils.split(trackPosition, ",");
		return toPoint(parseDouble(cells[1]), parseDouble(cells[0]));
	}
	private  Point2D toPoint(double lat, double lon) {
		return new Point2D.Double(lonToMeters(lon),latToMeters(lat));
	}
	public Point2D toPoint(Position position) {
		return toPoint(parseDouble(position.getLat()), parseDouble(position.getLon()));
	}	

}


