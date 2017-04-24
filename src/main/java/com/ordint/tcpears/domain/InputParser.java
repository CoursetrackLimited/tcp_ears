package com.ordint.tcpears.domain;

import java.util.Optional;

public interface InputParser {
	
	Optional<Position> parse(String message);

}
