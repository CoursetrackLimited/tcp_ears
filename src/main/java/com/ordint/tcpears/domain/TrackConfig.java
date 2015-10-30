package com.ordint.tcpears.domain;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class TrackConfig {	
	private long trackConfigId;
	private String kml;
	private String name;
	private String finishLine;
	private long venueId;

}
