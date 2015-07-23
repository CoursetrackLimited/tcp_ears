package com.ordint.tcpears.service.impl;

import java.time.Clock;

import com.ordint.tcpears.domain.DefaultInputParser;
import com.ordint.tcpears.domain.InputParser;
import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionLogger;
import com.ordint.tcpears.service.PositionService;

public class PositionServiceImpl implements PositionService {
	
	private ClientDetailsResolver clientDetailsResolver;;
	private ClientManager clientManager;
	private InputParser inputParser ;
	private PositionLogger positionLogger;
	
	public PositionServiceImpl() {}
	
	public PositionServiceImpl(ClientManager clientManager, ClientDetailsResolver clientDetailsResolve,
			PositionLogger positionLogger) {
		this.clientDetailsResolver = clientDetailsResolve;
		this.clientManager = clientManager;
		this.positionLogger = positionLogger;
		this.inputParser = new DefaultInputParser(clientDetailsResolver, Clock.systemUTC());
	}
	
	@Override
	public void update(String positionInfo) {
		
		Position p = inputParser.parse(positionInfo);
		positionLogger.log(p, "horse", "boxes");
		clientManager.updatePostion(p);	

	}



	
}
