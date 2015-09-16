package com.ordint.tcpears.service;

import java.time.LocalDateTime;

public interface ReplayService {
	
	
	
	boolean endReplay(String replayId);

	String replayFrom(LocalDateTime startDateTime, int numberOfSeconds, boolean useOriginalTimestamp);
	
	
}
