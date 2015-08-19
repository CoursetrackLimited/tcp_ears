package com.ordint.tcpears.service;

import java.io.IOException;

public interface PositionPublisher {

	void publishPositions() throws IOException;

	void publishTracks() throws Exception;

	void clearTrack(String groupId);

	void clearPositions(String groupId);

}