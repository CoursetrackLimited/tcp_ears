package com.ordint.tcpears.service;

import java.util.Optional;

import com.ordint.tcpears.domain.Position;

public interface ClientManager {

	void trackGroup(String groupId);

	void stopTrackingGroup(String groupId);
	
	void clearSnake(String groupId);
	
	void clearAllSnakes();

	void updatePostion(Position position);
	
	Optional<Position> getClientPosition(String clientId);

}