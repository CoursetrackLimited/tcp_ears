package com.ordint.tcpears.domain;

import com.ordint.tcpears.domain.lombok.Position;

public interface SnakeWriter {
	
	String write(Position p, String currentSnake);
	
	void calculateSnakeLength(int clientCount);
}
