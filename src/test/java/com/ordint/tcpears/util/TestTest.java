package com.ordint.tcpears.util;

import static java.lang.Math.abs;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.MaskFormatter;

import org.springframework.test.context.transaction.TestTransaction;

import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;
  
public class TestTest extends JPanel
{
	static final double[] offsets = new double[] { -0.402276,51.419584,-0.397179,51.414296};
	private static final double SCALE = 1000000;
    public static void main(String[] args)
    {
    	
    	
    	
    	
    	
   
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });     
        
    }
    @Override
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Point2D a = new Point2D.Double(upX(-0.4018246066470643), upY(51.41986586173692));
        Point2D b = new Point2D.Double(upX(-0.4017332700389376), upY(51.41977387312371));
        Point2D p2 = new Point2D.Double(upX( -0.401674), upY(51.419884));
        
        Point2D p1 = new Point2D.Double(upX(-0.401862), upY(51.419764));
        Point2D p = new Point2D.Double(upX(-0.401732), upY( 51.419845));
        Point2D r = new Point2D.Double(upX(-0.4017922407539071), upY(51.419833264800296));
       // davesClosestPoint(a, b, p);
       // myGetClosestPoint(a, b, p);
        System.out.println(a);
        System.out.println(b);
        System.out.println(p1);
        
        Point2D Bx = nearestPointOnLine(a, b, p);
        Point2D Cx = nearestPointOnLine(a, b, p2);
        System.out.println(tomsGo(a, b, p1));
        System.out.println(yetAnother(a, b, p1));
        System.out.println(Bx);
        System.out.println(Cx);
        
        Line2D track = new Line2D.Double(a,b);
        Line2D b1 = new Line2D.Double(p1, Bx);
        Line2D b2 = new Line2D.Double(p1, Cx);
        
        System.out.println(p1.distance(Bx) + " " + p1.distance(Cx) + " " + track.ptSegDist(p));
        
        double slope = getSlope(a, b);
        System.out.println(slope);
        double normal = -1/slope;
        double dist = track.ptSegDist(p);
        double sin = 1 /(Math.sqrt(1 + Math.pow(normal,2)));
        double cos = normal / (Math.sqrt(1 + Math.pow(normal,2)));
        
        
        
        Point2D f = new Point2D.Double( (p.getX() - (dist * cos)), (p.getY() - (dist * sin)));
        System.out.println(f);    	
        printScale(a);
        printScale(p);
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.scale(1, 1);
      	//g2d.scale(1 * SCALE,1 * SCALE);
    	//g2d.scale(1, -1);
    	//g2d.translate(0, -200);
    	//g2d.translate(-(2000 * offsets[0]),400);
    	//g2d.scale(10, 10);
    	
/*    	g2d.draw(track);
    	g2d.draw( new Line2D.Double(a, p2));
    	g2d.draw( new Line2D.Double(b, p));
    	g2d.draw( new Line2D.Double(Bx, p));
    	g2d.setColor(Color.BLUE);
    	g2d.draw( new Line2D.Double(f, p));
    	g2d.draw( new Line2D.Double(new Point2D.Double(-0.402276,10), p));
    	g2d.setColor(Color.GREEN);
    	g2d.draw(new Line2D.Double(nearestPointOnLine(a,b, p),p));
    	g2d.setColor(Color.RED);
    	g2d.draw( new Line2D.Double(a, new Point2D.Double(200,10)));
    
    	
    	Point2D from = new Point2D.Double(984.0999999999989,-556.8999999994162);
    	Point2D cp = new Point2D.Double(991.9720359099493,-557.6679697789189);
    	Line2D seg = new Line2D.Double(976.4680028907791,-553.671524469479,975.3303648729894,-565.3328262084756);
    	g2d.setColor(Color.ORANGE);
    	g2d.draw(seg);
    	g2d.setColor(Color.BLACK);
    	g2d.draw(new Line2D.Double(cp,from));
    	g2d.setColor(Color.GREEN);
    	g2d.draw(new Line2D.Double(seg.getP1(),from));
    	g2d.setColor(Color.RED);
    	g2d.draw(new Line2D.Double(seg.getP2(),from));
    	
    	
    	Point2D y = new Point2D.Double(600, -300);
    	Point2D x = new  Point2D.Double(500, -100);
    	Point2D z = new Point2D.Double(300, -200);
    	Line2D l = new Line2D.Double(x,y);
    	g2d.draw(l);
    	
    	g2d.draw(new Line2D.Double(y, z));
    	
    	g2d.draw(new Line2D.Double(z, MeasuredShape.calculateClosestPoint(l, l.ptSegDist(z), z)));
    	
    	g2d.translate(0, 0);*/
    	Line2D arse = new Line2D.Double(a,b);
    	g2d.setColor(Color.RED);
    	g2d.draw(arse);
    	Point2D  cx = MeasuredShape.calculateClosestPoint(arse, arse.ptSegDist(p2), p2);
    	Line2D bandit = new Line2D.Double(p2, cx);
    	
    	g2d.draw(bandit);
    	System.out.println(MeasuredShape.validSegment(arse, p2));
    	System.out.println(MeasuredShape.validSegment(arse, p2));
    	
    }
	private static Point2D upP(Point2D p) {
		return new Point2D.Double(upX(p.getX()), upY(p.getY()));
	}
	private static Line2D upL(Line2D l) {
		return new Line2D.Double(upP(l.getP1()), upP(l.getP2()));
	}   
    private static void  printScale(Point2D p) {
    	//
    	
    	System.out.println("OFFSET " + (p.getX() + (-1 * offsets[0])) + " " + ( p.getY()  -(1 * offsets[1])));
    	
    	System.out.println("SCALING " + (p.getX() * SCALE +  " " + ( p.getY() * SCALE )));
    	
    }
    private static void createAndShowGUI() {
        //Create and set up the window.
    	TestTest t = new TestTest();
    	t.setPreferredSize(new Dimension(1500, 1000));
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        JLabel emptyLabel = new JLabel("");
       
        //frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);
        frame.getContentPane().add(t);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
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
    private static double getSlope(Point2D pt1, Point2D pt2)
    {
        double deltaY = pt2.getY() - pt1.getY();
        double deltaX = pt2.getX() - pt1.getX();
  
        return deltaY / deltaX;
    }
  
    private static Point2D davesClosestPoint(Point2D pt1, Point2D pt2, Point2D p)
    {
        double m = getSlope(pt1, pt2);
        double x = (m * m * pt1.getX() - m * (pt1.getY() - p.getY()) + p.getX()) / (m * m + 1.0);
        double y = (m * m * p.getY() - m * (pt1.getX() - p.getX()) + pt1.getY()) / (m * m + 1.0);
        double yQuick = m * (x - pt1.getX()) + pt1.getY();
        System.out.println(pt1.toString());
        System.out.println(pt2.toString());
        System.out.println(getSlope(pt1, pt2));
        System.out.println(-1/getSlope(pt1, pt2));
        System.out.println(
                "(" + x + "," + y + ") y=" + yQuick + ", dist=" + 
                distance(p, x, y));
  
        return new Point((int)(x + 0.5), (int)(y+0.5));
    }
    private static double[] davesClosestPoint2(double x1, double y1, double x2, double y2, Point2D p)
    {
        double m = (y2-y1)/(x2-x1);
        double x = (m * m * x1 - m * (y1 - p.getY()) + p.getX()) / (m * m + 1.0);
        double y = (m * m * p.getY() - m * (x1 - p.getX()) + y1) / (m * m + 1.0);
        double yQuick = m * (x - x1) + y1;

  
        return new double[] {x,y};
    } 
    private static Point2D yetAnother(Point2D pt1, Point2D pt2, Point2D externalPoint) {
    	
    	Point2D bestPoint = new Point2D.Double();
    	Line2D.Double line = new Line2D.Double(pt1, pt2);
    	
    	 double u = 
                 ((externalPoint.getX() - line.x1) * (line.x2 - line.x1) + (externalPoint.getY() - line.y1) * (line.y2 - line.y1))
               / ((line.x2 - line.x1) * (line.x2 - line.x1) + (line.y2 - line.y1) * (line.y2 - line.y1));

    	
		double top = ((externalPoint.getX() - line.x1) * (line.x2 - line.x1) ) +  (externalPoint.getY() - abs(line.y1) * (line.y2 - line.y1));
		double bottom = Math.pow(line.x2 - line.x1, 2) + Math.pow(line.y2 - line.y1, 2);
		double u2 = top/bottom;
		double xu = line.x1 + (u * 1 * (line.x2 - line.x1)) ;
		double yu = line.y1 + (u * 1 * (line.y2 - line.y1));
		if (u < 0) {
			bestPoint.setLocation(line.getP1());
		} else if (u > 1) {
			bestPoint.setLocation(line.getP2());
		} else {
			bestPoint.setLocation(xu, yu);
		}    	
		return new Point2D.Double(downX(bestPoint.getX()), downY(bestPoint.getY()));
    }
    private static Point2D tomsGo(Point2D pt1, Point2D pt2, Point2D p) {
    	
    	Line2D l = new Line2D.Double(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
    	
    	double dist = l.ptSegDist(p);
    	//double xlen = Math.sqrt((Math.pow(p.getX()-pt1.getX(), 2) + Math.pow(p.getY() - pt1.getY(), 2)) - l.ptSegDistSq(p));
    	double normalSlope = -1/getSlope(pt1, pt2);
    	double x =  p.getX() + (dist / Math.sqrt((1 + Math.pow(normalSlope, 2))) );
    	double y = p.getY() + (normalSlope * (x-p.getX()));
    	
    	return new Point2D.Double(downX(x), downY(y));
    }
    private static Point2D myGetClosestPoint(Point2D pt1, Point2D pt2, Point2D p)
    {
        double u = ((p.getX() - pt1.getX()) * (pt2.getX() - pt1.getX()) + 
                   (p.getY() - pt1.getY()) * (pt2.getY() - pt1.getY())) / (sqr(pt2.getX() - pt1.getX()) + 
                   sqr(pt2.getY() - pt1.getY()));
  
        // if (u > 1.0)
        //    return (Point)pt2.clone();
        // else if (u <= 0.0)
        //     return (Point)pt1.clone();
        // else
        // {
            double x = (pt2.getX() * u + pt1.getX() * (1.0 - u));
            double y = (pt2.getY() * u + pt1.getY() * (1.0 - u));
            double m = getSlope(pt1, pt2);
            double yQuick = m * (x - pt1.getX()) + pt1.getY();
            System.out.println(
                    "(" + x + "," + y + ") y=" + yQuick + ", dist=" + 
                    distance(p, x, y));
  
            return new Point((int)(x + 0.5), (int)(y+0.5));
        // }
    }
	public static Point2D nearestPointOnLine(Point2D pt1, Point2D pt2, Point2D p) {
	    // Thanks StackOverflow!
	    // http://stackoverflow.com/questions/1459368/snap-point-to-a-line-java
		
	    

	    double p1x = p.getX() - pt1.getX();
	    double p1y = p.getY() - pt1.getY();
	    double p1ToP2x = pt2.getX() - pt1.getX();
	    double p1ToP2y = pt2.getY() - pt1.getY();

	    double ab2 = p1ToP2x * p1ToP2x + p1ToP2y * p1ToP2y;
	    double ap_ab = p1x * p1ToP2x + p1y * p1ToP2y;
	    double t = 1- ap_ab / ab2;
	    System.out.println("t = " + t);
        if (t < 0) {
            t = 0;
        } else if (t > 1) {
            t = 1;
        }
	    
	   //return new Point2D.Double(downX(pt1.getX() + p1ToP2x * t), downY(pt1.getY() + p1ToP2y * t));
	   return new Point2D.Double(pt1.getX() + p1ToP2x * t, pt1.getY() + p1ToP2y * t);
	   
	}  
    private static double sqr(double x)
    {
        return x * x;
    }
  
    private static double distance(Point2D from, double x, double y)
    {
        double deltaX = from.getX() - x;
        double deltaY = from.getY() - y;
  
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}