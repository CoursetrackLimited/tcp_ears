package com.ordint.tcpears.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.service.ClientDetailsResolver;

public class HorseDetailsResolver implements ClientDetailsResolver {
	
	private ConcurrentMap<String, ClientDetails> clientDetailsMap = new ConcurrentHashMap<>();
	private final static String UNKNOWN_GROUP ="nogroup";
	
	
	protected void init() {
		//fetch from database on startup only
	}

	@Override
	public ClientDetails resolveClientDetails(String clientId) {	
		return clientDetailsMap.computeIfAbsent(clientId, val -> new ClientDetails(UNKNOWN_GROUP, clientId));
	}
	@Override
	public void updateClientDetails(ClientDetails clientDetail) {
		clientDetailsMap.put(clientDetail.getClientId(), clientDetail);		
	}
}
