package com.ordint.tcpears.domain;

import static org.apache.commons.lang3.StringUtils.isBlank;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
	@JsonCreator
	public ClientDetails() {
		clientId = "";
		groupId = "";
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
