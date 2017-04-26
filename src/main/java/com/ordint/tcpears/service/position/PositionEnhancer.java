package com.ordint.tcpears.service.position;

import java.util.List;

import com.ordint.tcpears.domain.lombok.Position;


/**
 * Builds a group of Position objects
 * @author Tom
 *
 */
public interface PositionEnhancer {
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
	Position enhance(Position position);
	
}
