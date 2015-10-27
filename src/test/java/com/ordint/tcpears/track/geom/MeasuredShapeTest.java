package com.ordint.tcpears.track.geom;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.commons.math3.util.Precision;
import org.junit.Before;
import org.junit.Test;


public class MeasuredShapeTest {
	private MeasuredShape shape;
	@Before
	public void setUp() throws Exception {
		
		double[][] points = new double[][] {
			{3,2},{3,4},{4,7},{5,9},{7,11},{10,13},{13,14},{16,14},{19,12},{21,10},{22,7}
		};
		
		ArrayList<Point2D> points2D = new ArrayList<>();
		for(double[] xy : points) {
			points2D.add(new Point2D.Double(xy[0], xy[1]));
		}
		shape = new MeasuredShape(points2D);
	}
	
	@Test 
	public void getPointDistance() throws Exception {
		Point2D point = new Point2D.Double(6, 10);
		double distance = shape.getPointDistance(point);
		double expected = 2 + Math.sqrt(10) + Math.sqrt(5) + Math.sqrt(2);
		assertThat(distance, equalTo(expected));
		
	}
	@Test 
	public void getPointDistanceShouldReturnZeroIfNotOnShape() throws Exception {
		
		Point2D point = new Point2D.Double(4, 10);
		double distance = shape.getPointDistance(point);
		
		assertThat(distance, equalTo(0.0));
	}
	
	@Test 
	public void getPointShouldReturnPointSpecifiedDistanceFromStartOfShape() throws Exception {
		double distance  = 2 + Math.sqrt(10) + Math.sqrt(5) + Math.sqrt(2);
		Point2D expected = new Point2D.Double(6, 10);
		Point2D actual = shape.getPoint(distance);
		
		assertThat(eqPoint(actual, expected), equalTo(true));
		
	}
	@Test(expected = IllegalArgumentException.class)
	public void getPointShouldThrowIllegalArgumentExceptionWhenDistanceExceedsShapeLength() throws Exception {
		double distance  = shape.getClosedDistance() + 1;
		
		Point2D actual = shape.getPoint(distance);
		
		assertThat(actual, nullValue());
		
	}
	@Test 
	public void getPointShouldReturnPointSpecifiedDistanceFromStartPoint() throws Exception {
		double distance  = Math.sqrt(10) + Math.sqrt(5) + Math.sqrt(2);
		Point2D start = new Point2D.Double(3,4);
		Point2D expected = new Point2D.Double(6, 10);
		Point2D actual = shape.getPoint(distance, start);
		
		assertThat(eqPoint(actual, expected), equalTo(true));
		
	}
	@Test 
	public void getClosestPoint() throws Exception {
		Point2D externalPoint = new Point2D.Double(6, 12);
		Point2D actual = shape.getClosestPoint(externalPoint);
		Point2D expected =  new Point2D.Double(7, 11);
		
		assertThat(eqPoint(actual, expected), equalTo(true));
		
	}
	@Test 
	public void getClosestPoint1() throws Exception {
		Point2D externalPoint = new Point2D.Double(5, 12);
		Point2D actual = shape.getOffTrackPoint(externalPoint, Math.sqrt(150));
		Point2D expected =  new Point2D.Double(7, 11);
		System.out.println(actual);
		System.out.println(expected);
		//assertThat(eqPoint(actual, expected), equalTo(true));
		
	}	
	private static boolean eqPoint(Point2D p1, Point2D p2) {
		return Precision.equals(p1.getX(), p2.getX(), MeasuredShape.EIGHT_DP) && 
				Precision.equals(p1.getY(), p2.getY(), MeasuredShape.EIGHT_DP);		
	}
	

	
	
	
	
	
}
