package com.ordint.tcpears.server;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.service.AdministrationService;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionService;
import com.ordint.tcpears.service.impl.AdministrationServiceImpl;
import com.ordint.tcpears.service.impl.ClientManagerImpl;
import com.ordint.tcpears.service.impl.HorseDetailsResolver;
import com.ordint.tcpears.service.impl.PositionServiceImpl;

public class Config {
	MemcacheHelper memcacheHelper;
	ClientDetailsResolver clientDetailsResolver;
	ClientManager clientManager;
	public PositionService positionService() throws Exception {
		//memcacheHelper = new MemcacheHelper("localhost", 11211);
		memcacheHelper = new MemcacheHelper();
		clientDetailsResolver = new HorseDetailsResolver();
		clientManager = new ClientManagerImpl(memcacheHelper);
		return new PositionServiceImpl(clientManager, clientDetailsResolver);
	}
	
	public AdministrationService administrationService() {
		return new AdministrationServiceImpl(clientManager, clientDetailsResolver);
	}
	
}
