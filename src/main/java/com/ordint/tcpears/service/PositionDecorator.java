package com.ordint.tcpears.service;

import java.util.List;

import com.ordint.tcpears.domain.Position;


/**
 * Builds a group of Position objects
 * @author Tom
 *
 */
public interface PositionDecorator {
	/**
	 * Modifies a list of Positions in some way
	 * @param positions
	 * @return a List of the modified positions
	 */
	List<Position> decorate(List<Position> positions);
	
	/**
	 * Modifies a single Position
	 * @param position
	 * @return
	 */
	Position decorate(Position position);
	
}
