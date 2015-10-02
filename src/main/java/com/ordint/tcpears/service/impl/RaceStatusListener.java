package com.ordint.tcpears.service.impl;

import com.ordint.tcpears.service.impl.RaceObserver.RaceStatus;

public interface RaceStatusListener {

	void onStatusChange(RaceStatus status);
}
