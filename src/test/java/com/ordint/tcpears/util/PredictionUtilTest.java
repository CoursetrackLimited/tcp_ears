package com.ordint.tcpears.util;

import static org.junit.Assert.assertThat;
import io.netty.handler.codec.UnsupportedMessageTypeException;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.geom.Point2D;
import java.util.Scanner;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;
import org.gps.utils.ReferenceEllipsoids;
import org.gps.utils.UTMUtils;
import org.gps.utils.UTMPoint;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionUtil;

public class PredictionUtilTest {
	
	
	PredictionUtil util = new PredictionUtil();
	
	//@Test
	public void hoof() throws Exception {
		
		PolynomialCurveFitter c = PolynomialCurveFitter.create(3);
		PolynomialFunctionLagrangeForm f = new PolynomialFunctionLagrangeForm(new double[] {1,1.4,1.6,1.7,2.4}, 
				new double[] {1,1.5,1.9,2.3,3.4});	
		System.out.println(f.value(1.9));
	
		UTMPoint p = UTMUtils.LLtoUTM(ReferenceEllipsoids.WGS_60,50.23121, -0.23442);
		
		System.out.println(p);
		
		LatLonPoint ll = UTMUtils.UTMtoLL(ReferenceEllipsoids.WGS_60, p);
				
		
		System.out.println("---------" + ll.getLongitude() + " " + ll.getLatitude());
	}
	
	@Test 
	public void shouldConvertTrackPositionToPoint() throws Exception {
		
		Point2D p = util.toPoint("-0.23442,50.23121,10 ");
		
		assertThat(util.toTrackPosition(p, "10"), Matchers.equalTo("-0.23442,50.23121,10 "));
		LatLng currentPostion = new LatLng(51.420230, -0.406442);
		LatLng result = LatLngTool.travel(currentPostion, 0, 1, LengthUnit.METER);
		
		System.out.println("One meter " + (result.getLatitude() - currentPostion.getLatitude()));
		
	}
	
	//@Test
	public void shouldCreateCurve() throws Exception {
		String p[]= new String[] {
				"-0.406442,51.420230,10 ",
				"-0.406443,51.420203,10 ",
				"-0.406445,51.420153,10 ",
				"-0.406446,51.420127,10 ",
				"-0.406448,51.420102,10 ",
				"-0.406451,51.420073,10 ",
				};
		
		//System.out.println(util.toPoint(p[1]));
		//System.out.println(util.toPoint(p[3]));
		
		PolynomialFunctionLagrangeForm f = util.createCurve(p);
		
		System.out.println("---" + f.value(-0.406453));;
		
	}
	
	@Test
	public void outerBound() throws Exception {
		Position p = Position.builder()
				.altitude("10")
				.speed("15")
				.lon("-0.406439")
				.lat("51.420240")
				.build();
				
		Point2D point = util.outerBound(p, "-0.406442,51.420230,10 ", 5000);
		
		System.out.println(point);
	
		
	}
	
	@Test
	public void predict() throws Exception {
		Position p = Position.builder()
				.altitude("10")
				.speed("15")
				.lon("-0.406439")
				.lat("51.420240")
				.build();
		String track ="-0.406451,51.420073,10 -0.406448,51.420102,10 -0.406446,51.420127,10 -0.406445,51.420153,10 -0.406443,51.420203,10 -0.406442,51.420230,10 ";  
	
		System.out.println(util.predict(p, track, 5000, 5));
	}
	

}
