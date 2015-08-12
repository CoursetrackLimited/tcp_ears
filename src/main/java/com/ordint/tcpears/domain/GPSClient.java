package com.ordint.tcpears.domain;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class GPSClient {
	
	private Position currentPosition;
	private ConcurrentLinkedDeque<Position> history;
	private int maxHistorySize = 6000;
	private int historyCount = 0;
	
	public boolean isActive() {
		return true;
	}
	
	public void update(Position position) {
		this.currentPosition = position;
		if (historyCount == maxHistorySize) {
			history.removeLast();
		} else {
			historyCount ++;
		}
		
		history.addFirst(position);
		
	}
	
	public void clearHistory() {
		
	}

}
