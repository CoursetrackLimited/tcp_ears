package com.ordint.tcpears.util;

import java.time.Instant;

public interface TimeProvider {
	
	Instant now();
	
	long nanoSeconds();

}
