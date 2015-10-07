package com.ordint.tcpears.util.time;

import java.time.Instant;

public interface TimeProvider {
	
	Instant now();
	
	long nanoSeconds();

}
