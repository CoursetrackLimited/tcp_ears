package com.ordint.tcpears.domain.lombok;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Builder
@Value
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"currentName","currentGroupName"})
public class ClientDetails {
	private String groupId;
	private String clientId;
	@NonFinal
	private String fixedName;
	@NonFinal
	private String tempName;
	@NonFinal
	private String groupName;
	@NonFinal
	private String runnerIdent;
	
	
	
	@JsonCreator
	public ClientDetails() {
		clientId = "";
		groupId = "";
		runnerIdent = "";
	}	
	public String getCurrentName() {
		if (!isBlank(tempName)) {
			return tempName;
		} else if (!isBlank(fixedName)) {
			return fixedName;
		}  else {
			return clientId;
		}
	}
	
	public String getCurrentGroupName() {
		if (!isBlank(groupName)) {
			return groupName;
		} else if(!isBlank(groupId)){
			return groupId;
		} else {
			return "nogroup";
		}
			
	}
}
