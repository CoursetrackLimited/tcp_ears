package com.ordint.tcpears.util;

/*
 * @(#)ShapeStringUtils.java
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

import java.awt.Shape;
import java.awt.geom.PathIterator;

/** This is a small set of static methods that translate shape data into
 * <code>java.lang.Strings</code> and vice versa.
 * 
 *
 */
public class ShapeStringUtils {

    /** This describes a shape in a <code>String</code>.  The model is used
     * is based on how SVG encodes shape data.
     * <P>The call:
     * <BR><code>ShapeUtils.createPathIterator(ShapeUtils.toString(shape));</code>
     * <BR>should result in an identical shape.
     * <P>The shape data is formatted as a single letter (m, l, q, c, z)
     * followed by the appropriate number of points (2, 2, 4, 6, 0 respectively).
     * This uses floats, not doubles, so it will not contain strings with an exponent
     * (i.e. "1.3e-4").
     * @param s the shape to describe
     * @return textual representation of that shape.
     */
    public static String toString(Shape s) {
        PathIterator i = s.getPathIterator(null);
        return toString(i);
    }
    
    public static String toString(PathIterator i) {
        float[] f = new float[6];
        //TODO: didn't you read once that a string buffer
        //is a little inefficient?
        StringBuffer sb = new StringBuffer();
        int k;
        int j = 0;
        while(i.isDone()==false) {
            k = i.currentSegment(f);
            
            if(k==PathIterator.SEG_MOVETO) {
                sb.append('m');
                j = 2;
            } else if(k==PathIterator.SEG_LINETO) {
                sb.append('l');
                j = 2;
            } else if(k==PathIterator.SEG_QUADTO) {
                sb.append('q');
                j = 4;
            } else if(k==PathIterator.SEG_CUBICTO) {
                sb.append('c');
                j = 6;
            } else if(k==PathIterator.SEG_CLOSE) {
                sb.append('z');
                j = 0;
            }
            if(j!=0) {
                sb.append(' ');
                for(int a = 0; a<j; a++) {
                    sb.append(Float.toString(f[a]));
                    if(a<j-1)
                        sb.append(' ');
                }
            }
            
            i.next();
            if(i.isDone()==false)
                sb.append(' ');
        }
        return sb.toString();
    }



    
}

