package com.ordint.tcpears.util;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
 
public class CloseUp extends JPanel {
    Point2D.Double[] points;
    CubicCurve2D.Double curve;
    boolean firstTime = true;
    MeasuredShape shape;
    public CloseUp() {
        points = new Point2D.Double[4];
        points[0] = new Point2D.Double(355.0, 268.0);
        points[1] = new Point2D.Double( 20.0, 179.0);
        points[2] = new Point2D.Double( 77.0, 158.0);
        points[3] = new Point2D.Double(288.0,  32.0);
        curve = new CubicCurve2D.Double();
        curve.setCurve(points, 0);
        shape = new MeasuredShape(curve.getPathIterator(null,  0.0001));
    }
 
    private Point2D getPoint(float distance) {
      

       
        
        
        return shape.getPoint(distance, null);
        
       
    }
 

 
    public static void main(String[] args) {
/*        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new CloseUp());
        f.setSize(400,400);
        f.setLocation(100,100);
        f.setVisible(true);*/
        long t = System.currentTimeMillis();
        CloseUp c = new CloseUp();
        System.out.println(c.getPoint(3000f));
        System.out.println(c.getPoint(150f));
        System.out.println(c.getPoint(100f));
        System.out.println(c.getPoint(50f));
        System.out.println((System.currentTimeMillis() -t));
    }
}