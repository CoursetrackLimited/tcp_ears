package com.ordint.tcpears.util;

import static org.junit.Assert.assertThat;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.apache.commons.math3.util.Precision;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class MeasuredShapeTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testSetPoint() throws Exception {
		
		Path2D.Double path = new Path2D.Double();
		
		path.moveTo(1, 1);
		path.lineTo(2, 5);
		path.lineTo(3, 1);
		
		path.closePath();
		
		MeasuredShape shape = new MeasuredShape(path);
		
		Point2D p = shape.getPoint(4);
		
		System.out.println(p);
		
	}
	@Test 
	public void testGetPointDistance() {
		
		Path2D.Double path = new Path2D.Double();
		
		path.moveTo(1, 1);
		path.lineTo(2, 5);
		path.lineTo(3, 1);
		
		path.closePath();
		MeasuredShape shape = new MeasuredShape(path);
		
		Point2D.Double point = new Point2D.Double(1.9701426f, 4.8805704f);
		
		assertThat(Precision.round(shape.getPointDistance(point), 6), Matchers.equalTo(4.0));
		
	}
	
	@Test 
	public void testGetPoints() {
		
		Path2D.Double path = new Path2D.Double();
		
		path.moveTo(1, 1);
		path.lineTo(2, 5);
		path.lineTo(3, 1);
		
		path.closePath();
		MeasuredShape shape = new MeasuredShape(path);
		
		Point2D.Float point = new Point2D.Float(1.9701426f, 4.8805704f);
		
		System.out.println(shape.getPoints(4, 3));
		
	}
	
	@Test 
	public void testGetPoints2() {
		
		Path2D.Double path = new Path2D.Double();
		
		path.moveTo(1, 1);
		path.lineTo(1, 15);
		path.lineTo(10, 15);
		path.lineTo(10, 1);
		
		path.closePath();
		MeasuredShape shape = new MeasuredShape(path);
		
		Point2D p = shape.getPoint(4);
		
		System.out.println(p);
		
		System.out.println(shape.getPoints(10, p, 4));
		
	}
}
