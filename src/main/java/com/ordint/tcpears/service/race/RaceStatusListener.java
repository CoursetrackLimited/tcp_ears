package com.ordint.tcpears.service.race;

import com.ordint.tcpears.service.race.RaceObserver.EventState;

public interface RaceStatusListener {

	void onStatusChange(EventState status);
}
