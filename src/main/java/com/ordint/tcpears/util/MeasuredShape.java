package com.ordint.tcpears.util;

/*
 * @(#)MeasuredShape.java
 *
 * $Date$
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */


import static java.awt.geom.Line2D.ptSegDist;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.domain.PositionDistanceInfo;

/** This represents a single closed path.
 * <P>This object can trace arbitrary amounts of itself using the
 * <code>writeShape()</code> methods.
 * 
 */
public class MeasuredShape implements Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(MeasuredShape.class);
	
	private static final long serialVersionUID = 1L;
	
	/** Because a MeasuredShape must be exactly 1 subpath, this method
	 * will safely break up a path into separate subpaths and create one
	 * MeasuredShape for each. 
	 * 
	 * @param s a path, possibly containing multiple subpaths
	 * @return a MeasuredShape object for each subpath in <code>i</code>
	 */
	public static MeasuredShape[] getSubpaths(Shape s) {
		return getSubpaths(s.getPathIterator(null),DEFAULT_SPACING);
	}

	/** Because a MeasuredShape must be exactly 1 subpath, this method
	 * will safely break up a path into separate subpaths and create one
	 * MeasuredShape for each. 
	 * 
	 * @param s a path, possibly containing multiple subpaths
	 * @param spacing the spacing to be used for each <code>MeasuredShape</code>
	 * @return a MeasuredShape object for each subpath in <code>i</code>
	 */
	public static MeasuredShape[] getSubpaths(Shape s,double spacing) {
		return getSubpaths(s.getPathIterator(null),spacing);
	}
	
	/** Because a MeasuredShape must be exactly 1 subpath, this method
	 * will safely break up a path into separate subpaths and create one
	 * MeasuredShape for each. 
	 * 
	 * @param i a path, possibly containing multiple subpaths
	 * @return a MeasuredShape object for each subpath in <code>i</code>
	 */
	public static MeasuredShape[] getSubpaths(PathIterator i) {
		return getSubpaths(i,DEFAULT_SPACING);
	}
	
	/** Because a MeasuredShape must be exactly 1 subpath, this method
	 * will safely break up a path into separate subpaths and create one
	 * MeasuredShape for each. 
	 * 
	 * @param i a path, possibly containing multiple subpaths
	 * @return a MeasuredShape object for each subpath in <code>i</code>
	 */
	public static MeasuredShape[] getSubpaths(PathIterator i,double spacing) {
		Vector<MeasuredShape> v = new Vector<MeasuredShape>();
		GeneralPath path = null;
		double[] coords = new double[6];
		while(i.isDone()==false) {
			int k = i.currentSegment(coords);
			if(k==PathIterator.SEG_MOVETO) {
				if(path!=null) {
					v.add(new MeasuredShape(path,spacing));
					path = null;
				}
				path = new GeneralPath();
				path.moveTo(coords[0],coords[1]);
			} else if(k==PathIterator.SEG_LINETO) {
				path.lineTo(coords[0],coords[1]);
			} else if(k==PathIterator.SEG_QUADTO) {
				path.quadTo(coords[0],coords[1],coords[2],coords[3]);
			} else if(k==PathIterator.SEG_CUBICTO) {
				path.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
			} else if(k==PathIterator.SEG_CLOSE) {
				path.closePath();
			}
			i.next();
		}
		if(path!=null) {
			v.add(new MeasuredShape(path,spacing));
			path = null;
		}
		return v.toArray(new MeasuredShape[v.size()]);
	}
	
	static class Segment implements Serializable {
		private static final long serialVersionUID = 1L;
		
		int type;
		double[] data;
		double realDistance;
		double normalizedDistance;
		
		public void write(PathWriter path,double t0,double t1) {
			if(t0==0 && t1==1) {
				if(type==PathIterator.SEG_MOVETO) {
					path.moveTo(data[0], data[1]);
				} else if(type==PathIterator.SEG_LINETO) {
					path.lineTo(data[2], data[3]);
				} else if(type==PathIterator.SEG_QUADTO) {
					path.quadTo(data[2],data[3],data[4],data[5]);
				} else if(type==PathIterator.SEG_CUBICTO) {
					path.curveTo(data[2],data[3],data[4],data[5],data[6],data[7]);
				} else {
					throw new RuntimeException();
				}
				return;
			} else if(t0==1 && t1==0) {
				if(type==PathIterator.SEG_MOVETO) {
					path.moveTo(data[0], data[1]);
				} else if(type==PathIterator.SEG_LINETO) {
					path.lineTo(data[0], data[1]);
				} else if(type==PathIterator.SEG_QUADTO) {
					path.quadTo(data[2],data[3],data[0],data[1]);
				} else if(type==PathIterator.SEG_CUBICTO) {
					path.curveTo(data[4],data[5],data[2],data[3],data[0],data[1]);
				} else {
					throw new RuntimeException();
				}
				return;
			}
			if(type==PathIterator.SEG_MOVETO) {
				path.moveTo(data[0], data[1]); //not sure what this means?
			} else if(type==PathIterator.SEG_LINETO) {
				path.lineTo(getX(t1),getY(t1));
			} else if(type==PathIterator.SEG_QUADTO) {
				double ax = data[0]-2*data[2]+data[4];
	 			double bx = -2*data[0]+2*data[2];
				double cx = data[0];
				double ay = data[1]-2*data[3]+data[5];
				double by = -2*data[1]+2*data[3];
				double cy = data[1];
				
				PathWriter.quadTo(path, t0, t1, ax, bx, cx, ay, by, cy);
			} else if(type==PathIterator.SEG_CUBICTO) {
				double ax = -data[0]+3*data[2]-3*data[4]+data[6];
				double bx = 3*data[0]-6*data[2]+3*data[4];
				double cx = -3*data[0]+3*data[2];
				double dx = data[0];
				double ay = -data[1]+3*data[3]-3*data[5]+data[7];
				double by = 3*data[1]-6*data[3]+3*data[5];
				double cy = -3*data[1]+3*data[3];
				double dy = data[1];
				PathWriter.cubicTo(path, t0, t1, ax, bx, cx, dx, ay, by, cy, dy);
			} else if(type==PathIterator.SEG_CLOSE) {
				path.closePath();
			} else {
				throw new RuntimeException();
			}
		}
		
		public double getTangentSlope(double t) {
			if(type==PathIterator.SEG_LINETO) {
				double ax = data[2]-data[0];
				double ay = data[3]-data[1];
				return (double)Math.atan2(ay, ax);
			} else if(type==PathIterator.SEG_QUADTO) {
				double ax = data[0]-2*data[2]+data[4];
				double bx = -2*data[0]+2*data[2];
				double ay = data[1]-2*data[3]+data[5];
				double by = -2*data[1]+2*data[3];
				return (double)Math.atan2( 2*ay*t+by, 2*ax*t+bx  );
			} else if(type==PathIterator.SEG_CUBICTO) {
				double ax = -data[0]+3*data[2]-3*data[4]+data[6];
				double bx = 3*data[0]-6*data[2]+3*data[4];
				double cx = -3*data[0]+3*data[2];
				double ay = -data[1]+3*data[3]-3*data[5]+data[7];
				double by = 3*data[1]-6*data[3]+3*data[5];
				double cy = -3*data[1]+3*data[3];
				return (double)Math.atan2( 3*ay*t*t+2*by*t+cy, 3*ax*t*t+2*bx*t+cx );
			} else if(type==PathIterator.SEG_MOVETO) {
				return data[0];
			} else if(type==PathIterator.SEG_CLOSE) {
				throw new RuntimeException();
			} else {
				throw new RuntimeException();
			}
		}
		
		public double getX(double t) {
			if(type==PathIterator.SEG_LINETO) {
				double ax = data[2]-data[0];
				return ax*t+data[0];
			} else if(type==PathIterator.SEG_QUADTO) {
				double ax = data[0]-2*data[2]+data[4];
				double bx = -2*data[0]+2*data[2];
				double cx = data[0];
				return (ax*t+bx)*t+cx;
			} else if(type==PathIterator.SEG_CUBICTO) {
				double ax = -data[0]+3*data[2]-3*data[4]+data[6];
				double bx = 3*data[0]-6*data[2]+3*data[4];
				double cx = -3*data[0]+3*data[2];
				double dx = data[0];
				return ((ax*t+bx)*t+cx)*t+dx;
			} else if(type==PathIterator.SEG_MOVETO) {
				return data[0];
			} else if(type==PathIterator.SEG_CLOSE) {
				throw new RuntimeException();
			} else {
				throw new RuntimeException();
			}
		}

		public double getY(double t) {
			if(type==PathIterator.SEG_LINETO) {
				double ay = data[3]-data[1];
				return ay*t+data[1];
			} else if(type==PathIterator.SEG_QUADTO) {
				double ay = data[1]-2*data[3]+data[5];
				double by = -2*data[1]+2*data[3];
				double cy = data[1];
				return (ay*t+by)*t+cy;
			} else if(type==PathIterator.SEG_CUBICTO) {
				double ay = -data[1]+3*data[3]-3*data[5]+data[7];
				double by = 3*data[1]-6*data[3]+3*data[5];
				double cy = -3*data[1]+3*data[3];
				double dy = data[1];
				return ((ay*t+by)*t+cy)*t+dy;
			} else if(type==PathIterator.SEG_MOVETO) {
				return data[1];
			} else if(type==PathIterator.SEG_CLOSE) {
				throw new RuntimeException();
			} else {
				throw new RuntimeException();
			}
		}
		
		public Segment(int type,double lastX,double lastY,double[] coords,double spacing) {
			this.type = type;
			if(type==PathIterator.SEG_MOVETO) {
				data = new double[] {coords[0],coords[1]};
				realDistance = 0;
			} else if(type==PathIterator.SEG_LINETO) {
				data = new double[] {lastX, lastY, coords[0],coords[1]};
				realDistance = (double)(Math.sqrt(
						(coords[0]-lastX)*(coords[0]-lastX) + (coords[1]-lastY)*(coords[1]-lastY) ));
			} else if(type==PathIterator.SEG_CLOSE) {
				data = new double[0];
			} else {
				double ax, bx, cx, dx, ay, by, cy, dy;
				if(type==PathIterator.SEG_QUADTO) {
					ay = 0;
					by = lastY-2*coords[1]+coords[3];
					cy = -2*lastY+2*coords[1];
					dy = lastY;
                
					ax = 0;
					bx = lastX-2*coords[0]+coords[2];
					cx = -2*lastX+2*coords[0];
					dx = lastX;
					data = new double[] {lastX, lastY, coords[0], coords[1], coords[2], coords[3]};
				} else if(type==PathIterator.SEG_CUBICTO) {
					ay = -lastY+3*coords[1]-3*coords[3]+coords[5];
					by = 3*lastY-6*coords[1]+3*coords[3];
					cy = -3*lastY+3*coords[1];
					dy = lastY;
                
					ax = -lastX+3*coords[0]-3*coords[2]+coords[4];
					bx = 3*lastX-6*coords[0]+3*coords[2];
					cx = -3*lastX+3*coords[0];
					dx = lastX;
					data = new double[] {lastX, lastY, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]};
				} else {
					throw new RuntimeException("Unrecognized type: "+type);
				}
				realDistance = calculateDistance(ax,bx,cx,dx,ay,by,cy,dy,spacing);
			}
		}
		
		private double calculateDistance(double ax,double bx,double cx,double dx,
				double ay,double by,double cy,double dy,double spacing) {
			double x0 = dx;
			double y0 = dy;
			double x1, y1;
			
			double sum = 0;
			for(double t = spacing; t<1; t+=spacing) {
				x1 = ((ax*t+bx)*t+cx)*t+dx;
				y1 = ((ay*t+by)*t+cy)*t+dy;
				sum += Math.sqrt( (x0-x1)*(x0-x1)+(y0-y1)*(y0-y1) );
				x0 = x1;
				y0 = y1;
			}
			return (double)sum;
		}
	}

	/** This is the increments t goes throw as each shape segment is
	 * traversed.  For quadratic and cubic curves, this affects
	 * how the shape distance is measured.  The default value is .05,
	 * meaning quadratic and cubic curves are converted to linear segments
	 * connecting at t = 0, t = .05, t = .1, ... t = .95, t = 1.
	 */
	public static final double DEFAULT_SPACING = .05f;

	private static final double EIGHT_DP = 0.00000001;
	
	Segment[] segments;
	double closedDistance = 0;
	double originalDistance;

	/** Construct a <code>MeasuredShape</code> from a <code>Shape</code>,
	 * using the default spacing.
	 * 
	 * @param s the shape data
	 * @throws IllegalArgumentException if the shape has more than 1 path.
	 */
	public MeasuredShape(Shape s) {
		this(s.getPathIterator(null),DEFAULT_SPACING);
	}
	
	/** Construct a <code>MeasuredShape</code> from a <code>Shape</code>.
	 * 
	 * @param s the shape data to create
	 * @param spacing the value to increment t as each segment is traversed.
	 * The default value is .05.
	 * @throws IllegalArgumentException if the shape has more than 1 path.
	 */
	public MeasuredShape(Shape s,double spacing) {
		this(s.getPathIterator(null),spacing);
	}

	/** Construct a <code>MeasuredShape</code> from a <code>PathIterator</code>
	 * using the default spacing.
	 * 
	 * @param i the shape data to create
	 * @throws IllegalArgumentException if the shape has more than 1 path.
	 */
	public MeasuredShape(PathIterator i) {
		this(i,DEFAULT_SPACING);
	}
	
	/** Construct a <code>MeasuredShape</code> from a <code>PathIterator</code>.
	 * 
	 * @param i the shape data to create
	 * @param spacing the value to increment t as each segment is traversed.
	 * The default value is .05.
	 * @throws IllegalArgumentException if the shape has more than 1 path.
	 */
	public MeasuredShape(PathIterator i,double spacing) {
		Vector<Segment> v = new Vector<Segment>();
		double lastX = 0;
		double lastY = 0;
		double moveX = 0;
		double moveY = 0;
		int pathCount = 0; 
		boolean closed = false;
		
		double[] coords = new double[6];
		while(i.isDone()==false) {
			int k = i.currentSegment(coords);
			if(k==PathIterator.SEG_CLOSE) {
				closed = true;
			} else if(k==PathIterator.SEG_MOVETO) {
				if(pathCount==1)
					throw new IllegalArgumentException("this object can only contain 1 subpath");
				moveX = coords[0];
				moveY = coords[1];
				lastX = moveX;
				lastY = moveY;
				pathCount++;
			} else if(k==PathIterator.SEG_LINETO ||
					k==PathIterator.SEG_QUADTO ||
					k==PathIterator.SEG_CUBICTO) {
				if(pathCount!=1)
					throw new IllegalArgumentException("this shape data did not begin with a moveTo");
				Segment s = new Segment(k,lastX,lastY,coords,spacing);
				lastX = s.data[s.data.length-2];
				lastY = s.data[s.data.length-1];
				v.add(s);
				closedDistance += s.realDistance;
			}
			i.next();
		}
		double t = closedDistance;
		if(v.size()>0) {
			Segment last = v.get(v.size()-1);
			if(Math.abs(last.data[last.data.length-2]-moveX)>.001 ||
					Math.abs(last.data[last.data.length-1]-moveY)>.001) {
				coords[0] = moveX;
				coords[1] = moveY;
				Segment s = new Segment(PathIterator.SEG_LINETO,lastX,lastY,coords,spacing);
				v.add(s);
				closedDistance += s.realDistance;
			}
		}
		if(!closed) {
			originalDistance = t;
		} else {
			originalDistance = closedDistance;
		}
		
		segments = v.toArray(new Segment[v.size()]);
		//normalize everything:
		for(int a = 0; a<segments.length; a++) {
			segments[a].normalizedDistance = segments[a].realDistance/closedDistance;
		}
	}
	
	/** Writes the entire shape
	 * @param w the destination to write to
	 */
	public void writeShape(PathWriter w) {
		w.moveTo(segments[0].getX(0), 
				segments[0].getY(0) );
		for(int a = 0; a<segments.length; a++) {
			segments[a].write(w,0,1);
		}
		w.closePath();
	}
	
	/** The distance of this shape, assuming that the path is closed.
	 * This will be greater than or equal to <code>getOriginalDistance()</code>.
	 * 
	 * @see #getOriginalDistance()
	 */
	public double getClosedDistance() {
		return closedDistance;
	}
	

	/** The distance of the shape used to construct this
	 * <code>MeasuredShape</code>.
	 * <p>This will be less than or equal to <code>getClosedDistance()</code>.
	 * 
	 * @return The distance this path covered when the shape was constructed.
	 * 
	 * @see #getClosedDistance()
	 */
	public double getOriginalDistance() {
		return originalDistance;
	}

	/** Writes the entire shape backwards
	 * @param w the destination to write to
	 */
	public void writeShapeBackwards(PathWriter w) {
		w.moveTo(segments[segments.length-1].getX(1), 
				segments[segments.length-1].getY(1) );
		for(int a = segments.length-1; a>=0; a--) {
			segments[a].write(w,1,0);
		}
		w.closePath();
	}
	
	/** Returns the x-value of where this path begins.
	 * <P>Because a <code>MeasuredShape</code> can only be one
	 * path, there is only possible <code>moveTo()</code>.
	 * 
	 * @return the x-value of where this path begins.
	 */
	public double getMoveToX() {
		Segment s = segments[0];
		return s.getX(0);
	}


	/** Returns the y-value of where this path begins.
	 * <P>Because a <code>MeasuredShape</code> can only be one
	 * path, there is only possible <code>moveTo()</code>.
	 * 
	 * @return the y-value of where this path begins.
	 */
	public double getMoveToY() {
		Segment s = segments[0];
		return s.getY(0);
	}
	

	/** Trace the shape.
	 * 
	 * @param position a fraction from zero to one indicating where to start tracing
	 * @param length a fraction from negative one to one indicating how much to trace.
	 * If this value is negative then the shape will be traced backwards.
	 * @param w the destination to write to
	 */
	public void writeShape(double position,double length,PathWriter w) {
		writeShape(position,length,w,true);
	}

	/** Trace the shape.
	 * 
	 * @param position a fraction from zero to one indicating where to start tracing
	 * @param length a fraction from negative one to one indicating how much to trace.
	 * If this value is negative then the shape will be traced backwards.
	 * @param w the destination to write to
	 * @param includeMoveTo this controls whether a moveTo is the first thing
	 * written to the path.
	 * Note setting this to <code>false</code> means its the caller's responsibility
	 * to make sure the path is in the correct position.
	 */
	public void writeShape(double position,double length,PathWriter w,boolean includeMoveTo) {
		if(length>=.999999f) {
			writeShape(w);
			return;
		} else if(length<=-.999999f) {
			writeShapeBackwards(w);
			return;
		} else if(length<.000001 && length>-.000001) {
			return;
		}
		
		Position i1 = getIndexOfPosition(position);
		Position i2 = getIndexOfPosition(position+length);
		
		if(includeMoveTo) {
			w.moveTo(segments[i1.i].getX(i1.innerPosition),
					segments[i1.i].getY(i1.innerPosition));
		}
		if(i1.i==i2.i && ((length>0 && i2.innerPosition>i1.innerPosition)
				|| (length<0 && i2.innerPosition<i1.innerPosition) )) {
			segments[i1.i].write(w,i1.innerPosition,i2.innerPosition);
		} else {
			if(length>0) {
				segments[i1.i].write(w,i1.innerPosition,1);
				int i = i1.i+1;
				if(i>=segments.length)
					i = 0;
				while(i!=i2.i) {
					segments[i].write(w,0,1);
					i++;
					if(i>=segments.length)
						i = 0;
				}
				segments[i2.i].write(w,0,i2.innerPosition);
			} else {
				segments[i1.i].write(w,i1.innerPosition,0);
				int i = i1.i-1;
				if(i<0)
					i = segments.length-1;
				while(i!=i2.i) {
					segments[i].write(w,1,0);
					i--;
					if(i<0)
						i = segments.length-1;
				}
				segments[i2.i].write(w,1,i2.innerPosition);
			}
		}
		
	}
	
	/** Returns the point at a certain distance from the beginning of this shape.
	 * 
	 * @param distance the distance from the beginning of this shape to measure
	 * @param dest the destination to store the result in.  (If this is null a new
	 * Point2D will be constructed.)
	 * @return the point at a certain distance from the beginning of this shape.
	 * Note this will be <code>dest</code> if <code>dest</code> is non-null.
	 */
	public Point2D getPoint(double distance) {
		if(distance<0) throw new IllegalArgumentException("distance ("+distance+") must not be negative");
		if(distance>closedDistance) throw new IllegalArgumentException("distance ("+distance+") must not be greater than the total distance of this shape ("+closedDistance+")");
		Point2D dest = new Point2D.Double();
		for(int a = 0; a<segments.length; a++) {
			double t = distance/segments[a].realDistance;
			if(t>=1) {
				distance = distance - segments[a].realDistance;
			} else {
				dest.setLocation(segments[a].getX(t),segments[a].getY(t));
				return dest;
			}
		}
		dest.setLocation(segments[0].getX(0),segments[0].getY(0)); //a fluke case, where we're basically at the end of the shape
		return dest;
	}
	/** 
	 * Returns an evenly spaced number of points over a distance from the start of the
	 * track, the first point beeing the futherest away
	 * 
	 * @param distance
	 * @param numberOfPoints
	 * @return
	 */
	public List<Point2D> getPoints(double distance, int numberOfPoints) {
		List<Point2D> points = new ArrayList<>();
		for(int i = 0; i < numberOfPoints; i++) {
			points.add(getPoint(distance  - (i * distance / numberOfPoints)));
		}
		return points;
	}
	
	/**
	 * Returns the distance from the start of the track of this point
	 * 
	 * @param point
	 * @return
	 */

	public double getPointDistance(Point2D point) {
		double distance = 0;
		double x = point.getX();
		double y = point.getY();
		for(int a = 0; a<segments.length; a++) {
			double t1 = (segments[a].getX(1) - segments[a].getX(0)) / (segments[a].getY(1) - segments[a].getY(0)) ;
			double t2 = (x - segments[a].getX(0) ) / (y-segments[a].getY(0)) ;
			if (Math.abs(t1 - t2) < EIGHT_DP) {
				distance = distance + distance(point, segments[a]);
				return distance;
			}
			distance = distance + segments[a].realDistance;
		}
		
		
		return 0;
	}
	/**
	 * Returns the point the given distance on from the give point
	 * 
	 * @param distance
	 * @param start
	 * @return
	 */
	public Point2D getPoint(double distance, Point2D start) {
		double offset = getPointDistance(start);
		return getPoint(distance + offset);
	}
	/**
	 * 
	 * @param distance
	 * @param start
	 * @param numberOfPoints
	 * @return
	 */
	public List<Point2D> getPoints(double distance, Point2D start, int numberOfPoints) {
		double startFrom = getPointDistance(start);
		List<Point2D> points = new ArrayList<>();
		for(int i = 0; i < numberOfPoints; i++) {
			points.add(getPoint(distance + startFrom - (i * distance / numberOfPoints)));
		}
		return points;
	}
	/**
	 * Returns the a Point on the track that is closes to specified point
	 * 
	 * @param externalPoint
	 * @return
	 */
	public Point2D getClosestPoint(Point2D externalPoint) {

		double shortestDistance = java.lang.Double.MAX_VALUE;
		double currentDistance = shortestDistance; 
		Line2D lineToUse = null;
		Segment segmentToUse = null;
		
		for (int i = 0 ; i< segments.length-1; i++) {
			if(segments[i].type != PathIterator.SEG_LINETO) {
				throw new IllegalArgumentException("Cant run getCLosestPoint on track not made of line segments");
			}
			Line2D.Double line = new Line2D.Double(segments[i].data[0], segments[i].data[1], segments[i].data[2], segments[i].data[3]);
			
			//if(validSegment(line, externalPoint)) {
				currentDistance = line.ptSegDist(externalPoint);
				if (currentDistance < shortestDistance) {
					shortestDistance =  currentDistance;
					//System.out.println(shortestDistance);
					//System.out.println("--- " + externalPoint.distance(line.getP1()) + " -- " + externalPoint.distance(line.getP2()));
					lineToUse = line;
					segmentToUse = segments[i];
				}
			//}
		}
		if (lineToUse == null) {
			return null;
		}
		return calculateClosestPoint(lineToUse, segmentToUse, shortestDistance, externalPoint);
		
		
		
	}
	
	public PositionDistanceInfo calculateDistanceInfo(com.ordint.tcpears.domain.Position position) {
		Point2D currentPoint = PredictionUtil.toPoint(position);
		double[] info = getDistanceAlongTrack(currentPoint);
		if (info != null) {
			return new PositionDistanceInfo(position, info[2], -1, 0);
		} else {
			log.warn("Could not cacluclate distance info for {}", position);
			return  new PositionDistanceInfo(position, -1, -1, 0);
		}
		
	}
	/**
	 * Returns a Point2D whose position is calculated by moving to the nearest
	 * point on the track, travelling along the track for a distance determined
	 * by the timeInMilis and speed parameters and then offsetting the point by
	 * the amount required to place the original point on the track
	 * 
	 * @param offTrackpoint
	 * @param speed in meters per second
	 * @param timeInMilis 
	 * @return
	 */
	public Point2D predict(Point2D offTrackpoint, double speed, double timeInMilis) {
		
		Point2D startGuidePoint = getClosestPoint(offTrackpoint);
		double offset = offTrackpoint.distance(startGuidePoint);
		double cos = (offTrackpoint.getX() - startGuidePoint.getX()) / offset;
		double sin = (offTrackpoint.getY() - startGuidePoint.getY()) / offset;
		
		// get final prediction points based on distance
		double distance = speed * (timeInMilis / 1000);
		Point2D retval = getPoint(distance, startGuidePoint);
		retval.setLocation(retval.getX() + (offset * cos), retval.getY() + (offset * sin));
		return retval;
		
	}
	/**
	 * Finds the nearset point on this track to the offTrackpoint value and
	 * calculates how far from the start of the track the point is
	 * 
	 * @param offTrackpoint
	 * @return a double array containint the x,y coordinates of the nearest point and its distance form the start
	 */
	public double[] getDistanceAlongTrack(Point2D offTrackpoint) {

		double shortestDistance = java.lang.Double.MAX_VALUE;
		double currentDistance = java.lang.Double.MAX_VALUE; 
		int segmentIndex =  -1;
		
		for (int i = 0 ; i< segments.length-1; i++) {
			if(segments[i].type != PathIterator.SEG_LINETO) {
				throw new IllegalArgumentException("Cant run getCLosestPoint on track not made of line segments");
			}
			currentDistance = ptSegDist(segments[i].data[0], segments[i].data[1], segments[i].data[2], segments[i].data[3],
						offTrackpoint.getX(),offTrackpoint.getY());
			if (currentDistance < shortestDistance) {
				shortestDistance =  currentDistance;
				//System.out.println(shortestDistance);
				//System.out.println("--- " + externalPoint.distance(line.getP1()) + " -- " + externalPoint.distance(line.getP2()));
				segmentIndex = i;
			}
			//}
		}
		if (segmentIndex == -1) {
			return null;
		}
		return calculateDistance(segmentIndex, shortestDistance, offTrackpoint);
		
		
		
	}	
	
	public static boolean validSegment(Line2D line1, Point2D externalPoint) {
		double x1,x2,x3,y1,y2,y3;
		if(true)return true;
		x1 = line1.getX1();
		x2 = line1.getX2();
		x3 = externalPoint.getX();
		y1 = line1.getY1();
		y2 = line1.getY2();
		y3 = externalPoint.getY();
		
		
		double dotAB = (x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1);
		double dotBC = (x3 - x2) * (x1-x2) + (y3-y2) * (y1-y2); 

		if(dotAB * dotBC < 0) {
			return  false;
		} 
		
		return true;
	}
	public  Point2D calculateClosestPoint(Line2D segmentLine, Segment segment, double distance, Point2D from) {
		
        double normal = getNormal(segmentLine);
        double cos = 1 /(Math.sqrt(1 + Math.pow(normal,2)));
        double sin = normal / (Math.sqrt(1 + Math.pow(normal,2)));
        
		double x1, x2,x3,  y1, y2, y3;
		x1 = segmentLine.getX1();
		x2 = segmentLine.getX2();
		x3 = from.getX();
		y1 = segmentLine.getY1();
		y2 = segmentLine.getY2();
		y3 = from.getY();
		
		
		double sgn = Math.signum(((x3-x1) * (y2-y1)) - ((y3-y1) * (x2-x1))); 
		if(y2 < y1) {
			sgn  = sgn * -1;
		}
        
        Point2D closestPoint = new Point2D.Double(from.getX() - sgn * (distance * cos), from.getY() -  sgn * (distance * sin));

        return closestPoint;
	}
	
	public double[] calculateDistance(int segmentIndex, double distance, Point2D from) {
		double x1, x2,x3,  y1, y2, y3;
		x1 = segments[segmentIndex].data[0];
		x2 = segments[segmentIndex].data[2];
		x3 = from.getX();
		y1 = segments[segmentIndex].data[1];
		y2 = segments[segmentIndex].data[3];
		y3 = from.getY();		

        double normal = -1 / ((y2-y1)/(x2-x1));
        double cos = 1 /(Math.sqrt(1 + Math.pow(normal,2)));
        double sin = normal / (Math.sqrt(1 + Math.pow(normal,2)));
        

		double sgn = Math.signum(((x3-x1) * (y2-y1)) - ((y3-y1) * (x2-x1))); 
		if(y2 < y1) {
			sgn  = sgn * -1;
		}
		
        double cpx = from.getX() - sgn *(distance * cos);
        double cpy = from.getY() -  sgn * (distance * sin);
        double dist = 0;     
        int range = inRange(x1, y1, x2, y2, cpx,cpy);
        if(range ==-1) {
        	dist = 0;
        } else if (range ==1) {
        	dist = segments[segmentIndex].realDistance;
		} else {
			 double a2 = from.distanceSq(x1,y1);
			 double b2 = from.distanceSq(cpx, cpy);
			 dist =  Math.sqrt(Math.abs(a2-b2)) ;
		}
    	
    	//add all the previous segment distances;
    	for(int i = 0 ; i < segmentIndex; i++) {
    		dist = dist + segments[i].realDistance;
    	}
        return new double[] {cpx, cpy, dist};
	}
	


	public static int inRange(double x1, double y1, double x2, double y2,
            double px, double py) {
		//double sgn = Math.signum(((x3-x1) * (dy) - ((y3-y1) * (dx)));
		double dx = x2 - x1;
		double dy = y2 - y1;
		double innerProduct = (px - x1)*dx + (py - y1)*dy;
		if (innerProduct < 0) {
			return -1;
		} else if (innerProduct > dx*dx + dy*dy) {
			return 1;
		} else {
			return 0;
		}
			
		//return 0 <= innerProduct && innerProduct <= dx*dx + dy*dy;
	}	

    private static double getNormal(Line2D line)
    {
        return -1 / getSlope(line);
    }
   
    
    private static double getSlope(Line2D line)
    {
        double deltaY = line.getY2()- line.getY1();
        double deltaX = line.getX2() - line.getX1();
  
        return deltaY/ deltaX;
    }	
    private double distance(Point2D pt, Segment a) {
        double px = pt.getX() - a.getX(0);
        double py = pt.getY() - a.getY(0);
        return Math.sqrt(px * px + py * py);
    }
	
	/** Returns the tangent slope at a certain distance from the beginning of this shape.
	 * The behavior of this method when the point you request falls exactly on an edge
	 * (that is, when two bordering segments don't have a continuous slope) is undefined.
	 * 
	 * @param distance the distance from the beginning of this shape to measure
	 * @return the tangent slope (in radians) at a specific position
	 */
	public double getTangentSlope(double distance) {
		if(distance<0) throw new IllegalArgumentException("distance ("+distance+") must not be negative");
		if(distance>closedDistance) throw new IllegalArgumentException("distance ("+distance+") must not be greater than the total distance of this shape ("+closedDistance+")");
		for(int a = 0; a<segments.length; a++) {
			double t = distance/segments[a].realDistance;
			if(t>=1) {
				distance = distance - segments[a].realDistance;
			} else {
				return segments[a].getTangentSlope(t);
			}
		}
		return segments[0].getTangentSlope(0); //a fluke case, where we're basically at the end of the shape
	}
	
	private static boolean equal(double f1,double f2) {
		double d = f1-f2;
		if(d<0) d = -d;
		return d<.0001;
	}

	/** Returns the length that this shape has in common with the argument.
	 * This assumes the two shapes begin at the same point, and in the same
	 * direction.
	 * @param s
	 */
	public double getCommonDistance(MeasuredShape s) {
		double distance = 0;
		int m = Math.min(segments.length, s.segments.length);
		for(int a = 0; a<m; a++) {
			if(segments[a].type!=PathIterator.SEG_MOVETO &&
					s.segments[a].type!=PathIterator.SEG_MOVETO) {
				if(equal(segments[a].data[0],s.segments[a].data[0]) &&
						equal(segments[a].data[1],s.segments[a].data[1]) &&
						equal(segments[a].data[segments[a].data.length-2], s.segments[a].data[s.segments[a].data.length-2]) &&
						equal(segments[a].data[segments[a].data.length-1], s.segments[a].data[s.segments[a].data.length-1]) &&
						equal(segments[a].realDistance, s.segments[a].realDistance)) {
					distance += segments[a].realDistance;
				} else {
					return distance;
				}
			} else if(segments[a].type==PathIterator.SEG_MOVETO &&
					s.segments[a].type==PathIterator.SEG_MOVETO) {
				//skip
			} else {
				return distance;
			}
		}
		return distance;
	}
	
	/** Trace the shape.
	 * 
	 * @param position a fraction from zero to one indicating where to start tracing
	 * @param length a fraction from negative one to one indicating how much to trace.
	 * If this value is negative then the shape will be traced backwards.
	 * @return a new path
	 */
	public GeneralPath getShape(double position,double length) {
		GeneralPath dest = new GeneralPath(Path2D.WIND_NON_ZERO);
		PathWriter w = new GeneralPathWriter(dest);
		writeShape(position,length,w,true);
		return dest;
	}
	
	static class Position {
		int i;
		double innerPosition;
		
		public Position(int segmentIndex,double p) {
			this.i = segmentIndex;
			this.innerPosition = p;
		}
		
		@Override
		public String toString() {
			return "Position[ i="+i+" t="+innerPosition+"]";
		}
	}
	
	private Position getIndexOfPosition(double p) {
		while(p<0) p+=1;
		while(p>1) p-=1;
		if(p>.99999f)
			p = 0;
		
		int i = 0;
		double original = p;
		while(i<segments.length) {
			if(p<=segments[i].normalizedDistance && segments[i].normalizedDistance!=0) {
				return new Position(i,p/segments[i].normalizedDistance);
			}
			p-=segments[i].normalizedDistance;
			i++;
		}
		System.err.println("p = "+p);
		throw new RuntimeException("the position "+original+" could not be found.");
	}
		
}
