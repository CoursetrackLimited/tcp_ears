package com.ordint.tcpears.track.geom;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.awt.geom.Point2D;

import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionUtil;

public class PositionToPointConverterImplTest {

	private final static double LAT = 51.419744;
	private final static double LON = -0.401670;
	private final static double X = -27853.825420170127;
	private final static double Y = 5717614.66216199;
	
	PositionToPointConverterImpl latlongConverter = new PositionToPointConverterImpl(new RoughLatLonToMeters(51.417930));
	@Before
	public void setUp() throws Exception {
	
	
	}

	@Test
	public void shouldConvertTrackPositionToPoint() throws Exception {
		Point2D actual = latlongConverter.toPoint(String.format("%s,%s,10.2 ", LON, LAT));
		Point2D expected = new Point2D.Double(X, Y);
		
		assertThat(actual, equalTo(expected));
		
	}
/*
	@Test
	public void shouldConvertLatLongToPoint() throws Exception {
		Point2D actual = latlongConverter.toPoint(LAT, LON);
		Point2D expected = new Point2D.Double(X, Y);
		
		assertThat(actual, equalTo(expected));
	}*/

	@Test
	public void shouldConvertPositionToPoint() throws Exception {
		Position p = PositionUtil.createPosition(Double.toString(LAT), Double.toString(LON), "-1");
		Point2D actual = latlongConverter.toPoint(p);
		Point2D expected = new Point2D.Double(X, Y);
		
		assertThat(actual, equalTo(expected));
	}

/*	@Test
	public void shouldConvertToTrackPosition() throws Exception {
		String trackPosition = latlongConverter.toTrackPosition(new Point2D.Double(X, Y), "-1");
		assertThat(trackPosition, equalTo(String.format("%s,%s,-1 ", LON, LAT)));
	}

	@Test
	public void getMostRecentTrackPositionShouldReturnNull() throws Exception {
		String actual = latlongConverter.getMostRecentPosition("");
		assertThat(actual, nullValue());
	}
	@Test
	public void getMostRecentTrackPositionShouldReturnOnlyPosition() throws Exception {
		String actual = latlongConverter.getMostRecentPosition("12.33,04.00004,10.90 ");
		
		assertThat(actual, equalTo("12.33,04.00004,10.90 "));
	}
	@Test
	public void getMostRecentTrackPositionShouldReturnFirstPosition() throws Exception {
		String actual = latlongConverter.getMostRecentPosition("12.33,04.00004,10.90 32.3333,12.333,-1 ");
		
		assertThat(actual, equalTo("12.33,04.00004,10.90 "));
	}
	*/
	@Test
	public void latLongToMeters() throws Exception {
		
		double lat, lon, x, y;
		lat = 51.419844;
		lon =  -0.401731;
		y =  latlongConverter.latToMeters(lat);
		x =  latlongConverter.lonToMeters(lon);
		System.out.println("Lat TO meters " + y);
		System.out.println("Lon TO meters " + x);
		System.out.println("y TO lat " + latlongConverter.metersToLat(y));
		System.out.println("y + 1 TO lat " + latlongConverter.metersToLat(y+1));
		System.out.println("x TO lon " + latlongConverter.metersToLon(x));
		
		System.out.println("Lat to meters " + (Math.log(Math.tan((Math.PI/4) + 0.5 * Math.toRadians(lat)))) * 6378137.0);
		Mercator m = new Mercator();
		System.out.println(m.latToMeters(lat));
		System.out.println(m.lonToMeters(lon));
		Point2D p = fromLatLngToPoint(lat, lon, 2);
		System.out.println("GoogleMaps from latLongToPoint " + p);
		System.out.println("GoogleMaps from pointToLatLon " + fromPointToLatLng(p, 2));
		
		SimpleMercatorConverter s = new SimpleMercatorConverter();
		System.out.println("SMC lat " + s.latToMeters(lat));
		System.out.println("SMC lon " + s.lonToMeters(lon));
		
		
	}
    Point2D fromLatLngToPoint(double lat, double lng, int zoom)
    {
    	int TILE_SIZE = 256;
        
        double _pixelsPerLonDegree = TILE_SIZE / 360.0;
        double _pixelsPerLonRadian = TILE_SIZE / (2 * Math.PI);
    	

        double x = TILE_SIZE/2.0 + lng * _pixelsPerLonDegree;       

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        double siny = bound(Math.sin(Math.toRadians(lat)), -0.9999,0.9999);
        //double siny = Math.sin(Math.toRadians(lat));
        double y = TILE_SIZE/2.0  + 0.5 * Math.log((1 + siny) / (1 - siny)) *- _pixelsPerLonRadian;

        int numTiles = 1 << zoom;
        x = x * numTiles;
        y = y * numTiles;
        return new Point2D.Double(x, y);
     }
    Point2D fromPointToLatLng(Point2D point, int zoom)
    {
    	int TILE_SIZE = 256;
        double _pixelsPerLonDegree = TILE_SIZE / 360.0;
        double _pixelsPerLonRadian = TILE_SIZE / (2 * Math.PI);
        int numTiles = 1 << zoom;
        double x = point.getX() / numTiles;
        double y = point.getY() / numTiles;       

        double lng = (x - TILE_SIZE/2.0) / _pixelsPerLonDegree;
        double latRadians = (y - TILE_SIZE/2.0) / - _pixelsPerLonRadian;
        double lat = Math.toDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
        return new Point2D.Double(lat, lng);
    }
    
    
    double bound(double val, double valMin, double valMax)
    {
        double res;
        res = Math.max(val, valMin);
        res = Math.min(res, valMax);
        return res;
    }
	public class Mercator {
	    final private static double R_MAJOR = 6378137.0;
	    final private static double R_MINOR = 6356752.3142;
	 
	    public double[] merc(double x, double y) {
	        return new double[] {lonToMeters(x), latToMeters(y)};
	    }
	 
	    public double  lonToMeters(double lon) {
	        return R_MAJOR * Math.toRadians(lon);
	    }
	 
	    public double latToMeters(double lat) {
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
	        double ts = Math.tan(0.5 * ((Math.PI*0.5) - phi)) / con;
	        return 0 - R_MAJOR * Math.log(ts);
	       
	    }
	}	
	
}
