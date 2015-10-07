package com.ordint.tcpears.service.race;

import com.ordint.tcpears.service.race.RaceObserver.RaceStatus;

public interface RaceStatusListener {

	void onStatusChange(RaceStatus status);
}
