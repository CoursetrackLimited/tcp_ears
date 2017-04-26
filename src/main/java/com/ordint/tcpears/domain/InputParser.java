package com.ordint.tcpears.domain;

import java.util.Optional;

import com.ordint.tcpears.domain.lombok.Position;

public interface InputParser {
	
	Optional<Position> parse(String message);

}
