package com.ordint.tcpears.service;

import java.io.IOException;

public interface PositionPublisher {

	void publishPositions() throws IOException;

	void publishSnakes() throws Exception;

	void clearSnake(String groupId);
	
	void clearAllSnakes();

	void clearPositions(String groupId);

}