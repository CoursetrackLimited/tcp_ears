package com.ordint.tcpears.util;

import static java.lang.Double.parseDouble;

import java.awt.geom.Line2D;
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
	
	
	public class Mercator {
	    final private static double R_MAJOR = 6378137.0;
	    final private static double R_MINOR = 6356752.3142;
	 
	    public double[] merc(double x, double y) {
	        return new double[] {mercX(x), mercY(y)};
	    }
	 
	    private double  mercX(double lon) {
	        return R_MAJOR * Math.toRadians(lon);
	    }
	 
	    private double mercY(double lat) {
	        if (lat > 89.5) {
	            lat = 89.5;
	        }
	        if (lat < -89.5) {
	            lat = -89.5;
	        }
	        double temp = R_MINOR / R_MAJOR;
	        double es = 1.0 - (temp * temp);
	        double eccent = Math.sqrt(es);
	        double phi = Math.toRadians(lat);
	        double sinphi = Math.sin(phi);
	        double con = eccent * sinphi;
	        double com = 0.5 * eccent;
	        con = Math.pow(((1.0-con)/(1.0+con)), com);
	        double ts = Math.tan(0.5 * ((Math.PI*0.5) - phi))/con;
	        double y = 0 - R_MAJOR * Math.log(ts);
	        return y;
	    }
	}	
	

	  public static double y2lat(double aY) {
	    return Math.toDegrees(2* Math.atan(Math.exp(Math.toRadians(aY))) - Math.PI/2);
	  }
	 
	  public static double lat2y(double aLat) {
	    return Math.toDegrees(Math.log(Math.tan(Math.PI/4+Math.toRadians(aLat)/2)));
	  }
			
	
	
	
	
	
	
	
	static final double[] offsets = new double[] { -0.402276,51.419584,-0.397179,51.414296};
	private static final double SCALE = 1000000;

	private static Point2D upP(Point2D p) {
		return new Point2D.Double(upX(p.getX()), upY(p.getY()));
	}
	private static Line2D upL(Line2D l) {
		return new Line2D.Double(upP(l.getP1()), upP(l.getP2()));
	}   
	
    private static double upX(double x) {
    	//return x;
    	return (x * SCALE) - (offsets[0] * SCALE);	
    }
    private static double upY(double y) {
    	//return y;
    	return (y  * SCALE) -( offsets[1]*SCALE);
    }
    private static double downX(double x) {
    	//return x;
    	return (x/SCALE) + offsets[0];
    }
    private static double downY(double y) {
    	//return y;
    	return  (y/SCALE) + offsets[1];
    }

}


