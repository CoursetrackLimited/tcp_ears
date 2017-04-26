package com.ordint.tcpears.service;

import com.ordint.tcpears.domain.lombok.TrackConfig;

public interface TrackConfigService {

	TrackConfig loadTrackConfig(long id);
	
}
