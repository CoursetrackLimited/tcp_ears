package com.ordint.tcpears.service.impl;

import java.time.Clock;

import com.ordint.tcpears.domain.DefaultInputParser;
import com.ordint.tcpears.domain.InputParser;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionService;

public class PositionServiceImpl implements PositionService {
	
	private ClientDetailsResolver clientDetailsResolver = new AirplaneClientDetailsResolver();
	private ClientManager clientManager;
	private InputParser inputParser = new DefaultInputParser(clientDetailsResolver, Clock.systemUTC());
	
	public PositionServiceImpl(ClientManager clientManager, ClientDetailsResolver clientDetailsResolve) {
		this.clientDetailsResolver = clientDetailsResolve;
		this.clientManager = clientManager;
	}
	
	@Override
	public void update(String positionInfo) {
		
		Position p = inputParser.parse(positionInfo);
		
		clientManager.updatePostion(p);


	}



	
}
