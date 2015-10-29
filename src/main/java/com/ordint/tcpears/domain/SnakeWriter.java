package com.ordint.tcpears.domain;

public interface SnakeWriter {
	
	String write(Position p, String currentSnake);
	
	void calculateSnakeLength(int clientCount);
}
