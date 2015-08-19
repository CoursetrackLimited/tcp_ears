package com.ordint.tcpears.util.prediction;

import java.awt.geom.Path2D;


/**
 * Builds {@link Path2D} objects 
 * 
 * @author Tom
 *
 */
public interface TrackPathBuilder {
	
	Path2D build(String name);

}
