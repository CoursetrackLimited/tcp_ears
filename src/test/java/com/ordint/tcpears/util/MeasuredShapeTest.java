package com.ordint.tcpears.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.apache.commons.math3.util.Precision;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.domain.PositionUtil;
import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;
import com.ordint.tcpears.util.prediction.TrackBasedPredictor;

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
	
	
	@Test
	public void testGetClosesPoint() {
		//this is assuming that the algo we copied from stackoverfloww works
		StaticTrackPathBuilder builder = new  StaticTrackPathBuilder();
		Path2D track = builder.build("KEMPTON_740");
		
		TrackBasedPredictor t = new TrackBasedPredictor(track);
		
		MeasuredShape shape = new  MeasuredShape(track);
		
		// this point is on the track -0.3982040560018652,51.41628861332551,0
		
		Point2D p = PredictionUtil.toPoint(51.415837, -0.398032);
		
		
		System.out.println("CLosest Point 2 " + shape.getClosestPoint(p));
		

			long l = System.nanoTime();
		
		for(int i =0; i < 10000; i++) {
			
			Point2D p2 = shape.getClosestPoint(p);
			shape.getPointDistance(p2);
		}
		
		System.out.println((System.nanoTime() - l) / 1_000_000);
	}
	
	@Test
	public void helpme() {
        Point2D a = new Point2D.Double(-0.4018246066470643, 51.41986586173692);
        Point2D b = new Point2D.Double(-0.4017332700389376, 51.41977387312371);
        Point2D p = new Point2D.Double(-0.401862, 51.419764);
        Point2D r = new Point2D.Double(-0.4017922407539071, 51.419833264800296);	
        
        double s1 = (b.getY()-a.getY()) / (b.getX()-a.getX());
        double s2 = (p.getY()-r.getY()) / (r.getX()-p.getX());
        
        
        System.out.println(s1);
        
        System.out.println(s2);
        System.out.println(-1/s1);   
        
	}
	
	
	public void isValidSgement() {
		StaticTrackPathBuilder builder = new  StaticTrackPathBuilder();
		Path2D track = builder.build("KEMPTON_740");	
		MeasuredShape shape = new  MeasuredShape(track);
		
		//System.out.println(shape.validSegment(new Line2D.Double(0, 0, 6, 0), new Point2D.Double(3, 4)));
		
		//System.out.println(shape.validSegment(new Line2D.Double(0, 0, 6, 0), new Point2D.Double(30, 4)));
		//System.out.println(shape.validSegment(new Line2D.Double(0, 0, 6, 0), new Point2D.Double(3, -4)));
		assertTrue(shape.validSegment(new Line2D.Double(0, -1, -6, 0), new Point2D.Double(-3, -4)));
		
		assertTrue(shape.validSegment(new Line2D.Double(0, -1, -6, 0), new Point2D.Double(-8, -4)));
		
		assertFalse(shape.validSegment(new Line2D.Double(10, 10, 20, 10), new Point2D.Double(30, 9)));
		assertFalse(shape.validSegment(new Line2D.Double(10, 10, 20, 10), new Point2D.Double(30, 11)));
		
		assertTrue(shape.validSegment(new Line2D.Double(-10, -10, -20, -5), new Point2D.Double(-11, -9)));
	}
	
	@Test
	public void calculateDistanceTest() {
		StaticTrackPathBuilder builder = new  StaticTrackPathBuilder();
		Path2D track = builder.build("KEMPTON");	
		MeasuredShape shape = new  MeasuredShape(track);
		//-0.398705,51.416924
		//-0.398677,51.416965
		//-0.98769,51.416964		
		Position p1=PositionUtil.createPosition("51.416924", "-0.398705", "-1");
		Position p2=PositionUtil.createPosition("51.416965", "-0.398677","-1");
		Position p3=PositionUtil.createPosition("51.416964","-0.398769", "-1");
		
		
		System.out.println(shape.calculateDistanceInfo(p1));
		System.out.println(shape.calculateDistanceInfo(p2));
		System.out.println(shape.calculateDistanceInfo(p3));
		
		
		
		
	}
	
	
	
}
