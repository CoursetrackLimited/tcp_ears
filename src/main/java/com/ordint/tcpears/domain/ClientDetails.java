package com.ordint.tcpears.domain;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@RequiredArgsConstructor
@AllArgsConstructor
public class ClientDetails {
	private String groupId;
	private String clientId;
	@NonFinal
	private String fixedName;
	@NonFinal
	private String tempName;
	
	
	
	public String getCurrentName() {
		if (!isBlank(tempName)) {
			return tempName;
		} else if (!isBlank(fixedName)) {
			return fixedName;
		}  else {
			return clientId;
		}
	}
}
