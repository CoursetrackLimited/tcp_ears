package com.ordint.tcpears.service;

public interface AdministrationService {
	
	void startTracking(String groupId);
	
	void stopTracking(String groupId);
	
	void setClientGroup(String clientId, String groupId);
	
	void setClientsGroup(String[] clientId, String groupId);
	
	void setDefaultGroup(String groupId);
	
	
}
