package com.ordint.tcpears.service;

import java.time.LocalDateTime;

public interface ReplayService {
	
	String replayFrom(LocalDateTime dateTime, int numberOfSeconds);
	
	boolean endReplay(String replayId);
	
	
}
