package com.ordint.tcpears.service.impl;

import com.ordint.tcpears.domain.ClientDetails;
import com.ordint.tcpears.service.AdministrationService;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;


public class AdministrationServiceImpl implements AdministrationService {
	
	private final ClientManager clientManager;
	private final ClientDetailsResolver clientDetailsResolver;
	
	public AdministrationServiceImpl(ClientManager clientManager, ClientDetailsResolver clientDetailsResolver) {
		this.clientManager = clientManager;
		this.clientDetailsResolver = clientDetailsResolver;
	}	

	@Override
	public void startTracking(String groupId) {
		clientManager.trackGroup(groupId);
	}

	@Override
	public void stopTracking(String groupId) {
		clientManager.stopTrackingGroup(groupId);
	}

	@Override
	public void setClientGroup(String clientId, String groupId) {
		clientDetailsResolver.updateClientDetails(new ClientDetails( groupId, clientId));
	}
	@Override
	public void setClientsGroup(String[] clientIds, String groupId) {
		for(String clientId : clientIds) {
			setClientGroup(clientId, groupId);
		}
		
	}

	@Override
	public void setDefaultGroup(String groupId) {
		clientDetailsResolver.setDefaultGroup(groupId);
		
	}
	

}
