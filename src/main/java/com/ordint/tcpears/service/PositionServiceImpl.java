package com.ordint.tcpears.service;

import java.io.IOException;
import java.util.Map;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.server.LatLongUtil;
import com.ordint.tcpears.server.Position;

public class PositionServiceImpl implements PositionService {
	
	private MemcacheHelper memcacheHelper;
	private GroupPositions groupPositions;
	private ClientDetailsResolver clientDetailsResolver = new AirplaneClientDetailsResolver();
	
	public PositionServiceImpl(MemcacheHelper memcacheHelper) {
		this.memcacheHelper = memcacheHelper;
		this.groupPositions = new GroupPositions(memcacheHelper);
	}
	
	@Override
	public void update(String positionInfo) {
		ClientDetails client = clientDetailsResolver.resolveClientDetails(positionInfo.substring(0, positionInfo.indexOf(",")));
		Position p = new Position(LatLongUtil.supplementMsg(positionInfo, client.getGroupId()));
		String clientKeyName="/ggps/location/" + p.getClientId();
		//memcacheHelper.set(clientKeyName, clientKeyName, p.asMessage());
		groupPositions.update(p);


	}

	
}
