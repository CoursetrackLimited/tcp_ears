package com.ordint.tcpears.domain;

public interface TrackWriter {
	
	String write(Position p, String currentTrack);
	
	void calculateTrackLength(int clientCount);
}
