package com.ordint.tcpears.service;

import com.ordint.tcpears.domain.ClientDetails;

public interface ClientDetailsResolver {
	
	ClientDetails resolveClientDetails(String clientId);
	
	void updateClientDetails(ClientDetails clientDetails);
	
	void setDefaultGroup(String groupId);
	
}
