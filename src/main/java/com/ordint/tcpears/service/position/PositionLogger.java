package com.ordint.tcpears.service.position;

import com.ordint.tcpears.domain.lombok.Position;

public interface PositionLogger {

	void log(Position p, String vehicleType, String source);

}