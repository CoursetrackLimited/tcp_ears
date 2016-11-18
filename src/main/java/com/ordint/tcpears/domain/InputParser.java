package com.ordint.tcpears.domain;

import java.util.Optional;

import com.ordint.tcpears.service.ClientManager;

public interface InputParser {
	
	Optional<Position> parse(String message, ClientManager clientManager);

}
