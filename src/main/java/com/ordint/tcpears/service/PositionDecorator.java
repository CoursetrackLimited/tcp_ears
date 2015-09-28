package com.ordint.tcpears.service;

import java.util.List;

import com.ordint.tcpears.domain.Position;


/**
 * Builds a group of Position objects
 * @author Tom
 *
 */
public interface PositionDecorator {

	List<Position> decorate(List<Position> positions);
	
}
