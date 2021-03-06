package com.ordint.tcpears.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.ordint.tcpears.track.geom.MeasuredShape;
  
public class DistanceCalculatorTest extends JPanel
{
    static final double[] offsets = new double[] { -0.402276,51.419584,-0.397179,51.414296};
    private static final double SCALE = 1000000;
    
    
    private MouseHandler mouseHandler = new MouseHandler();

    GeneralPath path = null;
    
    Point2D p1 = null;
    Point2D p2 = null;
    
    ArrayList<Point2D> points = new ArrayList<>();
    
    Point2D externalPoint = null;
    Point2D closestPoint = null;
    private MeasuredShape shape;
    private PointPathBuilder  pathBuilder = new PointPathBuilder(); 
    private boolean drawing = false;
    private double distance = 0;
    public DistanceCalculatorTest() {
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }
    
    public static void main(String[] args)
    {
        
        
        DistanceCalculatorTest t = new DistanceCalculatorTest();
        
        
   
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
        if (points.size() > 1) {
        
            g2d.setColor(Color.RED);
            for (int i =0; i < points.size() -1; i ++) {
                Line2D arse = new Line2D.Double(points.get(i),points.get(i+1));
               
                g2d.draw(arse);
            }
            

            if (externalPoint != null) {
                shape = new MeasuredShape(points);
                
       
                double[] retval =  shape.getDistanceAlongTrack(externalPoint, distance);
                Point2D  cx = new Point2D.Double(retval[0], retval[1]);
                Line2D bandit = new Line2D.Double(externalPoint, cx);
                
                distance =retval[2];
                System.out.println(distance);
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
                points.add( new Point2D.Double(1*p.getX()-750,1* p.getY()-500));

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
    
    class PointPathBuilder  {
        
        public Path2D buildFromLine(Line2D line) {
        return buildPath(Arrays.asList(line.getP1(), new Point2D.Double(line.getP2().getX(), line.getP1().getY()), line.getP2(),
               new Point2D.Double(line.getP1().getX(), line.getP2().getY())));
       // return buildPath(Arrays.asList(line.getP1(), line.getP2()));

        }
        protected Path2D buildPath(List<Point2D> allpoints) {   
            Path2D path = new Path2D.Double();      
            boolean first = true;
            for(Point2D pos : allpoints) {
                if(first) {
                    path.moveTo(pos.getX(), pos.getY());
                    first = false;
                } else {
                    path.lineTo(pos.getX(), pos.getY());
                }
            }
            path.closePath();       
            return path;
        }       
    }
    
}