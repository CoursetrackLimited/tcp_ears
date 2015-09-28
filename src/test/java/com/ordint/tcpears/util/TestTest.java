package com.ordint.tcpears.util;

import static java.lang.Math.abs;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.MaskFormatter;

import org.springframework.test.context.transaction.TestTransaction;

import com.ordint.tcpears.util.prediction.AbstractTrackBuilder;
import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;
  
public class TestTest extends JPanel
{
	static final double[] offsets = new double[] { -0.402276,51.419584,-0.397179,51.414296};
	private static final double SCALE = 1000000;
	
	
    private MouseHandler mouseHandler = new MouseHandler();

    GeneralPath path = null;
    
    Point2D p1 = null;
    Point2D p2 = null;
    Point2D externalPoint = null;
    Point2D closestPoint = null;
    private MeasuredShape shape;
    private PointPathBuilder  pathBuilder = new PointPathBuilder(); 
    private boolean drawing = false;
    public TestTest() {
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }
    
    public static void main(String[] args)
    {
    	
    	
    	TestTest t = new TestTest();
    	
    	
   
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                t.createAndShowGUI();
            }
        });     
        
    }
    @Override
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Point2D a = new Point2D.Double(upX(-0.4018246066470643), upY(51.41986586173692));
        Point2D b = new Point2D.Double(upX(-0.4017332700389376), upY(51.41977387312371));
        //Point2D p2 = new Point2D.Double(upX( -0.401674), upY(51.419884));
        
        //Point2D p1 = new Point2D.Double(upX(-0.401862), upY(51.419764));
        //Point2D p = new Point2D.Double(upX(-0.401732), upY( 51.419845));
        //Point2D r = new Point2D.Double(upX(-0.4017922407539071), upY(51.419833264800296));
       // davesClosestPoint(a, b, p);
       // myGetClosestPoint(a, b, p);
        System.out.println(a);
        System.out.println(b);


    	Graphics2D g2d = (Graphics2D)g;
    	g2d.scale(1, 1);
    	g2d.translate(750, 500);
    	Line2D grid1 = new Line2D.Double(-750,0, 1250, 0);
    	Line2D grid2 = new Line2D.Double(0,-500,0,1000);
    	g2d.draw(grid1);
    	g2d.draw(grid2);
    	if (p1 != null && p2 != null) {
	    	Line2D arse = new Line2D.Double(p1,p2);
	    	g2d.setColor(Color.RED);
	    	g2d.draw(arse);
	    	if (externalPoint != null) {
	    		shape = new MeasuredShape(pathBuilder.buildFromLine(arse));
	    		
	    		Point2D  cx = shape.getClosestPoint(externalPoint);
	    		Line2D bandit = new Line2D.Double(externalPoint, cx);
	            System.out.println(shape.getDistanceAlongTrack(externalPoint));
	    		g2d.draw(bandit);
	    		//g2d.setColor(Color.GREEN);
	    		//g2d.draw(shape.test);
	    		//g2d.draw(shape.l);
	    	}
    	
    	}
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
    private  void createAndShowGUI() {
        //Create and set up the window.
    	
    	setPreferredSize(new Dimension(1000, 800));
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
       
       
        //frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);
        frame.getContentPane().add(this);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
        	
        	if(e.getButton() ==1) {
	            Point p = e.getPoint(); 
	            System.out.println("CLicked @ " + p);
	            if (true) {
	            	if (p1 == null) {
	            		p1 = new Point2D.Double(1*p.getX() -750 ,1*p.getY()- 500);
	            	} else {
	            		if (p2 == null) {
	            			p2 = new Point2D.Double(1*p.getX()-750,1*p.getY()-500);
	            		} else {
	            			p1 = null;
	            			p2 = null;
	            			externalPoint = null;
	            		}
	            	}
	            	
	                drawing = true;
	            } else {
	                path.lineTo(p.x, p.y);
	            }
        	} else if (e.getButton() ==3) {
        		Point p = e.getPoint(); 
        		externalPoint = new Point2D.Double(1*p.getX()-750,1* p.getY()-500);
        		
        	}

            repaint();
        }
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
    
    class PointPathBuilder extends AbstractTrackBuilder {
    	
    	public Path2D buildFromLine(Line2D line) {
    		return buildPath(Arrays.asList(line.getP1(), line.getP2()));
    	}
    	
    }
    
}