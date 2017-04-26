package com.ordint.tcpears.service;

import java.util.List;

import com.ordint.tcpears.domain.lombok.ClientDetails;

public interface ClientDetailsResolver {
	
	ClientDetails resolveClientDetails(String clientId);
	
	void updateClientDetails(ClientDetails clientDetails);
	
	void updateClientDetails(List<ClientDetails> clientDetails);
	
	void setDefaultGroup(String groupId);
	
	void refresh();
	
}
