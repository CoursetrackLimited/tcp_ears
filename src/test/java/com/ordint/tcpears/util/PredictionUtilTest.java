package com.ordint.tcpears.util;

import java.awt.geom.Point2D;

import org.junit.Test;

public class PredictionUtilTest {
	
	String track ="-0.406451,51.420073,10 -0.406448,51.420102,10 -0.406446,51.420127,10 -0.406445,51.420153,10 -0.406443,51.420203,10 -0.406442,51.420230,10 ";  

	PredictionUtil util = new PredictionUtil();
	
	

	
	@Test
	public void doh() throws Exception {
		
		Point2D p1 = new Point2D.Double(-4, 4);
		
		Point2D p2 = new Point2D.Double(-1, 6);
		
		double length = Math.sqrt(Math.pow( (p1.getX() - p2.getX()), 2) + Math.pow((p1.getY() - p2.getY()), 2));
		
		System.out.println(length);
		
		double unitSlopeX = (p1.getX() - p2.getX()) / length;
		double unitSlopeY = (p1.getY() - p2.getY()) / length;
		
		
		System.out.println(unitSlopeX);
		System.out.println(unitSlopeY);
		
		
		
	}
	

}
