package com.ordint.tcpears.service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AirplaneClientDetailsResolver implements ClientDetailsResolver {
	
	private ConcurrentHashMap<String, ClientDetails> clientDetails = new ConcurrentHashMap<>();
	private final static String[] GROUPS = {"GR1","GR2","GR3","GR4"};
	private final Random rnd = new Random();
	@Override
	public ClientDetails resolveClientDetails(String clientId) {
		
		return clientDetails.computeIfAbsent(clientId, val -> {		
			return new ClientDetails(clientId, GROUPS[rnd.nextInt(4)], clientId);
		});
	}

}
