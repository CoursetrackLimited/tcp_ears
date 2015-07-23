package com.ordint.tcpears.service;

import com.ordint.tcpears.domain.Position;

public interface PositionLogger {

	void log(Position p, String vehicleType, String source);

}