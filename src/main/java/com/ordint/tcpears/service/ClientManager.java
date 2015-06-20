package com.ordint.tcpears.service;

import com.ordint.tcpears.domain.Position;

public interface ClientManager {

	void trackGroup(String groupId);

	void stopTrackingGroup(String groupId);

	void updatePostion(Position position);

}