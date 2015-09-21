package com.ordint.tcpears.service.impl;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.service.ClientDetailsResolver;

public class AirplaneClientDetailsResolver implements ClientDetailsResolver {
	
	private ConcurrentHashMap<String, ClientDetails> clientDetailsMap = new ConcurrentHashMap<>();
	private final static String[] GROUPS = {"GR1","GR2","GR3","GR4"};
	private final Random rnd = new Random();
	@Override
	public ClientDetails resolveClientDetails(String clientId) {
		return clientDetailsMap.computeIfAbsent(clientId, val ->  new ClientDetails(GROUPS[rnd.nextInt(4)], clientId));
	}
	@Override
	public void updateClientDetails(ClientDetails clientDetail) {
		clientDetailsMap.put(clientDetail.getClientId(), clientDetail);
		
	}
	@Override
	public void setDefaultGroup(String groupId) {
		
	}
	@Override
	public void refresh() {
		
	}
	@Override
	public void updateClientDetails(List<ClientDetails> clientDetails) {
		// TODO Auto-generated method stub
		
	}

}
